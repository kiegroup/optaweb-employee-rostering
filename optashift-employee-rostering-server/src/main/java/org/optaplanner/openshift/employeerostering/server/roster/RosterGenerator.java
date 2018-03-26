/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.server.roster;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.time.zone.ZoneRules;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.optaplanner.openshift.employeerostering.server.common.generator.StringDataGenerator;
import org.optaplanner.openshift.employeerostering.server.rotation.ShiftGenerator;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaplanner.openshift.employeerostering.shared.roster.Roster;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterState;
import org.optaplanner.openshift.employeerostering.shared.rotation.ShiftTemplate;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantConfiguration;

import static java.util.stream.Collectors.groupingBy;

@Singleton
@Startup
public class RosterGenerator {

    private static class GeneratorType {
        private final String tenantNamePrefix;
        private final StringDataGenerator skillNameGenerator;
        private final StringDataGenerator spotNameGenerator;
        private final int[][] timeslotRanges; // Start and end in minutes

        public GeneratorType(String tenantNamePrefix, StringDataGenerator skillNameGenerator, StringDataGenerator spotNameGenerator, int[][] timeslotRanges) {
            this.tenantNamePrefix = tenantNamePrefix;
            this.skillNameGenerator = skillNameGenerator;
            this.spotNameGenerator = spotNameGenerator;
            this.timeslotRanges = timeslotRanges;
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
            new StringDataGenerator()
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
                            "emergency",
                            "maternity",
                            "anaesthetics",
                            "cardiology",
                            "critical care",
                            "gastroenterology",
                            "haematology",
                            "neurology",
                            "oncology",
                            "ophthalmology",
                            "orthopaedics",
                            "physiotherapy",
                            "radiotherapy",
                            "urology",
                            "ear nose throat")
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
            new int[][]{
                    {6 * 60, 14 * 60},
                    {9 * 60, 17 * 60},
                    {14 * 60, 22 * 60},
                    {22 * 60, 6 * 60}
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
            new int[][]{
                    {6 * 60, 14 * 60},
                    {14 * 60, 22 * 60},
                    {22 * 60, 6 * 60}
            });
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
                            "Basic",
                            "Advanced",
                            "Expert",
                            "Master",
                            "Novice"),
            new StringDataGenerator()
                    .addPart(false, 0,
                            "North gate",
                            "South gate",
                            "East gate",
                            "West gate",
                            "Roof",
                            "Cellar",
                            "North west gate",
                            "North east gate",
                            "South west gate",
                            "South east gate",
                            "Main door",
                            "Back door",
                            "Side door",
                            "Balcony",
                            "Patio")
                    .addPart(true, 1,
                            "Airport",
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
            new int[][]{
                    {7 * 60, 19 * 60},
                    {19 * 60, 7 * 60}
            });

    private Random random = new Random(37);

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unused")
    public RosterGenerator() {
    }

