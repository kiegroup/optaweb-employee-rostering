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
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.optaplanner.openshift.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaplanner.openshift.employeerostering.server.solver.WannabeSolverManager;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.roster.Roster;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestService;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

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
        return getCurrentSpotRosterView(tenantId, startDate, endDate);
    }

    @Override
    @Transactional
    public SpotRosterView getCurrentSpotRosterView(Integer tenantId, String startDateString, String endDateString) {
        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);
        return getCurrentSpotRosterView(tenantId, startDate, endDate);
    }

    @Transactional
    protected SpotRosterView getCurrentSpotRosterView(Integer tenantId, LocalDate startDate, LocalDate endDate) {
        SpotRosterView spotRosterView = new SpotRosterView(tenantId, startDate, endDate);
        List<Spot> spotList = entityManager.createNamedQuery("Spot.findAll", Spot.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        spotRosterView.setSpotList(spotList);
        List<Employee> employeeList = entityManager.createNamedQuery("Employee.findAll", Employee.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        spotRosterView.setEmployeeList(employeeList);
        List<TimeSlot> timeSlotList = entityManager.createNamedQuery("TimeSlot.findAll", TimeSlot.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        spotRosterView.setTimeSlotList(timeSlotList);
//        spotRosterView.setTimeSlotList(entityManager.createNamedQuery("TimeSlot.findByStartDateEndDate", TimeSlot.class)
//                .setParameter("tenantId", tenantId)
//                .setParameter("startDate", startDate)
//                .setParameter("endDate", endDate)
//                .getResultList());
        List<Shift> shiftList = entityManager.createNamedQuery("Shift.findAll", Shift.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        Map<Long, Map<Long, List<ShiftView>>> spotIdMap = new LinkedHashMap<>(spotList.size());
        for (Shift shift : shiftList) {
            Long spotId = shift.getSpot().getId();
            Map<Long, List<ShiftView>> timeSlotIdMap = spotIdMap
                    .computeIfAbsent(spotId, k -> new LinkedHashMap<>(timeSlotList.size()));
            Long timeSlotId = shift.getTimeSlot().getId();
            List<ShiftView> shiftViewList = timeSlotIdMap
                    .computeIfAbsent(timeSlotId, k -> new ArrayList<>(2));
            shiftViewList.add(new ShiftView(shift));
        }
        spotRosterView.setSpotIdToTimeSlotIdToShiftViewListMap(spotIdMap);
        return spotRosterView;
    }

    @Override
    public void solveRoster(Integer tenantId) {
        solverManager.solve(tenantId);
    }

    // TODO remove these
    @Inject
    private RosterDao rosterDao;

    // TODO remove these
    @Override
    public Roster getRoster(Integer tenantId) {
        return rosterDao.getRoster(tenantId);
    }

}
