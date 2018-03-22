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
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
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




        List<EmployeeAvailability> employeeAvailabilityList = new ArrayList<>();
//        List<EmployeeAvailability> employeeAvailabilityList = createEmployeeAvailabilityList(generatorType, tenantId,
//                tenantConfiguration, employeeList, parserOutput.getNewRosterState().getLastHistoricDate(),
//                parserOutput
//                        .getNewRosterState().getLastDraftDate());

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

        List<Shift> shiftList = new ArrayList<>();
        LocalDate lastDraftDate = rosterState.getLastDraftDate();

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




    private List<EmployeeAvailability> createEmployeeAvailabilityList(GeneratorType generatorType, int tenantId,
            TenantConfiguration config, List<Employee> employeeList, LocalDate fromDate, LocalDate toDate) {
        List<LocalDate> datesBetween = new ArrayList<>();
        for (LocalDate currDate = fromDate; currDate.isBefore(toDate); currDate = currDate.plusDays(1)) {
            datesBetween.add(currDate);
        }
        // TODO: Use TimeSlotPattern to make this more realistic
        for (LocalDate date : datesBetween) {
            List<Employee> employeesListCopy = new ArrayList<>(employeeList);
            List<Employee> unavailableEmployees = new ArrayList<>(extractRandomSubList(employeesListCopy, 0.3));
            employeesListCopy.removeAll(unavailableEmployees);
            List<Employee> undesiredEmployees = new ArrayList<>(extractRandomSubList(employeesListCopy, 0.3));
            employeesListCopy.removeAll(undesiredEmployees);
            List<Employee> desiredEmployees = new ArrayList<>(extractRandomSubList(employeesListCopy, 0.3));
            employeesListCopy.removeAll(desiredEmployees);

            unavailableEmployees.forEach(e -> createEmployeeAvailability(tenantId, config, e, date,
                    EmployeeAvailabilityState.UNAVAILABLE));
            undesiredEmployees.forEach(e -> createEmployeeAvailability(tenantId, config, e, date,
                    EmployeeAvailabilityState.UNDESIRED));
            desiredEmployees.forEach(e -> createEmployeeAvailability(tenantId, config, e, date,
                    EmployeeAvailabilityState.DESIRED));
        }
        return entityManager.createNamedQuery("EmployeeAvailability.findAll", EmployeeAvailability.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
    }

    private void createEmployeeAvailability(int tenantId, TenantConfiguration config, Employee employee, LocalDate date,
            EmployeeAvailabilityState state) {
        EmployeeAvailability availability = new EmployeeAvailability(tenantId, employee, date, OffsetTime.of(
                LocalTime.MIN,
                config.getTimeZone().getRules().getOffset(date.atStartOfDay())), OffsetTime.of(LocalTime.MAX,
                        config
                                .getTimeZone().getRules().getOffset(date.atTime(LocalTime.MAX))));
        availability.setState(state);
        entityManager.persist(availability);
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

    private class SpotSettings {

        private final Spot spot;
        private final TimeSlotPattern timeSlotPattern;
        private final int numberOfShiftsNormal;
        private final int numberOfShiftsNight;
        private final int numberOfShiftsWeekend;
        private final int numberOfShiftsWeekendNight;

        public SpotSettings(Spot spot) {
            this.spot = spot;
            timeSlotPattern = TimeSlotPattern.getRandomTimeSlotPattern(random);
            numberOfShiftsNormal = random.nextInt(3) + 1;
            numberOfShiftsNight = random.nextInt(numberOfShiftsNormal + 1);
            numberOfShiftsWeekend = random.nextInt(numberOfShiftsNormal + 1);
            numberOfShiftsWeekendNight = random.nextInt(Math.min(numberOfShiftsNight + 1, numberOfShiftsWeekend + 1));
        }

        public Spot getSpot() {
            return spot;
        }

        public TimeSlotPattern getTimeSlotPattern() {
            return timeSlotPattern;
        }

        public int getNumberOfShiftsNormal() {
            return numberOfShiftsNormal;
        }

        public int getNumberOfShiftsNight() {
            return numberOfShiftsNight;
        }

        public int getNumberOfShiftsWeekend() {
            return numberOfShiftsWeekend;
        }

        public int getNumberOfShiftsWeekendNight() {
            return numberOfShiftsWeekendNight;
        }

    }

    private static final class TimeSlotInfo {

        private final LocalDateTime startTime;
        private final LocalDateTime endTime;
        private final boolean isNightShift;

        public TimeSlotInfo(LocalDateTime startTime, LocalDateTime endTime, boolean isNightShift) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.isNightShift = isNightShift;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public boolean isNightShift() {
            return isNightShift;
        }
    }

    private static LocalDateTime time(long time) {
        return LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC);
    }

    private static LocalDateTime time(Duration time) {
        return LocalDateTime.ofEpochSecond(time.getSeconds(), 0, ZoneOffset.UTC);
    }

    private static LocalDateTime day(int day) {
        return time(Duration.ofDays(day));
    }

    private static LocalDateTime hour(int hour) {
        return time(Duration.ofHours(hour));
    }

    private static enum TimeSlotPattern {
        //9am-5pm, 4pm-midnight, 10pm-6am (next day)
        DAY_AFTERNOON_NIGHT(Duration.ofDays(1),
                            hour(9), hour(12 + 5), hour(12 + 4), hour(24), null, hour(12 + 10), day(1).plusHours(6)),
        //4pm-midnight
        DAY(Duration.ofDays(1), hour(9), hour(12 + 5)),
        //9am-5pm
        AFTERNOON(Duration.ofDays(1), hour(12 + 4), hour(24)),
        //10pm-6am (next day)
        NIGHT(Duration.ofDays(1), hour(12 + 10), day(1).plusHours(6)),
        LONG_DAY(Duration.ofDays(1), hour(9), hour(24));

        List<TimeSlotInfo> timeSlotInfoList;
        Duration offsetLength;

        // IMPORTANT NOTE: A Null seperate day shifts from night shifts
        // List is in the format dayShifts [null [nightShifts]]
        private TimeSlotPattern(Duration offsetLength, LocalDateTime... dateTimes) {
            timeSlotInfoList = new ArrayList<>(dateTimes.length);
            this.offsetLength = offsetLength;
            boolean isNightShift = false;

            for (int i = 0; i < dateTimes.length; i += 2) {
                if (null == dateTimes[i]) {
                    i++;
                    isNightShift = true;
                } else {
                    timeSlotInfoList.add(new TimeSlotInfo(dateTimes[i], dateTimes[i + 1],
                                                          isNightShift));
                }
            }
        }

        public static TimeSlotPattern getRandomTimeSlotPattern(Random random) {
            return TimeSlotPattern.values()[random.nextInt(TimeSlotPattern.values().length)];
        }

        public List<TimeSlotInfo> getTimeSlotInfoForOffset(int tenant, int offset) {
            return timeSlotInfoList.stream().map((t) -> new TimeSlotInfo(t.getStartTime().plus(offsetLength
                                                                                                       .multipliedBy(offset)),
                                                                         t.getEndTime().plus(offsetLength.multipliedBy(offset)), t.isNightShift()))
                    .collect(Collectors.toList());
        }

        public Duration getDuration() {
            return offsetLength;
        }
    }

    private List<ShiftTemplate> generateShiftTemplate(Integer tenantId,
                                                      List<Spot> spots,
                                                      List<Employee> employees,
                                                      boolean assignDefaultEmployee) {
        List<ShiftTemplate> out = new ArrayList<>();
        List<SpotSettings> spotSettingList = new ArrayList<SpotSettings>();
        spots.forEach((s) -> spotSettingList.add(new SpotSettings(s)));
        LocalDateTime startDate = time(0);
        LocalDateTime endDate = LocalDateTime.ofEpochSecond(Duration.ofDays(7).getSeconds(), 0, ZoneOffset.UTC);

        for (SpotSettings spotInfo : spotSettingList) {
            List<TimeSlotInfo> timeSlotList = getTimeSlotsFor(tenantId, spotInfo, startDate, endDate);
            for (TimeSlotInfo timeSlotInfo : timeSlotList) {
                if (hasAtLeastOneShift(spotInfo, timeSlotInfo)) {
                    List<ShiftTemplate> toAdd = createShiftTemplates(tenantId, spotInfo.getSpot(), getNumberOfShifts(
                            spotInfo, timeSlotInfo), employees, timeSlotInfo.getStartTime(), timeSlotInfo.getEndTime(),
                                                                     assignDefaultEmployee);
                    out.addAll(toAdd);
//                    toAdd.forEach((s) -> entityManager.persist(s));
                }
            }
        }

        return out;
    }

    private Employee getRotationEmployee(List<Employee> employeeList, boolean hasRotationEmployee) {
        if (!hasRotationEmployee) {
            return null;
        } else {
            return employeeList.get(random.nextInt(employeeList.size()));
        }
    }

    private int getNumberOfShifts(SpotSettings spotInfo, TimeSlotInfo timeSlotInfo) {
        if (isWeekend(timeSlotInfo.getStartTime()) && timeSlotInfo.isNightShift()) {
            return spotInfo.getNumberOfShiftsWeekendNight();
        } else if (isWeekend(timeSlotInfo.getStartTime())) {
            return spotInfo.getNumberOfShiftsWeekend();
        } else if (timeSlotInfo.isNightShift()) {
            return spotInfo.getNumberOfShiftsNight();
        } else {
            return spotInfo.getNumberOfShiftsNormal();
        }
    }

    private boolean hasAtLeastOneShift(SpotSettings spotInfo, TimeSlotInfo timeSlotInfo) {
        return getNumberOfShifts(spotInfo, timeSlotInfo) > 0;
    }

    // Assumes generated templates are a week long!
    private boolean isWeekend(LocalDateTime timeSlot) {
        return timeSlot.isAfter(day(5));
    }

    private List<TimeSlotInfo> getTimeSlotsFor(int tenantId,
                                               SpotSettings spotSettings,
                                               LocalDateTime start,
                                               LocalDateTime end) {
        List<TimeSlotInfo> out = new ArrayList<>();
        Duration duration = Duration.ZERO;
        for (int i = 0; duration.compareTo(Duration.between(start, end)) < 0; i++, duration = duration.plus(
                spotSettings.getTimeSlotPattern().getDuration())) {
            out.addAll(spotSettings.getTimeSlotPattern().getTimeSlotInfoForOffset(tenantId, i));
        }
        return out;
    }

    private List<ShiftTemplate> createShiftTemplates(int tenantId,
                                                     Spot spot,
                                                     int shifts,
                                                     List<Employee> employeeList,
                                                     LocalDateTime start,
                                                     LocalDateTime end,
                                                     boolean assignDefaultEmployee) {
        List<ShiftTemplate> out = new ArrayList<>(shifts);
        for (int i = 0; i < shifts; i++) {
            ShiftTemplate shift = new ShiftTemplate();
            shift.setStartDayOffset((int) Duration.between(day(0), start).toDays());
            shift.setStartTime(start.toLocalTime());
            shift.setEndDayOffset((int) Duration.between(day(0), end).toDays());
            shift.setEndTime(end.toLocalTime());
            shift.setTenantId(tenantId);
            shift.setSpot(spot);
            shift.setRotationEmployee(getRotationEmployee(employeeList, assignDefaultEmployee));
            out.add(shift);
        }

        return out;
    }

}