    /**
     * For benchmark only
     * @param entityManager never null
     */
    public RosterGenerator(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @PostConstruct
    public void setUpGeneratedData() {
        tenantNameGenerator.predictMaximumSizeAndReset(12);
        generateRoster(10, 7, hospitalGeneratorType);
        generateRoster(10, 7, factoryAssemblyGeneratorType);
        generateRoster(10, 7, guardSecurityGeneratorType);
        generateRoster(10, 7 * 4, factoryAssemblyGeneratorType);
        generateRoster(20, 7 * 4, factoryAssemblyGeneratorType);
        generateRoster(40, 7 * 2, factoryAssemblyGeneratorType);
        generateRoster(80, 7 * 4, factoryAssemblyGeneratorType);
        generateRoster(10, 7 * 4, factoryAssemblyGeneratorType);
        generateRoster(20, 7 * 4, factoryAssemblyGeneratorType);
        generateRoster(40, 7 * 2, factoryAssemblyGeneratorType);
        generateRoster(80, 7 * 4, factoryAssemblyGeneratorType);
    }

    public Roster generateRoster(int spotListSize, int lengthInDays) {
        return generateRoster(spotListSize, lengthInDays, factoryAssemblyGeneratorType);
    }

    @Transactional
    public Roster generateRoster(int spotListSize,
            int lengthInDays, GeneratorType generatorType) {
        int employeeListSize = spotListSize * 7 / 2;
        int skillListSize = (spotListSize + 4) / 5;

        Tenant tenant = createTenant(generatorType, employeeListSize);
        int tenantId = tenant.getId();
        TenantConfiguration tenantConfiguration = createTenantConfiguration(generatorType, tenantId);
        RosterState rosterState = createRosterState(generatorType, tenantId, lengthInDays);

        List<Skill> skillList = createSkillList(generatorType, tenantId, skillListSize);
        List<Spot> spotList = createSpotList(generatorType, tenantId, spotListSize, skillList);

        List<Employee> employeeList = createEmployeeList(generatorType, tenantId, employeeListSize, skillList);
        List<ShiftTemplate> shiftTemplateList = createShiftTemplateList(generatorType, tenantId, rosterState, spotList);
        List<Shift> shiftList = createShiftList(generatorType, tenantId, tenantConfiguration, rosterState, shiftTemplateList);
        List<EmployeeAvailability> employeeAvailabilityList = createEmployeeAvailabilityList(
                generatorType, tenantId, tenantConfiguration, rosterState, employeeList, shiftList);

        return new Roster((long) tenantId, tenantId,
                skillList, spotList, employeeList, employeeAvailabilityList,
                tenantConfiguration, rosterState, shiftList);
    }

    private Tenant createTenant(GeneratorType generatorType, int employeeListSize) {
        String tenantName = generatorType.tenantNamePrefix + " " + tenantNameGenerator.generateNextValue()
                + " (" + employeeListSize + " employees)";
        Tenant tenant = new Tenant(tenantName);
        entityManager.persist(tenant);
        return tenant;
    }

    private TenantConfiguration createTenantConfiguration(GeneratorType generatorType, Integer tenantId) {
        TenantConfiguration tenantConfiguration = new TenantConfiguration();
        tenantConfiguration.setTenantId(tenantId);
        entityManager.persist(tenantConfiguration);
        return tenantConfiguration;
    }

    private RosterState createRosterState(GeneratorType generatorType, Integer tenantId, int lengthInDays) {
        RosterState rosterState = new RosterState();
        rosterState.setTenantId(tenantId);
        int publishNotice = 14;
        rosterState.setPublishNotice(publishNotice);
        LocalDate firstDraftDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY))
                .plusDays(publishNotice);
        rosterState.setFirstDraftDate(firstDraftDate);
        rosterState.setPublishLength(7);
        rosterState.setDraftLength(14);
        rosterState.setUnplannedRotationOffset(0);
        rosterState.setRotationLength(7);
        rosterState.setLastHistoricDate(LocalDate.now().minusDays(1));
        entityManager.persist(rosterState);
        return rosterState;
    }

    private List<Skill> createSkillList(GeneratorType generatorType, Integer tenantId, int size) {
        List<Skill> skillList = new ArrayList<>(size);
        generatorType.skillNameGenerator.predictMaximumSizeAndReset(size);
        for (int i = 0; i < size; i++) {
            String name = generatorType.skillNameGenerator.generateNextValue();
            Skill skill = new Skill(tenantId, name);
            entityManager.persist(skill);
            skillList.add(skill);
        }
        return skillList;
    }

    private List<Spot> createSpotList(GeneratorType generatorType, Integer tenantId, int size, List<Skill> skillList) {
        List<Spot> spotList = new ArrayList<>(size);
        generatorType.spotNameGenerator.predictMaximumSizeAndReset(size);
        for (int i = 0; i < size; i++) {
            String name = generatorType.spotNameGenerator.generateNextValue();
            Set<Skill> requiredSkillSet = new HashSet<>(
                    extractRandomSubListOfSize(skillList, random.nextInt(skillList.size())));
            Spot spot = new Spot(tenantId, name, requiredSkillSet);
            entityManager.persist(spot);
            spotList.add(spot);
        }
        return spotList;
    }

    private List<Employee> createEmployeeList(GeneratorType generatorType, Integer tenantId, int size, List<Skill> generalSkillList) {
        List<Employee> employeeList = new ArrayList<>(size);
        employeeNameGenerator.predictMaximumSizeAndReset(size);
        for (int i = 0; i < size; i++) {
            String name = employeeNameGenerator.generateNextValue();
            Employee employee = new Employee(tenantId, name);
            employee.setSkillProficiencySet(new HashSet<>(extractRandomSubList(generalSkillList, 1.0)));
            entityManager.persist(employee);
            employeeList.add(employee);
        }
        return employeeList;
    }

    private List<ShiftTemplate> createShiftTemplateList(GeneratorType generatorType, Integer tenantId, RosterState rosterState, List<Spot> spotList) {
        int rotationLength = rosterState.getRotationLength();
        List<ShiftTemplate> shiftTemplateList = new ArrayList<>(spotList.size() * rotationLength
                * generatorType.timeslotRanges.length);
        for (Spot spot : spotList) {
            // For every day in the rotation (independent of publishLength and draftLength)
            for (int startDayOffset = 0; startDayOffset < rotationLength; startDayOffset++) {
                // Fill the offset day with shift templates
                for (int[] timeslotRange : generatorType.timeslotRanges) {
                    int startMinute = timeslotRange[0];
                    int endMinute = timeslotRange[1];
                    LocalTime startTime = LocalTime.of(startMinute / 60, startMinute % 60);
                    int endDayOffset = startDayOffset;
                    if (endMinute < startMinute) {
                        endDayOffset = (startDayOffset + 1) % rotationLength;
                    }
                    LocalTime endTime = LocalTime.of(endMinute / 60, endMinute % 60);
                    Employee rotationEmployee = null;
                    ShiftTemplate shiftTemplate = new ShiftTemplate(tenantId, spot, startDayOffset, startTime, endDayOffset, endTime, rotationEmployee);
                    entityManager.persist(shiftTemplate);
                    shiftTemplateList.add(shiftTemplate);
                }
            }
        }
        return shiftTemplateList;
    }

    private List<Shift> createShiftList(GeneratorType generatorType, Integer tenantId,
            TenantConfiguration tenantConfiguration, RosterState rosterState, List<ShiftTemplate> shiftTemplateList) {
        int rotationLength = rosterState.getRotationLength();
        LocalDate date = rosterState.getFirstDraftDate().minusDays(rosterState.getPublishNotice());
        LocalDate lastDraftDate = rosterState.getLastDraftDate();

        List<Shift> shiftList = new ArrayList<>();
        Map<Integer, List<ShiftTemplate>> startDayOffsetToShiftTemplateListMap = shiftTemplateList.stream()
                .collect(groupingBy(ShiftTemplate::getStartDayOffset));
        int dayOffset = 0;
        while (date.compareTo(lastDraftDate) <= 0) {
            List<ShiftTemplate> dayShiftTemplateList = startDayOffsetToShiftTemplateListMap.get(dayOffset);
            for (ShiftTemplate shiftTemplate : dayShiftTemplateList) {
                Shift shift = shiftTemplate.createShiftOnDate(date, tenantConfiguration.getTimeZone());
                entityManager.persist(shift);
                shiftList.add(shift);
            }
            date = date.plusDays(1);
            dayOffset = (dayOffset + 1) % rotationLength;
        }
        rosterState.setUnplannedRotationOffset(dayOffset);
        return shiftList;
    }

    private List<EmployeeAvailability> createEmployeeAvailabilityList(GeneratorType generatorType, Integer tenantId,
            TenantConfiguration tenantConfiguration, RosterState rosterState, List<Employee> employeeList, List<Shift> shiftList) {
        ZoneRules zoneRules = tenantConfiguration.getTimeZone().getRules();
        LocalDate date = rosterState.getFirstDraftDate();
        LocalDate lastDraftDate = rosterState.getLastDraftDate();
        List<EmployeeAvailability> employeeAvailabilityList = new ArrayList<>();
        Map<LocalDate, List<Shift>> startDayToShiftListMap = shiftList.stream()
                .collect(groupingBy(shift -> shift.getStartDateTime().toLocalDate()));

        while (date.compareTo(lastDraftDate) <= 0) {
            List<Shift> dayShiftList = startDayToShiftListMap.get(date);
            List<Employee> availableEmployeeList = new ArrayList<>(employeeList);
            int stateCount = (employeeList.size() - dayShiftList.size()) / 5;
            for (EmployeeAvailabilityState state : EmployeeAvailabilityState.values()) {
                for (int i = 0; i < stateCount; i++) {
                    Employee employee = availableEmployeeList.remove(random.nextInt(availableEmployeeList.size()));
                    // TODO Can this and ShiftTemplate.createShiftOnDate() be simplified and be DST spring/fall compatible?
                    EmployeeAvailability employeeAvailability = new EmployeeAvailability(tenantId, employee, date,
                            OffsetTime.of(LocalTime.MIN, zoneRules.getOffset(date.atStartOfDay())),
                            OffsetTime.of(LocalTime.MAX, zoneRules.getOffset(date.atTime(LocalTime.MAX)))); // TODO set to 00:00 next day instead
                    employeeAvailability.setState(state);
                    entityManager.persist(employeeAvailability);
                    employeeAvailabilityList.add(employeeAvailability);
                }
            }
            date = date.plusDays(1);
        }
        return employeeAvailabilityList;
    }

    private <E> List<E> extractRandomSubList(List<E> list, double maxRelativeSize) {
        int size = random.nextInt((int) (list.size() * maxRelativeSize)) + 1;
        return extractRandomSubListOfSize(list, size);
    }

    private <E> List<E> extractRandomSubListOfSize(List<E> list, int size) {
        List<E> subList = new ArrayList<>(list);
        Collections.shuffle(subList, random);
        // Remove elements not in the sublist (so it can be garbage collected)
        subList.subList(size, subList.size()).clear();
        return subList;
    }

}
