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
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.apache.commons.lang3.tuple.Pair;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.domain.roster.Roster;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.rotation.ShiftTemplate;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.tenant.RosterParametrization;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.service.admin.SystemPropertiesRetriever;
import org.optaweb.employeerostering.service.common.generator.StringDataGenerator;
import org.optaweb.employeerostering.service.contract.ContractRepository;
import org.optaweb.employeerostering.service.employee.EmployeeRepository;
import org.optaweb.employeerostering.service.rotation.ShiftTemplateRepository;
import org.optaweb.employeerostering.service.skill.SkillRepository;
import org.optaweb.employeerostering.service.spot.SpotRepository;
import org.springframework.stereotype.Component;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Component
public class RosterGenerator {

    private static final double[] EXTRA_SHIFT_THRESHOLDS = {0.5, 0.8, 0.95};

    private static class GeneratorType {

        private final String tenantNamePrefix;
        private final StringDataGenerator skillNameGenerator;
        private final StringDataGenerator spotNameGenerator;
        private final List<Pair<LocalTime, LocalTime>> timeslotRangeList; // Start and end time per timeslot
        private final int rotationLength;
        private final int rotationEmployeeListSize;
        private final BiFunction<Integer, Integer, Integer> rotationEmployeeIndexCalculator;

        public GeneratorType(String tenantNamePrefix, StringDataGenerator skillNameGenerator,
                             StringDataGenerator spotNameGenerator, List<Pair<LocalTime, LocalTime>> timeslotRangeList,
                             int rotationLength, int rotationEmployeeListSize,
                             BiFunction<Integer, Integer, Integer> rotationEmployeeIndexCalculator) {
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

    private final GeneratorType hospitalGeneratorType = new GeneratorType(
            "Hospital",
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
                    Pair.of(LocalTime.of(6, 0), LocalTime.of(14, 0)),
                    Pair.of(LocalTime.of(9, 0), LocalTime.of(17, 0)),
                    Pair.of(LocalTime.of(14, 0), LocalTime.of(22, 0)),
                    Pair.of(LocalTime.of(22, 0), LocalTime.of(6, 0))),
            // Morning:   A A A A A D D B B B B B D D C C C C C D D
            // Day:       F F B B B F F F F C C C F F F F A A A F F
            // Afternoon: D D D E E E E D D D E E E E D D D E E E E
            // Night:     E C C C C C C E A A A A A A E B B B B B B
            21, 6, (startDayOffset, timeslotRangesIndex) -> {
        switch (timeslotRangesIndex) {
            case 0:
                return startDayOffset % 7 >= 5 ? 3 : startDayOffset / 7;
            case 1:
                return (startDayOffset + 2) % 7 < 4 ? 5 : (startDayOffset - 16 + 21) % 21 / 7;
            case 2:
                return startDayOffset % 7 < 3 ? 3 : 4;
            case 3:
                return startDayOffset % 7 < 1 ? 4 : (startDayOffset - 8 + 21) % 21 / 7;
            default:
                throw new IllegalStateException("Impossible state for timeslotRangesIndex (" + timeslotRangesIndex
                                                        + ").");
        }
    });
    private final GeneratorType factoryAssemblyGeneratorType = new GeneratorType(
            "Factory assembly",
            new StringDataGenerator()
                    .addPart(
                            "Mechanical",
                            "Electrical",
                            "Safety",
                            "Transportation",
                            "Operational",
                            "Physics",
                            "Monitoring",
                            "ICT")
                    .addPart(
                            "bachelor",
                            "engineer",
                            "instructor",
                            "coordinator",
                            "manager",
                            "expert",
                            "inspector",
                            "analyst"),
            StringDataGenerator.buildAssemblyLineNames(),
            Arrays.asList(
                    Pair.of(LocalTime.of(6, 0), LocalTime.of(14, 0)),
                    Pair.of(LocalTime.of(14, 0), LocalTime.of(22, 0)),
                    Pair.of(LocalTime.of(22, 0), LocalTime.of(6, 0))),
            // Morning:   A A A A A A A B B B B B B B C C C C C C C D D D D D D D
            // Afternoon: C C D D D D D D D A A A A A A A B B B B B B B C C C C C
            // Night:     B B B B C C C C C C C D D D D D D D A A A A A A A B B B
            28, 4, (startDayOffset, timeslotRangesIndex) -> (startDayOffset - (9 * timeslotRangesIndex) + 28) % 28 / 7);
    private final GeneratorType guardSecurityGeneratorType = new GeneratorType(
            "Guard security",
            new StringDataGenerator()
                    .addPart(
                            "Martial art",
                            "Armed",
                            "Surveillance",
                            "Technical",
                            "Computer")
                    .addPart(
                            "basic",
                            "advanced",
                            "expert",
                            "master",
                            "novice"),
            new StringDataGenerator()
                    .addPart("Airport",
                             "Harbor",
                             "Bank",
                             "Office",
                             "Warehouse",
                             "Store",
                             "Factory",
                             "Station",
                             "Museum",
                             "Mansion",
                             "Monument",
                             "City hall",
                             "Prison",
                             "Mine",
                             "Palace")
                    .addPart("north gate",
                             "south gate",
                             "east gate",
                             "west gate",
                             "roof",
                             "cellar",
                             "north west gate",
                             "north east gate",
                             "south west gate",
                             "south east gate",
                             "main door",
                             "back door",
                             "side door",
                             "balcony",
                             "patio")
                    .addPart("Alpha",
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
                    Pair.of(LocalTime.of(7, 0), LocalTime.of(19, 0)),
                    Pair.of(LocalTime.of(19, 0), LocalTime.of(7, 0))),
            // Day:   A A A B B B B C C C A A A A B B B C C C C
            // Night: C C C A A A A B B B C C C C A A A B B B B
            21, 3, (startDayOffset, timeslotRangesIndex) -> {
        int offset = timeslotRangesIndex == 0 ? startDayOffset : (startDayOffset + 7) % 21;
        return offset < 3 ? 0 : offset < 7 ? 1 : offset < 10 ? 2 : offset < 14 ? 0 : offset < 17 ? 1 :
                offset < 21 ? 2 : -1;
    });
    private final GeneratorType callCenterGeneratorType = new GeneratorType(
            "Call center",
            new StringDataGenerator()
                    .addPart(
                            "English",
                            "Spanish",
                            "French",
                            "German",
                            "Japanese",
                            "Chinese",
                            "Dutch",
                            "Portuguese",
                            "Italian"),
            new StringDataGenerator()
                    .addPart("Business loans",
                             "Checking and savings accounts",
                             "Debit and credit cards",
                             "Insurances",
                             "Merchant services",
                             "Cash management",
                             "Tax management",
                             "Wealth management",
                             "Mortgages",
                             "Personal loans",
                             "Online payment"),
            Arrays.asList(
                    Pair.of(LocalTime.of(7, 0), LocalTime.of(16, 0)),
                    Pair.of(LocalTime.of(11, 0), LocalTime.of(20, 0))),
            // Morning:   B A A A A A B
            // Afternoon: C C B B C C C
            7, 3, (startDayOffset, timeslotRangesIndex) -> {
        return timeslotRangesIndex == 0
                ? startDayOffset < 1 ? 1 : startDayOffset < 6 ? 0 : startDayOffset < 7 ? 1 : -1
                : startDayOffset < 2 ? 2 : startDayOffset < 4 ? 1 : startDayOffset < 7 ? 2 : -1;
    });

