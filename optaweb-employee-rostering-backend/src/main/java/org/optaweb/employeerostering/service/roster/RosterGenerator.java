/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.service.roster;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.employee.CovidRiskType;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.domain.roster.Roster;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.rotation.ShiftTemplate;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.service.admin.SystemPropertiesRetriever;
import org.optaweb.employeerostering.service.common.generator.StringDataGenerator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Component
public class RosterGenerator implements ApplicationRunner {

    private static final double[] EXTRA_SHIFT_THRESHOLDS = {0.5, 0.8, 0.95};

    public static class GeneratorType {

        public final String tenantNamePrefix;
        public final StringDataGenerator skillNameGenerator;
        public final StringDataGenerator spotNameGenerator;

        // Start and end time per timeslot
        public final List<Triple<LocalTime, LocalTime, List<DayOfWeek>>> timeslotRangeList;
        public final int rotationLength;
        public final int rotationEmployeeListSize;
        public final BiFunction<Integer, Integer, List<Integer>> rotationEmployeeIndexCalculator;

        public GeneratorType(String tenantNamePrefix, StringDataGenerator skillNameGenerator,
                             StringDataGenerator spotNameGenerator,
                             List<Triple<LocalTime, LocalTime, List<DayOfWeek>>> timeslotRangeList,
                             int rotationLength, int rotationEmployeeListSize,
                             BiFunction<Integer, Integer, List<Integer>> rotationEmployeeIndexCalculator) {
            this.tenantNamePrefix = tenantNamePrefix;
            this.skillNameGenerator = skillNameGenerator;
            this.spotNameGenerator = spotNameGenerator;
            this.timeslotRangeList = timeslotRangeList;
            this.rotationLength = rotationLength;
            this.rotationEmployeeListSize = rotationEmployeeListSize;
            this.rotationEmployeeIndexCalculator = rotationEmployeeIndexCalculator;
        }
    }

    private final StringDataGenerator tenantNameGenerator = StringDataGenerator.buildLocationNames();
    private final StringDataGenerator employeeNameGenerator = StringDataGenerator.buildFullNames();

