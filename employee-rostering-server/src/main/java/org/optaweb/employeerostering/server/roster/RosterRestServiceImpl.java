/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.server.roster;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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

import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaweb.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaweb.employeerostering.server.common.IndictmentUtils;
import org.optaweb.employeerostering.server.solver.WannabeSolverManager;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeAvailability;
import org.optaweb.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.shared.roster.Pagination;
import org.optaweb.employeerostering.shared.roster.PublishResult;
import org.optaweb.employeerostering.shared.roster.Roster;
import org.optaweb.employeerostering.shared.roster.RosterRestService;
import org.optaweb.employeerostering.shared.roster.RosterState;
import org.optaweb.employeerostering.shared.roster.view.AvailabilityRosterView;
import org.optaweb.employeerostering.shared.roster.view.ShiftRosterView;
import org.optaweb.employeerostering.shared.rotation.ShiftTemplate;
import org.optaweb.employeerostering.shared.shift.Shift;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.skill.Skill;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.tenant.TenantRestService;

import static java.util.stream.Collectors.groupingBy;

public class RosterRestServiceImpl extends AbstractRestServiceImpl
        implements
        RosterRestService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private WannabeSolverManager solverManager;

    @Inject
    private TenantRestService tenantRestService;

    @Inject
    private IndictmentUtils indictmentUtils;

    // ************************************************************************
    // ShiftRosterView
    // ************************************************************************

    @Override
    @Transactional
    public ShiftRosterView getCurrentShiftRosterView(Integer tenantId, Integer pageNumber, Integer numberOfItemsPerPage) {
        RosterState rosterState = getRosterState(tenantId);
        LocalDate startDate = rosterState.getFirstPublishedDate();
        LocalDate endDate = rosterState.getFirstUnplannedDate();
        return getShiftRosterView(tenantId, startDate, endDate, Pagination.of(pageNumber, numberOfItemsPerPage));
    }

    @Override
    @Transactional
    public ShiftRosterView getShiftRosterView(final Integer tenantId,
                                              Integer pageNumber,
                                              Integer numberOfItemsPerPage,
                                              final String startDateString,
                                              final String endDateString) {

        return getShiftRosterView(tenantId, LocalDate.parse(startDateString), LocalDate.parse(endDateString), Pagination.of(pageNumber, numberOfItemsPerPage));
    }

    private ShiftRosterView getShiftRosterView(final Integer tenantId,
                                               final LocalDate startDate,
                                               final LocalDate endDate,
                                               final Pagination pagination) {

        final List<Spot> spots = entityManager.createNamedQuery("Spot.findAll", Spot.class)
                .setParameter("tenantId", tenantId)
                .setMaxResults(pagination.getNumberOfItemsPerPage())
                .setFirstResult(pagination.getFirstResultIndex())
                .getResultList();

        return getShiftRosterView(tenantId, startDate, endDate, spots);
    }

    @Override
    @Transactional
    public ShiftRosterView getShiftRosterViewFor(Integer tenantId, String startDateString, String endDateString, List<Spot> spots) {
        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);
        if (null == spots) {
            throw new IllegalArgumentException("spots is null!");
        }

        return getShiftRosterView(tenantId, startDate, endDate, spots);
    }

    @Transactional
    protected ShiftRosterView getShiftRosterView(Integer tenantId, LocalDate startDate, LocalDate endDate, List<Spot> spotList) {
        ShiftRosterView shiftRosterView = new ShiftRosterView(tenantId, startDate, endDate);
        shiftRosterView.setSpotList(spotList);
        List<Employee> employeeList = entityManager.createNamedQuery("Employee.findAll", Employee.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        shiftRosterView.setEmployeeList(employeeList);

        Set<Spot> spotSet = new HashSet<>(spotList);
        ZoneId timeZone = getRosterState(tenantId).getTimeZone();

        List<Shift> shiftList = entityManager.createNamedQuery("Shift.filterWithSpots", Shift.class)
                .setParameter("tenantId", tenantId)
                .setParameter("spotSet", spotSet)
                .setParameter("startDateTime", startDate.atStartOfDay(timeZone).toOffsetDateTime())
                .setParameter("endDateTime", endDate.atStartOfDay(timeZone).toOffsetDateTime())
                .getResultList();

        Map<Long, List<ShiftView>> spotIdToShiftViewListMap = new LinkedHashMap<>(spotList.size());
        // TODO FIXME race condition solverManager's bestSolution might differ from the one we just fetched,
        // so the score might be inaccurate.
        Roster roster = solverManager.getRoster(tenantId);
        if (roster == null) {
            roster = buildRoster(tenantId);
        }
        Map<Object, Indictment> indictmentMap = indictmentUtils.getIndictmentMapForRoster(roster);

        for (Shift shift : shiftList) {
            Indictment indictment = indictmentMap.get(shift);
            spotIdToShiftViewListMap.computeIfAbsent(shift.getSpot().getId(), k -> new ArrayList<>())
                    .add(indictmentUtils.getShiftViewWithIndictment(timeZone, shift, indictment));
        }
        shiftRosterView.setSpotIdToShiftViewListMap(spotIdToShiftViewListMap);

        shiftRosterView.setScore(roster == null ? null : roster.getScore());
        shiftRosterView.setRosterState(getRosterState(tenantId));

        return shiftRosterView;
    }

    // ************************************************************************
    // AvailabilityRosterView
    // ************************************************************************

    @Override
    @Transactional
    public AvailabilityRosterView getCurrentAvailabilityRosterView(Integer tenantId,
                                                                   Integer pageNumber,
                                                                   Integer numberOfItemsPerPage) {
        RosterState rosterState = getRosterState(tenantId);
        LocalDate startDate = rosterState.getLastHistoricDate();
        LocalDate endDate = rosterState.getFirstUnplannedDate();
        return getAvailabilityRosterView(tenantId, startDate, endDate, Pagination.of(pageNumber, numberOfItemsPerPage));
    }

    @Override
    @Transactional
    public AvailabilityRosterView getAvailabilityRosterView(Integer tenantId,
                                                            Integer pageNumber,
                                                            Integer numberOfItemsPerPage,
                                                            String startDateString,
                                                            String endDateString) {
        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);
        return getAvailabilityRosterView(tenantId, startDate, endDate, Pagination.of(pageNumber, numberOfItemsPerPage));
    }

    @Override
    @Transactional
    public AvailabilityRosterView getAvailabilityRosterViewFor(Integer tenantId,
                                                               String startDateString,
                                                               String endDateString,
                                                               List<Employee> employeeList) {
        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);
        if (employeeList == null) {
            throw new IllegalArgumentException("The employeeList (" + employeeList + ") must not be null.");
        }
        return getAvailabilityRosterView(tenantId, startDate, endDate, employeeList);
    }

    private AvailabilityRosterView getAvailabilityRosterView(final Integer tenantId,
                                                             final LocalDate startDate,
                                                             final LocalDate endDate,
                                                             final Pagination pagination) {

        final List<Employee> employeeList = entityManager.createNamedQuery("Employee.findAll", Employee.class)
                .setParameter("tenantId", tenantId)
                .setMaxResults(pagination.getNumberOfItemsPerPage())
                .setFirstResult(pagination.getFirstResultIndex())
                .getResultList();

        return getAvailabilityRosterView(tenantId, startDate, endDate, employeeList);
    }

    @Transactional
    protected AvailabilityRosterView getAvailabilityRosterView(Integer tenantId,
                                                               LocalDate startDate,
                                                               LocalDate endDate,
                                                               List<Employee> employeeList) {
        AvailabilityRosterView availabilityRosterView = new AvailabilityRosterView(tenantId, startDate, endDate);
        List<Spot> spotList = entityManager.createNamedQuery("Spot.findAll", Spot.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        availabilityRosterView.setSpotList(spotList);

        availabilityRosterView.setEmployeeList(employeeList);

        Map<Long, List<ShiftView>> employeeIdToShiftViewListMap = new LinkedHashMap<>(employeeList.size());
        List<ShiftView> unassignedShiftViewList = new ArrayList<>();
        Set<Employee> employeeSet = new HashSet<>(employeeList);
        ZoneId timeZone = getRosterState(tenantId).getTimeZone();

        List<Shift> shiftList = entityManager.createNamedQuery("Shift.filterWithEmployees", Shift.class)
                .setParameter("tenantId", tenantId)
                .setParameter("startDateTime", startDate.atStartOfDay(timeZone).toOffsetDateTime())
                .setParameter("endDateTime", endDate.atStartOfDay(timeZone).toOffsetDateTime())
                .setParameter("employeeSet", employeeSet)
                .getResultList();

        Roster roster = solverManager.getRoster(tenantId);
        if (roster == null) {
            roster = buildRoster(tenantId);
        }
        Map<Object, Indictment> indictmentMap = indictmentUtils.getIndictmentMapForRoster(roster);

        for (Shift shift : shiftList) {
            Indictment indictment = indictmentMap.get(shift);
            if (shift.getEmployee() != null) {
                employeeIdToShiftViewListMap.computeIfAbsent(shift.getEmployee().getId(),
                                                             k -> new ArrayList<>())
                        .add(indictmentUtils.getShiftViewWithIndictment(timeZone, shift, indictment));
            } else {
                unassignedShiftViewList.add(indictmentUtils.getShiftViewWithIndictment(timeZone, shift, indictment));
            }
        }
        availabilityRosterView.setEmployeeIdToShiftViewListMap(employeeIdToShiftViewListMap);
        availabilityRosterView.setUnassignedShiftViewList(unassignedShiftViewList);
        Map<Long, List<EmployeeAvailabilityView>> employeeIdToAvailabilityViewListMap = new LinkedHashMap<>(
                employeeList.size());
        List<EmployeeAvailability> employeeAvailabilityList = entityManager.createNamedQuery(
                "EmployeeAvailability.filterWithEmployee", EmployeeAvailability.class)
                .setParameter("tenantId", tenantId)
                .setParameter("startDateTime", startDate.atStartOfDay(timeZone).toOffsetDateTime())
                .setParameter("endDateTime", endDate.atStartOfDay(timeZone).toOffsetDateTime())
                .setParameter("employeeSet", employeeSet)
                .getResultList();
        for (EmployeeAvailability employeeAvailability : employeeAvailabilityList) {
            employeeIdToAvailabilityViewListMap.computeIfAbsent(employeeAvailability.getEmployee().getId(),
                                                                k -> new ArrayList<>())
                    .add(new EmployeeAvailabilityView(timeZone, employeeAvailability));
        }
        availabilityRosterView.setEmployeeIdToAvailabilityViewListMap(employeeIdToAvailabilityViewListMap);

        // TODO FIXME race condition solverManager's bestSolution might differ from the one we just fetched,
        // so the score might be inaccurate.
        availabilityRosterView.setScore(roster.getScore());
        availabilityRosterView.setRosterState(getRosterState(tenantId));
        return availabilityRosterView;
    }

    // ************************************************************************
    // Other
    // ************************************************************************

    @Override
    public void solveRoster(Integer tenantId) {
        solverManager.solve(tenantId);
    }

    @Override
    public void terminateRosterEarly(Integer tenantId) {
        solverManager.terminate(tenantId);
    }

    @Transactional
    public Roster buildRoster(Integer tenantId) {
        ZoneId zoneId = getRosterState(tenantId).getTimeZone();
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
                .getResultList()
                .stream()
                .map(ea -> ea.inTimeZone(zoneId))
                .collect(Collectors.toList());
        List<Shift> shiftList = entityManager.createNamedQuery("Shift.findAll", Shift.class)
                .setParameter("tenantId", tenantId)
                .getResultList()
                .stream()
                .map(s -> s.inTimeZone(zoneId))
                .collect(Collectors.toList());

        // TODO fill in the score too - do we inject a ScoreDirectorFactory?
        return new Roster((long) tenantId, tenantId,
                          skillList, spotList, employeeList, employeeAvailabilityList,
                          tenantRestService.getRosterParametrization(tenantId), getRosterState(tenantId), shiftList);
    }

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
    @Transactional
    public PublishResult publishAndProvision(Integer tenantId) {
        RosterState rosterState = getRosterState(tenantId);
        LocalDate publishFrom = rosterState.getFirstDraftDate();
        LocalDate publishTo = publishFrom.plusDays(rosterState.getPublishLength());
        LocalDate firstUnplannedDate = rosterState.getFirstUnplannedDate();
        // Publish
        rosterState.setFirstDraftDate(publishTo);
        // Provision
        List<ShiftTemplate> shiftTemplateList = entityManager.createNamedQuery("ShiftTemplate.findAll", ShiftTemplate.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        Map<Integer, List<ShiftTemplate>> dayOffsetToShiftTemplateListMap = shiftTemplateList.stream()
                .collect(groupingBy(ShiftTemplate::getStartDayOffset));
        int dayOffset = rosterState.getUnplannedRotationOffset();
        LocalDate shiftDate = firstUnplannedDate;
        for (int i = 0; i < rosterState.getPublishLength(); i++) {
            List<ShiftTemplate> dayShiftTemplateList = dayOffsetToShiftTemplateListMap.getOrDefault(dayOffset, Collections.emptyList());
            for (ShiftTemplate shiftTemplate : dayShiftTemplateList) {
                Shift shift = shiftTemplate.createShiftOnDate(shiftDate, rosterState.getRotationLength(), rosterState.getTimeZone(), false);
                entityManager.persist(shift);
            }
            shiftDate = shiftDate.plusDays(1);
            dayOffset = (dayOffset + 1) % rosterState.getRotationLength();
        }
        rosterState.setUnplannedRotationOffset(dayOffset);
        return new PublishResult(publishFrom, publishTo);
    }

    @Override
    public RosterState getRosterState(Integer tenantId) {
        return entityManager.createNamedQuery("RosterState.find", RosterState.class)
                .setParameter("tenantId", tenantId)
                .getSingleResult();
    }
}