    private Random random;

    // TODO: Make fields 'final' once Benchmark is added
    private SkillRepository skillRepository;
    private SpotRepository spotRepository;
    private ContractRepository contractRepository;
    private EmployeeRepository employeeRepository;
    private RosterStateRepository rosterStateRepository;
    private ShiftTemplateRepository shiftTemplateRepository;

    // TODO: Add ShiftRepository once Shift CRUD is implemented

    // TODO: Add EmployeeAvailabilityRepository once EmployeeAvailability CRUD is implemented

    // TODO: Add RosterParametrizationRepository once RosterParametrization CRUD is implemented

    // TODO: Add TenantRepository once Tenant CRUD is implemented

    @SuppressWarnings("unused")
    public RosterGenerator() {
    }

    /**
     * For benchmark only
     * @param skillRepository never null
     * @param spotRepository never null
     * @param contractRepository never null
     * @param employeeRepository never null
     * @param rosterStateRepository never null
     * @param shiftTemplateRepository never null
     */
    public RosterGenerator(SkillRepository skillRepository, SpotRepository spotRepository,
                           ContractRepository contractRepository, EmployeeRepository employeeRepository,
                           RosterStateRepository rosterStateRepository,
                           ShiftTemplateRepository shiftTemplateRepository) {
        this.skillRepository = skillRepository;
        this.spotRepository = spotRepository;
        this.contractRepository = contractRepository;
        this.employeeRepository = employeeRepository;
        this.rosterStateRepository = rosterStateRepository;
        this.shiftTemplateRepository = shiftTemplateRepository;
        random = new Random(37);
    }