    private final List<DayOfWeek> ALL_WEEK = Arrays.asList(DayOfWeek.values());
    private final List<DayOfWeek> WEEKDAYS = Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                                           DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
    private final List<DayOfWeek> WEEKENDS = Arrays.asList(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    private final GeneratorType hospitalGeneratorType = new GeneratorType(
            "",
            new StringDataGenerator()
                    .addPart(
                            "Ambulatory care",
                            "Critical care",
                            "Midwife",
                            "Gastroenterology",
                            "Neuroscience",
                            "Oncology",
                            "Pediatric",
                            "Psychiatric",
                            "Geriatric",
                            "Radiology")
                    .addPart(
                            "nurse",
                            "physician",
                            "doctor",
                            "attendant",
                            "specialist",
                            "surgeon",
                            "medic",
                            "practitioner",
                            "pharmacist",
                            "researcher"),
            new StringDataGenerator(true)
                    .addPart(false, 0,
                             "Basic",
                             "Advanced",
                             "Expert",
                             "Specialized",
                             "Elder",
                             "Child",
                             "Infant",
                             "Baby",
                             "Male",
                             "Female",
                             "Common",
                             "Uncommon",
                             "Research",
                             "Administrative",
                             "Regressing")
                    .addPart(true, 1,
                             "anaesthetics",
                             "cardiology",
                             "critical care",
                             "emergency",
                             "ear nose throat",
                             "gastroenterology",
                             "haematology",
                             "maternity",
                             "neurology",
                             "oncology",
                             "ophthalmology",
                             "orthopaedics",
                             "physiotherapy",
                             "radiotherapy",
                             "urology")
                    .addPart(false, 0,
                             "Alpha",
                             "Beta",
                             "Gamma",
                             "Delta",
                             "Epsilon",
                             "Zeta",
                             "Eta",
                             "Theta",
                             "Iota",
                             "Kappa",
                             "Lambda",
                             "Mu",
                             "Nu",
                             "Xi",
                             "Omicron"),
            Arrays.asList(
                    Triple.of(LocalTime.of(6, 0), LocalTime.of(14, 0), ALL_WEEK),
                    Triple.of(LocalTime.of(9, 0), LocalTime.of(17, 0), ALL_WEEK),
                    Triple.of(LocalTime.of(14, 0), LocalTime.of(22, 0), ALL_WEEK),
                    Triple.of(LocalTime.of(22, 0), LocalTime.of(6, 0), ALL_WEEK)),
            // Morning:   A A A A A D D B B B B B D D C C C C C D D
            // Day:       F F B B B F F F F C C C F F F F A A A F F
            // Afternoon: D D D E E E E D D D E E E E D D D E E E E
            // Night:     E C C C C C C E A A A A A A E B B B B B B
            21, 12, (startDayOffset, timeslotRangesIndex) -> {
        switch (timeslotRangesIndex) {
            case 0:
                return startDayOffset % 7 >= 5 ? Arrays.asList(3, 9) :
                        Arrays.asList(startDayOffset / 7, startDayOffset / 7 + 6);
            case 1:
                return (startDayOffset + 2) % 7 < 4 ? Arrays.asList(5, 11) :
                        Arrays.asList((startDayOffset - 16 + 21) % 21 / 7, (startDayOffset - 16 + 21) % 21 / 7 + 6);
            case 2:
                return startDayOffset % 7 < 3 ? Arrays.asList(3, 9) : Arrays.asList(4, 10);
            case 3:
                return startDayOffset % 7 < 1 ? Arrays.asList(4, 10) :
                        Arrays.asList((startDayOffset - 8 + 21) % 21 / 7, (startDayOffset - 8 + 21) % 21 / 7 + 6);
            default:
                throw new IllegalStateException("Impossible state for timeslotRangesIndex (" + timeslotRangesIndex
                                                        + ").");
        }
    });

    private Random random;
    private final String COVID19 = "COVID-19";

    private Skill COVID_SKILL;
    private Skill DOCTOR_SKILL;
    private Skill NURSE_SKILL;

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unused")
    public RosterGenerator() {
    }

    /**
     * For benchmark only
     *
     * @param entityManager never null
     */
    public RosterGenerator(EntityManager entityManager) {
        this.entityManager = entityManager;
        random = new Random(37);
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        checkForExistingData();
    }

    @Transactional
    public void checkForExistingData() {
        // Check if Tenant entities already exist before generating data
        List<Tenant> tenantList = entityManager.createQuery("select t from Tenant t").getResultList();

        if (!tenantList.isEmpty()) {
            return;
        }

        setUpGeneratedData();
    }

    @Transactional
    public void setUpGeneratedData() {
        ZoneId zoneId = SystemPropertiesRetriever.determineZoneId();
        SystemPropertiesRetriever.InitialData initialData = SystemPropertiesRetriever.determineInitialData();

        random = new Random(37);

        switch (initialData) {
            case EMPTY:
                return;
            case DEMO_DATA:
                tenantNameGenerator.predictMaximumSizeAndReset(12);
                generateRoster(6, 7, hospitalGeneratorType, zoneId);
                generateRoster(11, 7 * 4, hospitalGeneratorType, zoneId);
                generateRoster(16, 7 * 2, hospitalGeneratorType, zoneId);
        }
    }

    @Transactional
    public Roster generateRoster(int spotListSize,
                                 int lengthInDays,
                                 RosterGenerator.GeneratorType generatorType,
                                 ZoneId zoneId) {
        int maxShiftSizePerDay = generatorType.timeslotRangeList.size() + EXTRA_SHIFT_THRESHOLDS.length;
        // The average employee works 5 days out of 7
        int employeeListSize = (int) Math.ceil(spotListSize * maxShiftSizePerDay * 7 * 1.5);
        int skillListSize = (spotListSize + 4) / 5;

        Tenant tenant = createTenant(generatorType, employeeListSize);
        Integer tenantId = tenant.getId();
        RosterConstraintConfiguration rosterConstraintConfiguration = createTenantConfiguration(generatorType,
                                                                                                tenantId, zoneId);
        RosterState rosterState = createRosterState(generatorType, tenant, zoneId, lengthInDays);

        List<Skill> skillList = createSkillList(generatorType, tenantId, skillListSize);
        List<Spot> spotList = createSpotList(generatorType, tenantId, spotListSize, skillList);
        List<Contract> contractList = createContractList(tenantId);
        List<Employee> employeeList = createEmployeeList(generatorType, tenantId, employeeListSize,
                                                         contractList, skillList);
        List<ShiftTemplate> shiftTemplateList = createShiftTemplateList(generatorType, tenantId,
                                                                        rosterState, spotList,
                                                                        employeeList, skillList);
        List<Shift> shiftList = createShiftList(generatorType, tenantId, rosterConstraintConfiguration,
                                                rosterState, spotList, shiftTemplateList);
        List<EmployeeAvailability> employeeAvailabilityList = createEmployeeAvailabilityList(
                generatorType, tenantId, rosterConstraintConfiguration, rosterState, employeeList, shiftList);

        return new Roster((long) tenantId, tenantId, rosterConstraintConfiguration, skillList, spotList, employeeList,
                          employeeAvailabilityList, rosterState, shiftList);
    }

    @Transactional
    public Roster generateRoster(int spotListSize, int lengthInDays) {
        ZoneId zoneId = SystemPropertiesRetriever.determineZoneId();
        return generateRoster(spotListSize, lengthInDays, hospitalGeneratorType, zoneId);
    }

    @Transactional
    public Tenant createTenant(GeneratorType generatorType, int employeeListSize) {
        String tenantName = generatorType.tenantNamePrefix + " " + tenantNameGenerator.generateNextValue() + " ("
                + employeeListSize + " employees)";
        Tenant tenant = new Tenant(tenantName);
        entityManager.persist(tenant);
        return tenant;
    }

    @Transactional
    public RosterConstraintConfiguration createTenantConfiguration(GeneratorType generatorType, Integer tenantId,
                                                                   ZoneId zoneId) {
        RosterConstraintConfiguration rosterConstraintConfiguration = new RosterConstraintConfiguration();
        rosterConstraintConfiguration.setTenantId(tenantId);
        entityManager.persist(rosterConstraintConfiguration);
        return rosterConstraintConfiguration;
    }

    @Transactional
    public RosterState createRosterState(GeneratorType generatorType, Tenant tenant, ZoneId zoneId, int lengthInDays) {
        RosterState rosterState = new RosterState();
        rosterState.setTenantId(tenant.getId());
        int publishNotice = 14;
        rosterState.setPublishNotice(publishNotice);
        LocalDate firstDraftDate = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .plusDays(publishNotice);
        rosterState.setFirstDraftDate(firstDraftDate);
        // publishLength is read-only and set to 1 day
        //rosterState.setPublishLength(1);
        rosterState.setDraftLength(14);
        rosterState.setUnplannedRotationOffset(0);
        rosterState.setRotationLength(generatorType.rotationLength);
        rosterState.setLastHistoricDate(LocalDate.now().minusDays(1));
        rosterState.setTimeZone(zoneId);
        rosterState.setTenant(tenant);
        entityManager.persist(rosterState);
        return rosterState;
    }

    @Transactional
    public List<Skill> createSkillList(GeneratorType generatorType, Integer tenantId, int size) {
        List<Skill> skillList = new ArrayList<>(size + 3);
        generatorType.skillNameGenerator.predictMaximumSizeAndReset(size);
        COVID_SKILL = new Skill(tenantId, "Respiratory Specialist");
        DOCTOR_SKILL = new Skill(tenantId, "Doctor");
        NURSE_SKILL = new Skill(tenantId, "Nurse");

        entityManager.persist(COVID_SKILL);
        entityManager.persist(DOCTOR_SKILL);
        entityManager.persist(NURSE_SKILL);

        skillList.addAll(Arrays.asList(COVID_SKILL, DOCTOR_SKILL, NURSE_SKILL));
        for (int i = 0; i < size; i++) {
            String name = generatorType.skillNameGenerator.generateNextValue();
            Skill skill = new Skill(tenantId, name);
            entityManager.persist(skill);
            skillList.add(skill);
        }
        return skillList;
    }

    @Transactional
    public List<Spot> createSpotList(GeneratorType generatorType, Integer tenantId, int size, List<Skill> skillList) {
        List<Spot> spotList = new ArrayList<>(size);
        generatorType.spotNameGenerator.predictMaximumSizeAndReset(size);
        final int NUM_OF_COVID_WARDS = 1;

        for (int i = 0; i < NUM_OF_COVID_WARDS; i++) {
            Set<Skill> requiredSkillSet = new HashSet<>();
            Spot spot = new Spot(tenantId, COVID19 + " Ward " + (i + 1), requiredSkillSet, true);
            entityManager.persist(spot);
            spotList.add(spot);
        }

        for (int i = 0; i < size - NUM_OF_COVID_WARDS; i++) {
            String name = generatorType.spotNameGenerator.generateNextValue();
            Set<Skill> requiredSkillSet = new HashSet<>(extractRandomSubList(
                    skillList.subList(3,
                                      skillList.size()),
                    0.5, 0.9, 1.0));
            if (i == 0) {
                requiredSkillSet.add(COVID_SKILL);
            }
            Spot spot = new Spot(tenantId, name, requiredSkillSet, false);
            entityManager.persist(spot);
            spotList.add(spot);
        }
        return spotList;
    }

    @Transactional
    public List<Contract> createContractList(Integer tenantId) {
        List<Contract> contractList = new ArrayList<>(1);

        Contract contract = new Contract(tenantId, "Max 48 Hours Per Week Contract", null, 48 * 60, null, null);
        entityManager.persist(contract);
        contractList.add(contract);

        return contractList;
    }

    @Transactional
    public List<Employee> createEmployeeList(GeneratorType generatorType, Integer tenantId, int size,
                                             List<Contract> contractList, List<Skill> generalSkillList) {
        List<Employee> employeeList = new ArrayList<>(size);
        employeeNameGenerator.predictMaximumSizeAndReset(size);
        for (int i = 0; i < size; i++) {
            String name = employeeNameGenerator.generateNextValue();
            HashSet<Skill> skillProficiencySet = new HashSet<>(
                    extractRandomSubList(generalSkillList.subList(3,
                                                                  generalSkillList.size()),
                                         0.0, 0.1, 0.3, 0.5, 0.7, 0.9));
            if (random.nextDouble() < 0.2) {
                skillProficiencySet.add(DOCTOR_SKILL);
            } else {
                skillProficiencySet.add(NURSE_SKILL);
            }

            if (random.nextDouble() < 0.35) {
                skillProficiencySet.add(COVID_SKILL);
            }

            CovidRiskType covidRisk = Arrays.asList(CovidRiskType.values())
                    .get(generateRandomIntFromThresholds(0.061, 0.303, 0.685, 0.927));
            Employee employee = new Employee(tenantId, name,
                                             contractList.get(0),
                                             skillProficiencySet, covidRisk);
            entityManager.persist(employee);
            employeeList.add(employee);
        }
        return employeeList;
    }

    @Transactional
    public List<ShiftTemplate> createShiftTemplateList(GeneratorType generatorType,
                                                       Integer tenantId,
                                                       RosterState rosterState,
                                                       List<Spot> spotList,
                                                       List<Employee> employeeList,
                                                       List<Skill> skillList) {
        int rotationLength = rosterState.getRotationLength();
        List<ShiftTemplate> shiftTemplateList = new ArrayList<>(spotList.size() * rotationLength *
                                                                        generatorType.timeslotRangeList.size());
        List<Employee> remainingEmployeeList = new ArrayList<>(employeeList);
        Consumer<Spot> createShiftTemplatesForWard = (spot) -> {
            final Function<Predicate<Employee>, List<Employee>> findEmployees = p -> {
                List<Employee> out = remainingEmployeeList.stream()
                        .filter(employee -> employee.getSkillProficiencySet().containsAll(spot.getRequiredSkillSet()) &&
                                (!spot.isCovidWard() ||
                                        employee.getCovidRiskType() != CovidRiskType.EXTREME) && p.test(employee))
                        .limit(generatorType.rotationEmployeeListSize).collect(toList());
                remainingEmployeeList.removeAll(out);
                return out;
            };
            List<Employee> doctorRotationEmployeeList = findEmployees
                    .apply(employee -> employee.getSkillProficiencySet().contains(DOCTOR_SKILL));
            List<Employee> nurseRotationEmployeeList = findEmployees
                    .apply(employee -> employee.getSkillProficiencySet().contains(NURSE_SKILL));

            List<Employee> covidRotationEmployeeList;

            if (spot.isCovidWard()) {
                covidRotationEmployeeList = findEmployees
                        .apply(employee -> employee.getSkillProficiencySet().contains(NURSE_SKILL) &&
                                employee.getSkillProficiencySet().contains(COVID_SKILL));
            } else {
                covidRotationEmployeeList = Collections.emptyList();
            }

            // For every day in the rotation (independent of publishLength and draftLength)
            for (int startDayOffset = 0; startDayOffset < rotationLength; startDayOffset++) {
                // Fill the offset day with shift templates
                for (int timeslotRangesIndex = 0; timeslotRangesIndex < generatorType.timeslotRangeList.size();
                        timeslotRangesIndex++) {
                    Triple<LocalTime, LocalTime, List<DayOfWeek>> timeslotRange =
                            generatorType.timeslotRangeList.get(timeslotRangesIndex);
                    final int dayOfWeek = (startDayOffset % 7) + 1;
                    // Only generate shifts for days in the timeslot
                    if (timeslotRange.getRight().stream().anyMatch(d -> d.getValue() == dayOfWeek)) {
                        LocalTime startTime = timeslotRange.getLeft();
                        LocalTime endTime = timeslotRange.getMiddle();
                        int endDayOffset = startDayOffset;
                        if (endTime.compareTo(startTime) < 0) {
                            endDayOffset = (startDayOffset + 1) % rotationLength;
                        }
                        int rotationEmployeeIndex = generatorType.rotationEmployeeIndexCalculator
                                .apply(startDayOffset, timeslotRangesIndex).get(0);
                        if (rotationEmployeeIndex < 0 || rotationEmployeeIndex >=
                                generatorType.rotationEmployeeListSize) {
                            throw new IllegalStateException(
                                    "The rotationEmployeeIndexCalculator for generatorType (" +
                                            generatorType.tenantNamePrefix +
                                            ") returns an invalid rotationEmployeeIndex (" + rotationEmployeeIndex +
                                            ") for startDayOffset (" + startDayOffset + ") and timeslotRangesIndex (" +
                                            timeslotRangesIndex + ").");
                        }
                        // There might be less employees than we need (overconstrained planning)
                        Employee rotationEmployee = rotationEmployeeIndex >= doctorRotationEmployeeList.size() ? null :
                                doctorRotationEmployeeList.get(rotationEmployeeIndex);
                        ShiftTemplate shiftTemplate = new ShiftTemplate(tenantId, spot, startDayOffset, startTime,
                                                                        endDayOffset, endTime, rotationEmployee,
                                                                        Arrays.asList(DOCTOR_SKILL));
                        entityManager.persist(shiftTemplate);
                        shiftTemplateList.add(shiftTemplate);

                        rotationEmployeeIndex = generatorType.rotationEmployeeIndexCalculator
                                .apply(startDayOffset, timeslotRangesIndex).get(0);
                        rotationEmployee = rotationEmployeeIndex >= nurseRotationEmployeeList.size() ? null :
                                nurseRotationEmployeeList.get(rotationEmployeeIndex);
                        shiftTemplate = new ShiftTemplate(tenantId, spot, startDayOffset, startTime,
                                                          endDayOffset, endTime, rotationEmployee,
                                                          Arrays.asList(NURSE_SKILL));
                        entityManager.persist(shiftTemplate);
                        shiftTemplateList.add(shiftTemplate);

                        rotationEmployeeIndex = generatorType.rotationEmployeeIndexCalculator
                                .apply(startDayOffset, timeslotRangesIndex).get(1);
                        rotationEmployee = rotationEmployeeIndex >= nurseRotationEmployeeList.size() ? null :
                                nurseRotationEmployeeList.get(rotationEmployeeIndex);
                        shiftTemplate = new ShiftTemplate(tenantId, spot, startDayOffset, startTime,
                                                          endDayOffset, endTime, rotationEmployee,
                                                          Arrays.asList(NURSE_SKILL));
                        entityManager.persist(shiftTemplate);
                        shiftTemplateList.add(shiftTemplate);

                        if (spot.isCovidWard()) {
                            rotationEmployeeIndex = generatorType.rotationEmployeeIndexCalculator
                                    .apply(startDayOffset, timeslotRangesIndex).get(1);
                            rotationEmployee = rotationEmployeeIndex >= covidRotationEmployeeList.size() ? null :
                                    covidRotationEmployeeList.get(rotationEmployeeIndex);
                            shiftTemplate = new ShiftTemplate(tenantId, spot, startDayOffset, startTime,
                                                              endDayOffset, endTime, rotationEmployee,
                                                              Arrays.asList(COVID_SKILL,
                                                                            NURSE_SKILL));
                            entityManager.persist(shiftTemplate);
                            shiftTemplateList.add(shiftTemplate);
                        }
                    }
                }
            }
        };
        // Create COVID templates first as they are more limited
        spotList.stream().filter(Spot::isCovidWard).forEach(createShiftTemplatesForWard);
        spotList.stream().filter(s -> !s.isCovidWard()).forEach(createShiftTemplatesForWard);
        return shiftTemplateList;
    }

    @Transactional
    public List<Shift> createShiftList(GeneratorType generatorType,
                                       Integer tenantId,
                                       RosterConstraintConfiguration rosterConstraintConfiguration,
                                       RosterState rosterState,
                                       List<Spot> spotList,
                                       List<ShiftTemplate> shiftTemplateList) {
        ZoneId zoneId = rosterState.getTimeZone();
        int rotationLength = rosterState.getRotationLength();
        LocalDate date = rosterState.getLastHistoricDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate firstDraftDate = rosterState.getFirstDraftDate();
        LocalDate firstUnplannedDate = rosterState.getFirstUnplannedDate();

        List<Shift> shiftList = new ArrayList<>();
        Map<Pair<Integer, Spot>, List<ShiftTemplate>> dayOffsetAndSpotToShiftTemplateListMap = shiftTemplateList
                .stream()
                .collect(groupingBy(shiftTemplate -> Pair.of(shiftTemplate.getStartDayOffset(),
                                                             shiftTemplate.getSpot())));
        int dayOffset = 0;
        while (date.compareTo(firstUnplannedDate) < 0) {
            for (Spot spot : spotList) {
                List<ShiftTemplate> subShiftTemplateList = dayOffsetAndSpotToShiftTemplateListMap.getOrDefault(
                        Pair.of(dayOffset, spot), Collections.emptyList());
                for (ShiftTemplate shiftTemplate : subShiftTemplateList) {
                    boolean defaultToRotationEmployee = date.compareTo(firstDraftDate) < 0;
                    Shift shift = shiftTemplate.createShiftOnDate(date, rosterState.getRotationLength(),
                                                                  zoneId, defaultToRotationEmployee);
                    if (date.compareTo(firstDraftDate) < 0) {
                        shift.setOriginalEmployee(shiftTemplate.getRotationEmployee());
                    }
                    entityManager.persist(shift);
                    shiftList.add(shift);
                }
                if (date.compareTo(firstDraftDate) >= 0 && !subShiftTemplateList.isEmpty()) {
                    int extraShiftCount = generateRandomIntFromThresholds(EXTRA_SHIFT_THRESHOLDS);
                    for (int i = 0; i < extraShiftCount; i++) {
                        ShiftTemplate shiftTemplate = extractRandomElement(subShiftTemplateList);
                        Shift shift = shiftTemplate.createShiftOnDate(date, rosterState.getRotationLength(),
                                                                      zoneId, false);
                        entityManager.persist(shift);
                        shiftList.add(shift);
                    }
                }
            }
            date = date.plusDays(1);
            dayOffset = (dayOffset + 1) % rotationLength;
        }
        rosterState.setUnplannedRotationOffset(dayOffset);
        return shiftList;
    }

    @Transactional
    public List<EmployeeAvailability> createEmployeeAvailabilityList(GeneratorType generatorType,
                                                                     Integer tenantId,
                                                                     RosterConstraintConfiguration
                                                                             rosterConstraintConfiguration,
                                                                     RosterState rosterState,
                                                                     List<Employee> employeeList,
                                                                     List<Shift> shiftList) {
        ZoneId zoneId = rosterState.getTimeZone();
        // Generate a feasible published schedule: no EmployeeAvailability instancer during the published period
        // nor on the first draft day (because they might overlap with shift on the last published day)
        LocalDate date = rosterState.getFirstDraftDate().plusDays(1);
        LocalDate firstUnplannedDate = rosterState.getFirstUnplannedDate();
        List<EmployeeAvailability> employeeAvailabilityList = new ArrayList<>();
        Map<LocalDate, List<Shift>> startDayToShiftListMap = shiftList.stream()
                .collect(groupingBy(shift -> shift.getStartDateTime().toLocalDate()));

        while (date.compareTo(firstUnplannedDate) < 0) {
            List<Shift> dayShiftList = startDayToShiftListMap.getOrDefault(date, Collections.emptyList());
            List<Employee> availableEmployeeList = new ArrayList<>(employeeList);
            int stateCount = (employeeList.size() - dayShiftList.size()) / 4;
            if (stateCount <= 0) {
                // Heavy overconstrained planning (more shifts than employees)
                stateCount = 1;
            }
            for (EmployeeAvailabilityState state : EmployeeAvailabilityState.values()) {
                for (int i = 0; i < stateCount; i++) {
                    Employee employee = availableEmployeeList.remove(random.nextInt(availableEmployeeList.size()));
                    LocalDateTime startDateTime = date.atTime(LocalTime.MIN);
                    LocalDateTime endDateTime = date.plusDays(1).atTime(LocalTime.MIN);
                    OffsetDateTime startOffsetDateTime = OffsetDateTime.of(startDateTime,
                                                                           zoneId.getRules().getOffset(startDateTime));
                    OffsetDateTime endOffsetDateTime = OffsetDateTime.of(endDateTime,
                                                                         zoneId.getRules().getOffset(endDateTime));
                    EmployeeAvailability employeeAvailability = new EmployeeAvailability(tenantId, employee,
                                                                                         startOffsetDateTime,
                                                                                         endOffsetDateTime);
                    employeeAvailability.setState(state);
                    entityManager.persist(employeeAvailability);
                    employeeAvailabilityList.add(employeeAvailability);
                }
            }
            date = date.plusDays(1);
        }
        return employeeAvailabilityList;
    }

    private <E> E extractRandomElement(List<E> list) {
        return list.get(random.nextInt(list.size()));
    }

    private <E> List<E> extractRandomSubList(List<E> list, double... thresholds) {
        int size = generateRandomIntFromThresholds(thresholds);
        if (size > list.size()) {
            size = list.size();
        }
        return extractRandomSubListOfSize(list, size);
    }

    private <E> List<E> extractRandomSubListOfSize(List<E> list, int size) {
        List<E> subList = new ArrayList<>(list);
        Collections.shuffle(subList, random);
        // Remove elements not in the sublist (so it can be garbage collected)
        subList.subList(size, subList.size()).clear();
        return subList;
    }

    private int generateRandomIntFromThresholds(double... thresholds) {
        double randomDouble = random.nextDouble();
        for (int i = 0; i < thresholds.length; i++) {
            if (randomDouble < thresholds[i]) {
                return i;
            }
        }
        return thresholds.length;
    }
}
