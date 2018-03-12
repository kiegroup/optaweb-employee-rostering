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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
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

@Singleton
@Startup
public class RosterGenerator {

    private final StringDataGenerator tenantNameGenerator = StringDataGenerator.buildLocationNames();
    private final StringDataGenerator employeeNameGenerator = StringDataGenerator.buildFullNames();
    private final StringDataGenerator spotNameGenerator = StringDataGenerator.buildAssemblyLineNames();

    private final StringDataGenerator skillNameGenerator = new StringDataGenerator()
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
                    "analyst");

    private Random random = new Random(37);

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private ShiftGenerator shiftGenerator;

    @SuppressWarnings("unused")
    public RosterGenerator() {}

    /**
     * For benchmark only
     * @param entityManager never null
     */
    public RosterGenerator(EntityManager entityManager, ShiftGenerator shiftGenerator) {
        this.entityManager = entityManager;
        this.shiftGenerator = shiftGenerator;
    }

    @PostConstruct
    public void setUpGeneratedData() {
        tenantNameGenerator.predictMaximumSizeAndReset(10);
        generateRoster(10, 7, false, false);
        generateRoster(10, 7 * 4, false, false);
        generateRoster(20, 7 * 4, false, true);
        generateRoster(40, 7 * 2, false, false);
        generateRoster(80, 7 * 4, false, true);
        generateRoster(10, 7 * 4, true, false);
        generateRoster(20, 7 * 4, true, true);
        generateRoster(40, 7 * 2, true, false);
        generateRoster(80, 7 * 4, true, true);
    }

    @Transactional
    public Roster generateRoster(int spotListSize,
                                 int lengthInDays,
                                 boolean continuousPlanning,
                                 boolean assignDefaultEmployee) {
        int employeeListSize = spotListSize * 7 / 2;
        int skillListSize = (spotListSize + 4) / 5;
        Integer tenantId = createTenant(spotListSize, employeeListSize, lengthInDays);
        List<Skill> skillList = createSkillList(tenantId, skillListSize);
        List<Spot> spotList = createSpotList(tenantId, spotListSize, skillList);

        List<Employee> employeeList = createEmployeeList(tenantId, employeeListSize, skillList);

        RosterState rosterState = entityManager.createNamedQuery("RosterState.find", RosterState.class)
                .setParameter("tenantId", tenantId)
                .getSingleResult();
        TenantConfiguration tenantConfiguration = entityManager.createNamedQuery("TenantConfiguration.find", TenantConfiguration.class)
                .setParameter("tenantId", tenantId)
                .getSingleResult();

        ShiftFileParser.ParserOut parserOutput = shiftGenerator.parse(tenantId, tenantConfiguration, rosterState, rosterState.getPublishLength() * 2, generateShiftTemplate(tenantId, spotList,
                employeeList,
                assignDefaultEmployee));

        for (Shift shift : parserOutput.getShiftOutputList()) {
            entityManager.persist(shift);
        }
        for (EmployeeAvailability avaliability : parserOutput.getEmployeeAvailabilityOutputList()) {
            entityManager.persist(avaliability);
        }
        entityManager.merge(parserOutput.getNewRosterState());

        List<Shift> shiftList = parserOutput.getShiftOutputList();

        List<EmployeeAvailability> employeeAvailabilityList = createEmployeeAvailabilityList(tenantId, tenant
                .getConfiguration(), employeeList, parserOutput.getNewRosterState().getFirstPublishedDate(),
                parserOutput
                        .getNewRosterState().getFirstDraftDate());

        return new Roster((long) tenantId, tenantId,
                skillList, spotList, employeeList, employeeAvailabilityList,
                tenantConfiguration, rosterState, shiftList);
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
        LONG_DAY(Duration.ofDays(1), hour(9), hour(24)),
        LONG_NIGHT(Duration.ofDays(1), hour(7), day(1).plusHours(7));

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
                    toAdd.forEach((s) -> entityManager.persist(s));
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
            shift.setOffsetStartDay((int) Duration.between(day(0), start).toDays());
            shift.setStartTime(start.toLocalTime());
            shift.setOffsetEndDay((int) Duration.between(day(0), end).toDays());
            shift.setEndTime(end.toLocalTime());
            shift.setTenantId(tenantId);
            shift.setSpot(spot);
            shift.setRotationEmployee(getRotationEmployee(employeeList, assignDefaultEmployee));
            out.add(shift);
        }

        return out;
    }

    private Integer createTenant(int spotListSize, int employeeListSize, int lengthInDays) {
        Tenant tenant = new Tenant(tenantNameGenerator.generateNextValue() + " (" + employeeListSize + " employees, " + spotListSize + "spots)");
        TenantConfiguration configuration = new TenantConfiguration();
        RosterState rosterState = new RosterState();
        rosterState.setDraftLength(0);
        rosterState.setFirstDraftDate(LocalDate.now());
        rosterState.setPublishLength(lengthInDays);
        rosterState.setPublishNotice(lengthInDays);
        rosterState.setRotationLength(7);
        rosterState.setUnplannedOffset(0);
        rosterState.setLastHistoricDate(LocalDate.now());
        entityManager.persist(tenant);
        configuration.setTenantId(tenant.getId());
        rosterState.setTenantId(tenant.getId());
        entityManager.persist(rosterState);
        entityManager.persist(configuration);
        return tenant.getId();
    }

    private List<Skill> createSkillList(Integer tenantId, int size) {
        List<Skill> skillList = new ArrayList<>(size);
        skillNameGenerator.predictMaximumSizeAndReset(size);
        for (int i = 0; i < size; i++) {
            String name = skillNameGenerator.generateNextValue();
            Skill skill = new Skill(tenantId, name);
            entityManager.persist(skill);
            skillList.add(skill);
        }
        return skillList;
    }

    private List<Spot> createSpotList(Integer tenantId, int size, List<Skill> skillList) {
        List<Spot> spotList = new ArrayList<>(size);
        spotNameGenerator.predictMaximumSizeAndReset(size);
        for (int i = 0; i < size; i++) {
            String name = spotNameGenerator.generateNextValue();
            Spot spot = new Spot(tenantId, name, extractRandomSubListOfLength(skillList, random.nextInt(skillList
                    .size())).stream().collect(Collectors.toSet()));
            entityManager.persist(spot);
            spotList.add(spot);
        }
        return spotList;
    }

    private List<Employee> createEmployeeList(Integer tenantId, int size, List<Skill> generalSkillList) {
        List<Employee> employeeList = new ArrayList<>(size);
        employeeNameGenerator.predictMaximumSizeAndReset(size);
        for (int i = 0; i < size; i++) {
            String name = employeeNameGenerator.generateNextValue();
            Employee employee = new Employee(tenantId, name);
            employee.setSkillProficiencySet(
                    extractRandomSubList(generalSkillList, 1.0).stream()
                            .collect(Collectors.toCollection(HashSet::new)));
            entityManager.persist(employee);
            employeeList.add(employee);
        }
        return employeeList;
    }

    private List<EmployeeAvailability> createEmployeeAvailabilityList(int tenantId, TenantConfiguration config, List<Employee> employeeList, LocalDate fromDate, LocalDate toDate) {
        List<EmployeeAvailability> out = new ArrayList<>();
        List<LocalDate> datesBetween = new ArrayList<>();
        for (LocalDate currDate = fromDate; currDate.isBefore(toDate); currDate = currDate.plusDays(1)) {
            datesBetween.add(currDate);
        }
        for (LocalDate date : datesBetween) {
            List<Employee> employeesListCopy = new ArrayList<>(employeeList);
            List<Employee> unavailableEmployees = new ArrayList<>(extractRandomSubList(employeesListCopy, 0.3));
            employeesListCopy.removeAll(unavailableEmployees);
            List<Employee> undesiredEmployees = new ArrayList<>(extractRandomSubList(employeesListCopy, 0.3));
            employeesListCopy.removeAll(undesiredEmployees);
            List<Employee> desiredEmployees = new ArrayList<>(extractRandomSubList(employeesListCopy, 0.3));
            employeesListCopy.removeAll(desiredEmployees);

            unavailableEmployees.forEach((e) -> {
                EmployeeAvailability toAdd = new EmployeeAvailability(tenantId, e, date, OffsetTime.of(LocalTime.MIN,
                        config.getTimeZone().getRules().getOffset(date.atStartOfDay())), OffsetTime.of(LocalTime.MAX,
                                config
                                        .getTimeZone().getRules().getOffset(date.atTime(LocalTime.MAX))));
                toAdd.setState(EmployeeAvailabilityState.UNAVAILABLE);
                out.add(toAdd);
            });
            undesiredEmployees.forEach((e) -> {
                EmployeeAvailability toAdd = new EmployeeAvailability(tenantId, e, date, OffsetTime.of(LocalTime.MIN,
                        config.getTimeZone().getRules().getOffset(date.atStartOfDay())), OffsetTime.of(LocalTime.MAX,
                                config
                                        .getTimeZone().getRules().getOffset(date.atTime(LocalTime.MAX))));
                toAdd.setState(EmployeeAvailabilityState.UNDESIRED);
                out.add(toAdd);
            });
            desiredEmployees.forEach((e) -> {
                EmployeeAvailability toAdd = new EmployeeAvailability(tenantId, e, date, OffsetTime.of(LocalTime.MIN,
                        config.getTimeZone().getRules().getOffset(date.atStartOfDay())), OffsetTime.of(LocalTime.MAX,
                                config
                                        .getTimeZone().getRules().getOffset(date.atTime(LocalTime.MAX))));
                toAdd.setState(EmployeeAvailabilityState.DESIRED);
                out.add(toAdd);
            });
        }
        out.forEach((e) -> entityManager.persist(e));
        return out;
    }

    private <E> List<E> extractRandomSubList(List<E> list, double maxRelativeSize) {
        List<E> subList = new ArrayList<>(list);
        Collections.shuffle(subList, random);
        int size = random.nextInt((int) (list.size() * maxRelativeSize)) + 1;
        // Remove elements not in the sublist (so it can be garbage collected)
        subList.subList(size, subList.size()).clear();
        return subList;
    }

    private <E> List<E> extractRandomSubListOfLength(List<E> list, int length) {
        List<E> subList = new ArrayList<>(list);
        Collections.shuffle(subList, random);
        // Remove elements not in the sublist (so it can be garbage collected)
        subList.subList(length, subList.size()).clear();
        return subList;
    }

    // TODO: Implement some of the logic from these methods into generateShiftTemplate

    /*private List<TimeSlot> createTimeSlotList(Integer tenantId, int size, boolean continuousPlanning) {
        List<TimeSlot> timeSlotList = new ArrayList<>(size);
        LocalDateTime previousEndDateTime = LocalDateTime.of(2017, 2, 1, 6, 0);
        for (int i = 0; i < size; i++) {
            LocalDateTime startDateTime = previousEndDateTime;
            LocalDateTime endDateTime = startDateTime.plusHours(8);
            TimeSlot timeSlot = new TimeSlot(tenantId, startDateTime, endDateTime);
            if (continuousPlanning && i < size / 2) {
                if (i < size / 4) {
                    timeSlot.setTimeSlotState(TimeSlotState.HISTORY);
                } else {
                    timeSlot.setTimeSlotState(TimeSlotState.TENTATIVE);
                }
            } else {
                timeSlot.setTimeSlotState(TimeSlotState.DRAFT);
            }
            entityManager.persist(timeSlot);
            timeSlotList.add(timeSlot);
            previousEndDateTime = endDateTime;
        }
        return timeSlotList;
    }
    
    private List<EmployeeAvailability> createEmployeeAvailabilityList(Integer tenantId,
            List<Employee> employeeList, List<TimeSlot> timeSlotList) {
        List<EmployeeAvailability> employeeAvailabilityList = new ArrayList<>(employeeList.size() * timeSlotList.size());
        for (Employee employee : employeeList) {
            for (TimeSlot timeSlot : extractRandomSubList(timeSlotList, 0.6)) {
                EmployeeAvailability employeeAvailability = new EmployeeAvailability(tenantId, employee, timeSlot);
                employeeAvailability.setState(EmployeeAvailabilityState.values()[
                        random.nextInt(EmployeeAvailabilityState.values().length)]);
                entityManager.persist(employeeAvailability);
            }
        }
        return employeeAvailabilityList;
    }
    
    private List<Shift> createShiftList(Integer tenantId, List<Spot> spotList, List<TimeSlot> timeSlotList,
            List<Employee> employeeList, boolean continuousPlanning) {
        List<Shift> shiftList = new ArrayList<>(spotList.size() * timeSlotList.size());
        for (Spot spot : spotList) {
            boolean weekendEnabled = random.nextInt(10) < 8;
            boolean nightEnabled = weekendEnabled && random.nextInt(10) < 8;
            int timeSlotIndex = 0;
            for (TimeSlot timeSlot : timeSlotList) {
                DayOfWeek dayOfWeek = timeSlot.getStartDateTime().getDayOfWeek();
                if (!weekendEnabled && (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)) {
                    timeSlotIndex++;
                    continue;
                }
                if (!nightEnabled && timeSlot.getStartDateTime().getHour() >= 20) {
                    timeSlotIndex++;
                    continue;
                }
                Shift shift = new Shift(tenantId, spot, timeSlot);
                if (continuousPlanning) {
                    if (timeSlotIndex < timeSlotList.size() / 2) {
                        List<Employee> availableEmployeeList = employeeList.stream()
    //                                .filter(employee -> !employee.getUnavailableTimeSlotSet().contains(timeSlot))
                                .collect(Collectors.toList());
                        Employee employee = availableEmployeeList.get(random.nextInt(availableEmployeeList.size()));
                        shift.setEmployee(employee);
                        shift.setLockedByUser(random.nextDouble() < 0.05);
                    }
                }
                entityManager.persist(shift);
                shiftList.add(shift);
                timeSlotIndex++;
            }
    
        }
        return shiftList;
    
    }*/

}