    @PostConstruct
    public void setUpGeneratedData() {
        //TODO: Check if Tenant list is empty before generating data. Application crashes from trying to recreate
        // existing entities
        ZoneId zoneId = SystemPropertiesRetriever.determineZoneId();
        setUpGeneratedData(zoneId);
    }

    public void setUpGeneratedData(ZoneId zoneId) {
        random = new Random(37);
        tenantNameGenerator.predictMaximumSizeAndReset(12);
        generateRoster(10, 7, hospitalGeneratorType, zoneId);
        generateRoster(10, 7, factoryAssemblyGeneratorType, zoneId);
        generateRoster(10, 7, guardSecurityGeneratorType, zoneId);
        generateRoster(10, 7, callCenterGeneratorType, zoneId);
        generateRoster(10, 7 * 4, factoryAssemblyGeneratorType, zoneId);
        generateRoster(20, 7 * 4, factoryAssemblyGeneratorType, zoneId);
        generateRoster(40, 7 * 2, factoryAssemblyGeneratorType, zoneId);
        generateRoster(80, 7 * 4, factoryAssemblyGeneratorType, zoneId);
        generateRoster(10, 7 * 4, factoryAssemblyGeneratorType, zoneId);
        generateRoster(20, 7 * 4, factoryAssemblyGeneratorType, zoneId);
        generateRoster(40, 7 * 2, factoryAssemblyGeneratorType, zoneId);
        generateRoster(80, 7 * 4, factoryAssemblyGeneratorType, zoneId);
    }

    public Roster generateRoster(int spotListSize, int lengthInDays) {
        ZoneId zoneId = SystemPropertiesRetriever.determineZoneId();
        return generateRoster(spotListSize, lengthInDays, factoryAssemblyGeneratorType, zoneId);
    }

    @Transactional
    public Roster generateRoster(int spotListSize,
                                 int lengthInDays,
                                 GeneratorType generatorType,
                                 ZoneId zoneId) {
        int maxShiftSizePerDay = generatorType.timeslotRangeList.size() + EXTRA_SHIFT_THRESHOLDS.length;
        // The average employee works 5 days out of 7
        int employeeListSize = spotListSize * maxShiftSizePerDay * 7 / 5;
        int skillListSize = (spotListSize + 4) / 5;

        Tenant tenant = createTenant(generatorType, employeeListSize);

        // TODO: Remove artificial setting of tenant fields once Tenant CRUD is implemented
        tenant.setId(1);
        tenant.setVersion(0L);

        Integer tenantId = tenant.getId();
        RosterParametrization rosterParametrization = createTenantConfiguration(generatorType, tenantId, zoneId);
        RosterState rosterState = createRosterState(generatorType, tenant, zoneId, lengthInDays);

        List<Skill> skillList = createSkillList(generatorType, tenantId, skillListSize);
        List<Spot> spotList = createSpotList(generatorType, tenantId, spotListSize, skillList);
        List<Contract> contractList = createContractList(tenantId);
        List<Employee> employeeList = createEmployeeList(generatorType, tenantId, employeeListSize, contractList,
                                                         skillList);
        List<ShiftTemplate> shiftTemplateList = createShiftTemplateList(generatorType, tenantId, rosterState, spotList,
                                                                        employeeList);
        List<Shift> shiftList = createShiftList(generatorType, tenantId, rosterParametrization, rosterState, spotList,
                                                shiftTemplateList);
        List<EmployeeAvailability> employeeAvailabilityList = createEmployeeAvailabilityList(
                generatorType, tenantId, rosterParametrization, rosterState, employeeList, shiftList);

        return new Roster((long) tenantId, tenantId, skillList, spotList, employeeList, employeeAvailabilityList,
                          rosterParametrization, rosterState, shiftList);
    }

    private Tenant createTenant(GeneratorType generatorType, int employeeListSize) {
        String tenantName = generatorType.tenantNamePrefix + " " + tenantNameGenerator.generateNextValue() + " ("
                + employeeListSize + " employees)";
        Tenant tenant = new Tenant(tenantName);

        //TODO: Persist tenant in TenantRepository once CRUD is implemented
        //return tenantRepository.save(tenant);
        return tenant;
    }

