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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.optaplanner.openshift.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaplanner.openshift.employeerostering.server.solver.WannabeSolverManager;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.roster.IndictmentMap;
import org.optaplanner.openshift.employeerostering.shared.roster.Roster;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestService;
import org.optaplanner.openshift.employeerostering.shared.roster.view.EmployeeRosterView;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlotUtils;

public class RosterRestServiceImpl extends AbstractRestServiceImpl implements RosterRestService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private WannabeSolverManager solverManager;

    @Override
    @Transactional
    public SpotRosterView getCurrentSpotRosterView(Integer tenantId) {
        List<TimeSlot> timeSlotList = entityManager.createNamedQuery("TimeSlot.findAll", TimeSlot.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        LocalDate startDate;
        LocalDate endDate;
        if (timeSlotList.isEmpty()) {
            startDate = LocalDate.parse("1900-01-01");
            endDate = LocalDate.parse("2900-01-01");
        } else {
            startDate = timeSlotList.get(0).getStartDateTime().toLocalDate();
            endDate = timeSlotList.get(timeSlotList.size() - 1).getStartDateTime().toLocalDate();
        }
        return getSpotRosterView(tenantId, startDate, endDate, entityManager.createNamedQuery("Spot.findAll",
                Spot.class)
                .setParameter("tenantId", tenantId)
                .getResultList());
    }

    @Override
    @Transactional
    public SpotRosterView getSpotRosterView(Integer tenantId, String startDateString, String endDateString) {
        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);
        return getSpotRosterView(tenantId, startDate, endDate, entityManager.createNamedQuery("Spot.findAll",
                Spot.class)
                .setParameter("tenantId", tenantId)
                .getResultList());
    }

    @Override
    @Transactional
    public SpotRosterView getSpotRosterViewFor(Integer tenantId, String startDateString, String endDateString, List<
            Spot> spots) {
        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);
        if (null == spots) {
            throw new IllegalArgumentException("spots is null!");
        }
        return getSpotRosterView(tenantId, startDate, endDate, spots);
    }

    @Transactional
    protected SpotRosterView getSpotRosterView(Integer tenantId, LocalDate startDate, LocalDate endDate, List<
            Spot> spotList) {
        SpotRosterView spotRosterView = new SpotRosterView(tenantId, startDate, endDate);
        spotRosterView.setSpotList(spotList);
        Set<Spot> spotSet = spotList.stream().collect(Collectors.toSet());
        List<Employee> employeeList = entityManager.createNamedQuery("Employee.findAll", Employee.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        spotRosterView.setEmployeeList(employeeList);
        // TODO use startDate and endDate in query
        List<TimeSlot> timeSlotList = entityManager.createNamedQuery("TimeSlot.findAll", TimeSlot.class)
                .setParameter("tenantId", tenantId)
                .getResultList().stream().filter((t) -> TimeSlotUtils.doTimeslotsIntersect(startDate.atStartOfDay(),
                        endDate.atStartOfDay(),
                        t.getStartDateTime(), t.getEndDateTime())).collect(Collectors.toList());
        spotRosterView.setTimeSlotList(timeSlotList);
//        spotRosterView.setTimeSlotList(entityManager.createNamedQuery("TimeSlot.findByStartDateEndDate", TimeSlot.class)
//                .setParameter("tenantId", tenantId)
//                .setParameter("startDate", startDate)
//                .setParameter("endDate", endDate)
//                .getResultList());
        List<Shift> shiftList = entityManager.createNamedQuery("Shift.findAll", Shift.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        Map<Long, Map<Long, List<ShiftView>>> timeSlotIdMap = new LinkedHashMap<>(timeSlotList.size());
        for (Shift shift : shiftList) {
            Long timeSlotId = shift.getTimeSlot().getId();
            if (spotSet.contains(shift.getSpot())) {
            Map<Long, List<ShiftView>> spotIdMap = timeSlotIdMap
                    .computeIfAbsent(timeSlotId, k -> new LinkedHashMap<>(spotList.size()));
            Long spotId = shift.getSpot().getId();
            List<ShiftView> shiftViewList = spotIdMap
                    .computeIfAbsent(spotId, k -> new ArrayList<>(2));
            shiftViewList.add(new ShiftView(shift));
            }
        }
        spotRosterView.setTimeSlotIdToSpotIdToShiftViewListMap(timeSlotIdMap);

        Roster roster = solverManager.getRoster(tenantId);
        if (null != roster) {
            spotRosterView.setScore(roster.getScore());
        }
        return spotRosterView;
    }

    @Override
    @Transactional
    public EmployeeRosterView getCurrentEmployeeRosterView(Integer tenantId) {
        List<TimeSlot> timeSlotList = entityManager.createNamedQuery("TimeSlot.findAll", TimeSlot.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        LocalDate startDate;
        LocalDate endDate;
        if (timeSlotList.isEmpty()) {
            startDate = LocalDate.parse("1900-01-01");
            endDate = LocalDate.parse("2900-01-01");
        } else {
            startDate = timeSlotList.get(0).getStartDateTime().toLocalDate();
            endDate = timeSlotList.get(timeSlotList.size() - 1).getStartDateTime().toLocalDate();
        }
        return getEmployeeRosterView(tenantId, startDate, endDate, entityManager.createNamedQuery("Employee.findAll",
                Employee.class)
                .setParameter("tenantId", tenantId)
                .getResultList());
    }

    @Override
    @Transactional
    public EmployeeRosterView getEmployeeRosterView(Integer tenantId, String startDateString, String endDateString) {
        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);
        return getEmployeeRosterView(tenantId, startDate, endDate, entityManager.createNamedQuery("Employee.findAll",
                Employee.class)
                .setParameter("tenantId", tenantId)
                .getResultList());
    }

    @Override
    @Transactional
    public EmployeeRosterView getEmployeeRosterViewFor(Integer tenantId, String startDateString, String endDateString,
            List<
                    Employee> employees) {
        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);
        if (null == employees) {
            throw new IllegalArgumentException("employees is null!");
        }
        return getEmployeeRosterView(tenantId, startDate, endDate, employees);
    }

    @Transactional
    protected EmployeeRosterView getEmployeeRosterView(Integer tenantId, LocalDate startDate, LocalDate endDate, List<
            Employee> employeeList) {
        EmployeeRosterView employeeRosterView = new EmployeeRosterView(tenantId, startDate, endDate);
        List<Spot> spotList = entityManager.createNamedQuery("Spot.findAll", Spot.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        employeeRosterView.setSpotList(spotList);

        employeeRosterView.setEmployeeList(employeeList);
        Set<Employee> employeeSet = employeeList.stream().collect(Collectors.toSet());
        // TODO use startDate and endDate in query
        List<TimeSlot> timeSlotList = entityManager.createNamedQuery("TimeSlot.findAll", TimeSlot.class)
                .setParameter("tenantId", tenantId)
                .getResultList().stream().filter((t) -> TimeSlotUtils.doTimeslotsIntersect(startDate.atStartOfDay(),
                        endDate.atStartOfDay(),
                        t.getStartDateTime(), t.getEndDateTime())).collect(Collectors.toList());
        employeeRosterView.setTimeSlotList(timeSlotList);
        List<Shift> shiftList = entityManager.createNamedQuery("Shift.findAll", Shift.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        Map<Long, Map<Long, List<ShiftView>>> timeSlotIdToEmployeeIdToShiftViewListMap = new LinkedHashMap<>(timeSlotList.size());
        for (Shift shift : shiftList) {
            Long timeSlotId = shift.getTimeSlot().getId();
            Map<Long, List<ShiftView>> employeeIdMap = timeSlotIdToEmployeeIdToShiftViewListMap
                    .computeIfAbsent(timeSlotId, k -> new LinkedHashMap<>(spotList.size()));
            Employee employee = shift.getEmployee();
            if (employee != null && employeeSet.contains(employee)) {
                Long employeeId = employee.getId();
                List<ShiftView> shiftViewList = employeeIdMap
                        .computeIfAbsent(employeeId, k -> new ArrayList<>(2));
                shiftViewList.add(new ShiftView(shift));
            }
        }
        employeeRosterView.setTimeSlotIdToEmployeeIdToShiftViewListMap(timeSlotIdToEmployeeIdToShiftViewListMap);
        Map<Long, Map<Long, EmployeeAvailabilityView>> timeSlotIdToEmployeeIdToAvailabilityViewMap = new LinkedHashMap<>(timeSlotList.size());
        // TODO use startDate and endDate
        List<EmployeeAvailability>employeeAvailabilityList = entityManager.createNamedQuery("EmployeeAvailability.findAll", EmployeeAvailability.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        for (EmployeeAvailability employeeAvailability : employeeAvailabilityList) {

            Long timeSlotId = employeeAvailability.getTimeSlot().getId();
            Map<Long, EmployeeAvailabilityView> employeeIdMap = timeSlotIdToEmployeeIdToAvailabilityViewMap
                    .computeIfAbsent(timeSlotId, k -> new LinkedHashMap<>(spotList.size()));
            Long employeeId = employeeAvailability.getEmployee().getId();
            if (employeeSet.contains(employeeAvailability.getEmployee())) {
                EmployeeAvailabilityView old = employeeIdMap.put(employeeId, new EmployeeAvailabilityView(
                        employeeAvailability));
                if (old != null) {
                    throw new IllegalStateException("Duplicate employeeAvailability (" + employeeAvailability + ", "
                            + old
                            + ") for timeSlot (" + employeeAvailability.getTimeSlot()
                            + ") and employee (" + employeeAvailability.getEmployee() + ").");
                }
            }
        }
        employeeRosterView.setTimeSlotIdToEmployeeIdToAvailabilityViewMap(timeSlotIdToEmployeeIdToAvailabilityViewMap);
        Roster roster = solverManager.getRoster(tenantId);
        if (null != roster) {
            employeeRosterView.setScore(roster.getScore());
        }
        return employeeRosterView;
    }

    @Override
    public void solveRoster(Integer tenantId) {
        solverManager.solve(tenantId);
    }
    
    @Override
    public void terminateRosterEarly(Integer tenantId) {
        solverManager.terminate(tenantId);
    }

    @Override
    @Transactional
    public Roster buildRoster(Integer tenantId) {
        List<Skill> skillList = entityManager.createNamedQuery("Skill.findAll", Skill.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        List<Spot> spotList = entityManager.createNamedQuery("Spot.findAll", Spot.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        List<Employee> employeeList = entityManager.createNamedQuery("Employee.findAll", Employee.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        List<TimeSlot> timeSlotList = entityManager.createNamedQuery("TimeSlot.findAll", TimeSlot.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        List<EmployeeAvailability> employeeAvailabilityList
                = entityManager.createNamedQuery("EmployeeAvailability.findAll", EmployeeAvailability.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        List<Shift> shiftList = entityManager.createNamedQuery("Shift.findAll", Shift.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        
        Tenant tenant = entityManager.find(Tenant.class, tenantId);
        // TODO fill in the score too - do we inject a ScoreDirectorFactory?
        return new Roster((long) tenantId, tenantId,
                skillList, spotList, employeeList, timeSlotList, employeeAvailabilityList, 
                tenant.getConfiguration(), shiftList);
    }

    @Override
    @Transactional
    public void updateShiftsOfRoster(Roster newRoster) {
        Integer tenantId = newRoster.getTenantId();
        // TODO HACK avoids optimistic locking exception while solve(), but it circumvents optimistic locking completely
        Map<Long, Employee> employeeIdMap = entityManager.createNamedQuery("Employee.findAll", Employee.class)
                .setParameter("tenantId", tenantId)
                .getResultList().stream().collect(Collectors.toMap(Employee::getId, Function.identity()));
        Map<Long, Shift> shiftIdMap = entityManager.createNamedQuery("Shift.findAll", Shift.class)
                .setParameter("tenantId", tenantId)
                .getResultList().stream().collect(Collectors.toMap(Shift::getId, Function.identity()));

        for (Shift shift : newRoster.getShiftList()) {
            Shift attachedShift = shiftIdMap.get(shift.getId());
            if (attachedShift == null) {
                continue;
            }
            attachedShift.setEmployee((shift.getEmployee() == null)
                    ? null : employeeIdMap.get(shift.getEmployee().getId()));
        }
    }

    @Override
    public IndictmentMap getIndictmentMap(Integer tenantId) {
        return new IndictmentMap(solverManager.getIndictmentMap(tenantId));
    }

}
