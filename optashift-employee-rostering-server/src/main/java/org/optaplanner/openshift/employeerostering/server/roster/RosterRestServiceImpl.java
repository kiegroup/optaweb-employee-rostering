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
import org.optaplanner.openshift.employeerostering.shared.roster.Pagination;
import org.optaplanner.openshift.employeerostering.shared.roster.Roster;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestService;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterState;
import org.optaplanner.openshift.employeerostering.shared.roster.view.EmployeeRosterView;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;

public class RosterRestServiceImpl extends AbstractRestServiceImpl implements RosterRestService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private WannabeSolverManager solverManager;

    @Override
    @Transactional
    public SpotRosterView getCurrentSpotRosterView(Integer tenantId, Integer pageNumber, Integer numberOfItemsPerPage) {
        RosterState rosterState = entityManager.find(Tenant.class, tenantId).getRosterState();
        LocalDate startDate = rosterState.getFirstPublishedDate();
        LocalDate endDate = rosterState.getLastDraftDate();
        return getSpotRosterView(tenantId, startDate, endDate, Pagination.of(pageNumber, numberOfItemsPerPage));
    }

    @Override
    @Transactional
    public SpotRosterView getSpotRosterView(final Integer tenantId,
            final String startDateString,
            final String endDateString) {

        return getSpotRosterView(tenantId, LocalDate.parse(startDateString), LocalDate.parse(endDateString));
    }

    private SpotRosterView getSpotRosterView(final Integer tenantId,
            final LocalDate startDate,
            final LocalDate endDate,
            final Pagination pagination) {

        final List<Spot> spots = entityManager.createNamedQuery("Spot.findAll", Spot.class)
                .setParameter("tenantId", tenantId)
                .setMaxResults(pagination.getNumberOfItemsPerPage())
                .setFirstResult(pagination.getFirstResultIndex())
                .getResultList();

        return getSpotRosterView(tenantId, startDate, endDate, spots);
    }

    private SpotRosterView getSpotRosterView(final Integer tenantId,
            final LocalDate startDate,
            final LocalDate endDate) {

        final List<Spot> spots = entityManager.createNamedQuery("Spot.findAll", Spot.class)
                .setParameter("tenantId", tenantId)
                .getResultList();

        return getSpotRosterView(tenantId, startDate, endDate, spots);
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
        List<Employee> employeeList = entityManager.createNamedQuery("Employee.findAll", Employee.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        spotRosterView.setEmployeeList(employeeList);

        List<Shift> shiftList = entityManager.createNamedQuery("Shift.findAll", Shift.class)
                .setParameter("tenantId", tenantId)
                .getResultList();

        Map<Long, List<ShiftView>> spotIdToShiftViewListMap = new LinkedHashMap<>(spotList.size());
        for (Shift shift : shiftList) {
            spotIdToShiftViewListMap.computeIfAbsent(shift.getSpot().getId(), k -> new ArrayList<>()).add(new ShiftView(
                    shift));
        }
        spotRosterView.setSpotIdToShiftViewListMap(spotIdToShiftViewListMap);

        //Score
        spotRosterView.setScore(solverManager.getRoster(tenantId).map(Roster::getScore).orElse(null));

        return spotRosterView;
    }

    //
    //
    // Employee Roster

    @Override
    @Transactional
    public EmployeeRosterView getCurrentEmployeeRosterView(Integer tenantId) {
        RosterState rosterState = entityManager.find(Tenant.class, tenantId).getRosterState();
        LocalDate startDate = rosterState.getFirstPublishedDate();
        LocalDate endDate = rosterState.getLastDraftDate();
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
            List<Employee> employees) {
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

        // TODO use startDate and endDate in query
        List<Shift> shiftList = entityManager.createNamedQuery("Shift.findAll", Shift.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        Map<Long, List<ShiftView>> employeeIdToShiftViewListMap = new LinkedHashMap<>(employeeList.size());
        for (Shift shift : shiftList) {
            employeeIdToShiftViewListMap.computeIfAbsent(shift.getEmployee().getId(), k -> new ArrayList<>()).add(
                    new ShiftView(shift));
        }
        employeeRosterView.setEmployeeIdToShiftViewListMap(employeeIdToShiftViewListMap);
        Map<Long, List<EmployeeAvailabilityView>> employeeIdToAvailabilityViewListMap = new LinkedHashMap<>(employeeList
                .size());
        // TODO use startDate and endDate
        List<EmployeeAvailability> employeeAvailabilityList = entityManager.createNamedQuery(
                "EmployeeAvailability.findAll", EmployeeAvailability.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        for (EmployeeAvailability employeeAvailability : employeeAvailabilityList) {
            employeeIdToAvailabilityViewListMap.computeIfAbsent(employeeAvailability.getEmployee().getId(),
                    k -> new ArrayList<>()).add(new EmployeeAvailabilityView(employeeAvailability));
        }
        employeeRosterView.setEmployeeIdToAvailabilityViewListMap(employeeIdToAvailabilityViewListMap);
        Roster roster = solverManager.getRoster(tenantId).orElse(null);
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
        List<EmployeeAvailability> employeeAvailabilityList = entityManager.createNamedQuery(
                "EmployeeAvailability.findAll", EmployeeAvailability.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        List<Shift> shiftList = entityManager.createNamedQuery("Shift.findAll", Shift.class)
                .setParameter("tenantId", tenantId)
                .getResultList();

        Tenant tenant = entityManager.find(Tenant.class, tenantId);
        // TODO fill in the score too - do we inject a ScoreDirectorFactory?
        return new Roster((long) tenantId, tenantId,
                skillList, spotList, employeeList, employeeAvailabilityList,
                tenant.getConfiguration(), tenant.getRosterState(), shiftList);
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
}