    private RosterParametrization createTenantConfiguration(GeneratorType generatorType, Integer tenantId,
                                                            ZoneId zoneId) {
        RosterParametrization rosterParametrization = new RosterParametrization();
        rosterParametrization.setTenantId(tenantId);

        //TODO: Persist rosterParametrization in RosterParametrization once CRUD is implemented
        //return rosterParametrizationRepository.save(rosterParametrization);
        return rosterParametrization;
    }

    private RosterState createRosterState(GeneratorType generatorType, Tenant tenant, ZoneId zoneId, int lengthInDays) {
        RosterState rosterState = new RosterState();
        rosterState.setTenantId(tenant.getId());
        int publishNotice = 14;
        rosterState.setPublishNotice(publishNotice);
        LocalDate firstDraftDate = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .plusDays(publishNotice);
        rosterState.setFirstDraftDate(firstDraftDate);
        // publishLength is read-only and set to 7 days
        //rosterState.setPublishLength(7);
        rosterState.setDraftLength(14);
        rosterState.setUnplannedRotationOffset(0);
        rosterState.setRotationLength(generatorType.rotationLength);
        rosterState.setLastHistoricDate(LocalDate.now().minusDays(1));
        rosterState.setTimeZone(zoneId);
        rosterState.setTenant(tenant);
        return rosterStateRepository.save(rosterState);
    }

    private List<Skill> createSkillList(GeneratorType generatorType, Integer tenantId, int size) {
        List<Skill> skillList = new ArrayList<>(size);
        generatorType.skillNameGenerator.predictMaximumSizeAndReset(size);
        for (int i = 0; i < size; i++) {
            String name = generatorType.skillNameGenerator.generateNextValue();
            Skill skill = new Skill(tenantId, name);
            skillList.add(skillRepository.save(skill));
        }
        return skillList;
    }

    private List<Spot> createSpotList(GeneratorType generatorType, Integer tenantId, int size, List<Skill> skillList) {
        List<Spot> spotList = new ArrayList<>(size);
        generatorType.spotNameGenerator.predictMaximumSizeAndReset(size);
        for (int i = 0; i < size; i++) {
            String name = generatorType.spotNameGenerator.generateNextValue();
            Set<Skill> requiredSkillSet = new HashSet<>(extractRandomSubList(skillList, 0.5, 0.9, 1.0));
            Spot spot = new Spot(tenantId, name, requiredSkillSet);
            spotList.add(spotRepository.save(spot));
        }
        return spotList;
    }

    private List<Contract> createContractList(Integer tenantId) {
        List<Contract> contractList = new ArrayList<>(3);
        Contract contract = new Contract(tenantId, "Part Time Contract");
        contractList.add(contractRepository.save(contract));

        contract = new Contract(tenantId, "Max 16 Hours Per Week Contract", null, 16 * 60, null, null);
        contractList.add(contractRepository.save(contract));

        contract = new Contract(tenantId, "Max 16 Hours Per Week, 32 Hours Per Month Contract", null, 16 * 60, 32 * 60,
                                null);
        contractList.add(contractRepository.save(contract));

        return contractList;
    }

    private List<Employee> createEmployeeList(GeneratorType generatorType, Integer tenantId, int size,
                                              List<Contract> contractList, List<Skill> generalSkillList) {
        List<Employee> employeeList = new ArrayList<>(size);
        employeeNameGenerator.predictMaximumSizeAndReset(size);
        for (int i = 0; i < size; i++) {
            String name = employeeNameGenerator.generateNextValue();
            HashSet<Skill> skillProficiencySet = new HashSet<>(extractRandomSubList(generalSkillList,
                                                                                    0.1, 0.3, 0.5, 0.7, 0.9, 1.0));
            Employee employee = new Employee(tenantId, name,
                                             contractList.get(generateRandomIntFromThresholds(0.7, 0.5)),
                                             skillProficiencySet);
            employeeList.add(employeeRepository.save(employee));
        }
        return employeeList;
    }

    private List<ShiftTemplate> createShiftTemplateList(GeneratorType generatorType,
                                                        Integer tenantId,
                                                        RosterState rosterState,
                                                        List<Spot> spotList,
                                                        List<Employee> employeeList) {
        int rotationLength = rosterState.getRotationLength();
        List<ShiftTemplate> shiftTemplateList = new ArrayList<>(spotList.size() * rotationLength *
                                                                        generatorType.timeslotRangeList.size());
        List<Employee> remainingEmployeeList = employeeList.stream()
                .filter((e) -> e.getContract().getMaximumMinutesPerWeek() == null)
                .collect(Collectors.toCollection(ArrayList::new));
        for (Spot spot : spotList) {
            List<Employee> rotationEmployeeList = remainingEmployeeList.stream()
                    .filter(employee -> employee.getSkillProficiencySet().containsAll(spot.getRequiredSkillSet()))
                    .limit(generatorType.rotationEmployeeListSize).collect(toList());
            remainingEmployeeList.removeAll(rotationEmployeeList);
            // For every day in the rotation (independent of publishLength and draftLength)
            for (int startDayOffset = 0; startDayOffset < rotationLength; startDayOffset++) {
                // Fill the offset day with shift templates
                for (int timeslotRangesIndex = 0; timeslotRangesIndex < generatorType.timeslotRangeList.size();
                        timeslotRangesIndex++) {
                    Pair<LocalTime, LocalTime> timeslotRange = generatorType.timeslotRangeList.get(timeslotRangesIndex);
                    LocalTime startTime = timeslotRange.getLeft();
                    LocalTime endTime = timeslotRange.getRight();
                    int endDayOffset = startDayOffset;
                    if (endTime.compareTo(startTime) < 0) {
                        endDayOffset = (startDayOffset + 1) % rotationLength;
                    }
                    int rotationEmployeeIndex = generatorType.rotationEmployeeIndexCalculator
                            .apply(startDayOffset, timeslotRangesIndex);
                    if (rotationEmployeeIndex < 0 || rotationEmployeeIndex >= generatorType.rotationEmployeeListSize) {
                        throw new IllegalStateException(
                                "The rotationEmployeeIndexCalculator for generatorType (" +
                                        generatorType.tenantNamePrefix + ") returns an invalid rotationEmployeeIndex ("
                                        + rotationEmployeeIndex + ") for startDayOffset (" + startDayOffset
                                        + ") and timeslotRangesIndex (" + timeslotRangesIndex + ").");
                    }
                    // There might be less employees than we need (overconstrained planning)
                    Employee rotationEmployee = rotationEmployeeIndex >= rotationEmployeeList.size() ? null :
                            rotationEmployeeList.get(rotationEmployeeIndex);
                    ShiftTemplate shiftTemplate = new ShiftTemplate(tenantId, spot, startDayOffset, startTime,
                                                                    endDayOffset, endTime, rotationEmployee);
                    shiftTemplateList.add(shiftTemplateRepository.save(shiftTemplate));
                }
            }
        }
        return shiftTemplateList;
    }

    private List<Shift> createShiftList(GeneratorType generatorType,
                                        Integer tenantId,
                                        RosterParametrization rosterParametrization,
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
                List<ShiftTemplate> subShiftTemplateList = dayOffsetAndSpotToShiftTemplateListMap.get(Pair.of(dayOffset,
                                                                                                              spot));
                for (ShiftTemplate shiftTemplate : subShiftTemplateList) {
                    boolean defaultToRotationEmployee = date.compareTo(firstDraftDate) < 0;
                    Shift shift = shiftTemplate.createShiftOnDate(date, rosterState.getRotationLength(),
                                                                  zoneId, defaultToRotationEmployee);
                    // TODO: Persist shift in shiftRepository once Shift CRUD is implemented
                    //shiftList.add(shiftRepository.save(shift));
                    shiftList.add(shift);
                }
                if (date.compareTo(firstDraftDate) >= 0) {
                    int extraShiftCount = generateRandomIntFromThresholds(EXTRA_SHIFT_THRESHOLDS);
                    for (int i = 0; i < extraShiftCount; i++) {
                        ShiftTemplate shiftTemplate = extractRandomElement(subShiftTemplateList);
                        Shift shift = shiftTemplate.createShiftOnDate(date, rosterState.getRotationLength(),
                                                                      zoneId, false);
                        // TODO: Persist shift in shiftRepository once Shift CRUD is implemented
                        //shiftList.add(shiftRepository.save(shift));
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

    private List<EmployeeAvailability> createEmployeeAvailabilityList(GeneratorType generatorType,
                                                                      Integer tenantId,
                                                                      RosterParametrization rosterParametrization,
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
            List<Shift> dayShiftList = startDayToShiftListMap.get(date);
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
                    // TODO: Persist employeeAvailability in employeeAvailabilityRepository once employeeAvailability
                    //  CRUD is implemented
                    //employeeAvailabilityList.add(employeeAvailabilityRepository.save(employeeAvailability));
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
