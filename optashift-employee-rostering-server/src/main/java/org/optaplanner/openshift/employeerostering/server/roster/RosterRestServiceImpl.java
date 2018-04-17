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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.openshift.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaplanner.openshift.employeerostering.server.solver.WannabeSolverManager;
import org.optaplanner.openshift.employeerostering.shared.common.OutOfDateException;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.roster.Pagination;
import org.optaplanner.openshift.employeerostering.shared.roster.Roster;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestService;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterState;
import org.optaplanner.openshift.employeerostering.shared.roster.view.EmployeeRosterView;
import org.optaplanner.openshift.employeerostering.shared.roster.view.IndictmentView;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;
import org.optaplanner.openshift.employeerostering.shared.rotation.ShiftTemplate;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestService;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantConfiguration;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantRestService;

import static java.util.stream.Collectors.groupingBy;

@ApplicationScoped
public class RosterRestServiceImpl extends AbstractRestServiceImpl implements RosterRestService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private WannabeSolverManager solverManager;

    @Inject
    private TenantRestService tenantRestService;

    @Inject
    private ShiftRestService shiftRestService;

    private Map<Integer, Map<Shift, IndictmentView>> tenantIdToShiftIndictmentMap = new ConcurrentHashMap<>();
    private Map<Integer, Map<Employee, IndictmentView>> tenantIdToEmployeeIndictmentMap = new ConcurrentHashMap<>();
    private Map<Integer, List<List<Long>>> tenantIdToShiftIndictmentMapUpdateTime = new ConcurrentHashMap<>();
    private Map<Integer, List<List<Long>>> tenantIdToEmployeeIndictmentMapUpdateTime = new ConcurrentHashMap<>();

    // ************************************************************************
    // SpotRosterView
    // ************************************************************************

    @Override
    @Transactional
    public SpotRosterView getCurrentSpotRosterView(Integer tenantId, Integer pageNumber, Integer numberOfItemsPerPage) {
        RosterState rosterState = getRosterState(tenantId);
        LocalDate startDate = rosterState.getFirstPublishedDate();
        LocalDate endDate = rosterState.getFirstUnplannedDate();
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
    public SpotRosterView getSpotRosterViewFor(Integer tenantId, String startDateString, String endDateString, List<Spot> spots) {
        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);
        if (null == spots) {
            throw new IllegalArgumentException("spots is null!");
        }

        return getSpotRosterView(tenantId, startDate, endDate, spots);
    }

    @Transactional
    protected SpotRosterView getSpotRosterView(Integer tenantId, LocalDate startDate, LocalDate endDate, List<Spot> spotList) {
        SpotRosterView spotRosterView = new SpotRosterView(tenantId, startDate, endDate);
        spotRosterView.setSpotList(spotList);
        List<Employee> employeeList = entityManager.createNamedQuery("Employee.findAll", Employee.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        spotRosterView.setEmployeeList(employeeList);

        Set<Spot> spotSet = new HashSet<>(spotList);
        TenantConfiguration tenantConfig = entityManager.createNamedQuery("TenantConfiguration.find",
                TenantConfiguration.class)
                .setParameter("tenantId", tenantId).getSingleResult();

        List<Shift> shiftList = entityManager.createNamedQuery("Shift.filterWithSpots", Shift.class)
                .setParameter("tenantId", tenantId)
                .setParameter("spotSet", spotSet)
                .setParameter("startDateTime", startDate.atStartOfDay(tenantConfig.getTimeZone()).toOffsetDateTime())
                .setParameter("endDateTime", endDate.atStartOfDay(tenantConfig.getTimeZone()).toOffsetDateTime())
                .getResultList();

        Map<Long, List<ShiftView>> spotIdToShiftViewListMap = new LinkedHashMap<>(spotList.size());
        for (Shift shift : shiftList) {
            spotIdToShiftViewListMap.computeIfAbsent(shift.getSpot().getId(), k -> new ArrayList<>()).add(
                    new ShiftView(
                            shift));
        }
        spotRosterView.setSpotIdToShiftViewListMap(spotIdToShiftViewListMap);

        // TODO FIXME race condition solverManager's bestSolution might differ from the one we just fetched,
        // so the score might be inaccurate.
        Solver<Roster> solver = solverManager.getSolver(tenantId);
        Roster roster = (solver != null) ? solver.getBestSolution() : null;
        spotRosterView.setScore(roster == null ? null : roster.getScore());
        spotRosterView.setRosterState(getRosterState(tenantId));

        return spotRosterView;
    }

    // ************************************************************************
    // EmployeeRosterView
    // ************************************************************************

    @Override
    @Transactional
    public EmployeeRosterView getCurrentEmployeeRosterView(Integer tenantId, Integer pageNumber,
                                                           Integer numberOfItemsPerPage) {
        RosterState rosterState = getRosterState(tenantId);
        LocalDate startDate = rosterState.getLastHistoricDate();
        LocalDate endDate = rosterState.getFirstUnplannedDate();
        return getEmployeeRosterView(tenantId, startDate, endDate, Pagination.of(pageNumber, numberOfItemsPerPage));
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
                                                       List<Employee> employeeList) {
        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);
        if (employeeList == null) {
            throw new IllegalArgumentException("The employeeList (" + employeeList + ") must not be null.");
        }
        return getEmployeeRosterView(tenantId, startDate, endDate, employeeList);
    }

    private EmployeeRosterView getEmployeeRosterView(final Integer tenantId,
                                                     final LocalDate startDate,
                                                     final LocalDate endDate,
                                                     final Pagination pagination) {

        final List<Employee> employeeList = entityManager.createNamedQuery("Employee.findAll", Employee.class)
                .setParameter("tenantId", tenantId)
                .setMaxResults(pagination.getNumberOfItemsPerPage())
                .setFirstResult(pagination.getFirstResultIndex())
                .getResultList();

        return getEmployeeRosterView(tenantId, startDate, endDate, employeeList);
    }

    @Transactional
    protected EmployeeRosterView getEmployeeRosterView(Integer tenantId, LocalDate startDate, LocalDate endDate,
                                                       List<Employee> employeeList) {
        EmployeeRosterView employeeRosterView = new EmployeeRosterView(tenantId, startDate, endDate);
        List<Spot> spotList = entityManager.createNamedQuery("Spot.findAll", Spot.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
        employeeRosterView.setSpotList(spotList);

        employeeRosterView.setEmployeeList(employeeList);

        Map<Long, List<ShiftView>> employeeIdToShiftViewListMap = new LinkedHashMap<>(employeeList.size());
        List<ShiftView> unassignedShiftViewList = new ArrayList<>();
        Set<Employee> employeeSet = new HashSet<>(employeeList);
        TenantConfiguration tenantConfig = entityManager.createNamedQuery("TenantConfiguration.find",
                TenantConfiguration.class)
                .setParameter("tenantId", tenantId).getSingleResult();

        List<Shift> shiftList = entityManager.createNamedQuery("Shift.filterWithEmployees", Shift.class)
                .setParameter("tenantId", tenantId)
                .setParameter("startDateTime", startDate.atStartOfDay(tenantConfig.getTimeZone()).toOffsetDateTime())
                .setParameter("endDateTime", endDate.atStartOfDay(tenantConfig.getTimeZone()).toOffsetDateTime())
                .setParameter("employeeSet", employeeSet)
                .getResultList();

        for (Shift shift : shiftList) {
            if (shift.getEmployee() != null) {
                employeeIdToShiftViewListMap.computeIfAbsent(shift.getEmployee().getId(),
                        k -> new ArrayList<>())
                        .add(new ShiftView(shift));
            } else {
                unassignedShiftViewList.add(new ShiftView(shift));
            }
        }
        employeeRosterView.setEmployeeIdToShiftViewListMap(employeeIdToShiftViewListMap);
        employeeRosterView.setUnassignedShiftViewList(unassignedShiftViewList);
        Map<Long, List<EmployeeAvailabilityView>> employeeIdToAvailabilityViewListMap = new LinkedHashMap<>(
                employeeList.size());
        List<EmployeeAvailability> employeeAvailabilityList = entityManager.createNamedQuery(
                "EmployeeAvailability.filterWithEmployee", EmployeeAvailability.class)
                .setParameter("tenantId", tenantId)
                .setParameter("startDateTime", startDate.atStartOfDay(tenantConfig.getTimeZone()).toOffsetDateTime())
                .setParameter("endDateTime", endDate.atStartOfDay(tenantConfig.getTimeZone()).toOffsetDateTime())
                .setParameter("employeeSet", employeeSet)
                .getResultList();
        for (EmployeeAvailability employeeAvailability : employeeAvailabilityList) {
            employeeIdToAvailabilityViewListMap.computeIfAbsent(employeeAvailability.getEmployee().getId(),
                    k -> new ArrayList<>())
                    .add(new EmployeeAvailabilityView(employeeAvailability));
        }
        employeeRosterView.setEmployeeIdToAvailabilityViewListMap(employeeIdToAvailabilityViewListMap);

        // TODO FIXME race condition solverManager's bestSolution might differ from the one we just fetched,
        // so the score might be inaccurate.
        Solver<Roster> solver = solverManager.getSolver(tenantId);
        Roster roster = (solver != null) ? solver.getBestSolution() : null;
        employeeRosterView.setScore(roster == null ? null : roster.getScore());
        employeeRosterView.setRosterState(getRosterState(tenantId));
        return employeeRosterView;
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

        // TODO fill in the score too - do we inject a ScoreDirectorFactory?
        return new Roster((long) tenantId, tenantId,
                skillList, spotList, employeeList, employeeAvailabilityList,
                tenantRestService.getTenantConfiguration(tenantId), getRosterState(tenantId), shiftList);
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
    @Transactional
    public void publishAndProvision(Integer tenantId) {
        TenantConfiguration tenantConfiguration = tenantRestService.getTenantConfiguration(tenantId);
        RosterState rosterState = getRosterState(tenantId);
        LocalDate firstUnplannedDate = rosterState.getFirstUnplannedDate();
        // Publish
        rosterState.setFirstDraftDate(rosterState.getFirstDraftDate().plusDays(rosterState.getPublishLength()));
        // Provision
        List<ShiftTemplate> shiftTemplateList = shiftRestService.getShiftTemplateList(tenantId);
        Map<Integer, List<ShiftTemplate>> dayOffsetToShiftTemplateListMap = shiftTemplateList.stream()
                .collect(groupingBy(ShiftTemplate::getStartDayOffset));
        int dayOffset = rosterState.getUnplannedRotationOffset();
        LocalDate shiftDate = firstUnplannedDate;
        for (int i = 0; i < rosterState.getPublishLength(); i++) {
            List<ShiftTemplate> dayShiftTemplateList = dayOffsetToShiftTemplateListMap.get(dayOffset);
            for (ShiftTemplate shiftTemplate : dayShiftTemplateList) {
                Shift shift = shiftTemplate.createShiftOnDate(shiftDate, rosterState.getRotationLength(), tenantConfiguration.getTimeZone(), false);
                entityManager.persist(shift);
            }
            shiftDate = shiftDate.plusDays(1);
            dayOffset = (dayOffset + 1) % rosterState.getRotationLength();
        }
        rosterState.setUnplannedRotationOffset(dayOffset);
    }

    @Override
    public RosterState getRosterState(Integer tenantId) {
        return entityManager.createNamedQuery("RosterState.find", RosterState.class)
                .setParameter("tenantId", tenantId)
                .getSingleResult();
    }

    @Override
    public Map<Long, IndictmentView> getCurrentShiftIndictmentMap(Integer tenantId, Integer pageNumber, Integer numberOfItemsPerPage) {
        final Pagination pagination = Pagination.of(pageNumber, numberOfItemsPerPage);
        final Set<Spot> spots = new HashSet<>(entityManager.createNamedQuery("Spot.findAll", Spot.class)
                .setParameter("tenantId", tenantId)
                .setMaxResults(pagination.getNumberOfItemsPerPage())
                .setFirstResult(pagination.getFirstResultIndex())
                .getResultList());

        try {
            final Map<Shift, IndictmentView> shiftIndictmentMap = getShiftIndictmentMap(tenantId);
            return shiftIndictmentMap.entrySet().stream()
                    .filter((e) -> spots.contains(e.getKey().getSpot()))
                    .collect(Collectors.toMap((e) -> e.getKey().getId(),
                            (e) -> e.getValue()));
        } catch (OutOfDateException e) {
            // Since there is no way to pass this exception to client-side...
            return null;
        }
    }

    @Override
    public Map<Long, IndictmentView> getCurrentEmployeeIndictmentMap(Integer tenantId, Integer pageNumber, Integer numberOfItemsPerPage) {
        final Pagination pagination = Pagination.of(pageNumber, numberOfItemsPerPage);
        final Set<Employee> employees = new HashSet<>(entityManager.createNamedQuery("Employee.findAll", Employee.class)
                .setParameter("tenantId", tenantId)
                .setMaxResults(pagination.getNumberOfItemsPerPage())
                .setFirstResult(pagination.getFirstResultIndex())
                .getResultList());

        try {
            final Map<Employee, IndictmentView> employeeIndictmentMap = getEmployeeIndictmentMap(tenantId);
            return employeeIndictmentMap.entrySet().stream()
                    .filter((e) -> employees.contains(e.getKey()))
                    .collect(Collectors.toMap((e) -> e.getKey().getId(),
                            (e) -> e.getValue()));
        } catch (OutOfDateException e) {
            // Since there is no way to pass this exception to client-side...
            return null;
        }
    }

    @Override
    public Map<Long, IndictmentView> getLatestShiftIndictmentMap(Integer tenantId, Integer pageNumber, Integer numberOfItemsPerPage) {
        final Pagination pagination = Pagination.of(pageNumber, numberOfItemsPerPage);
        final Set<Spot> spots = new HashSet<>(entityManager.createNamedQuery("Spot.findAll", Spot.class)
                .setParameter("tenantId", tenantId)
                .setMaxResults(pagination.getNumberOfItemsPerPage())
                .setFirstResult(pagination.getFirstResultIndex())
                .getResultList());

        final Map<Shift, IndictmentView> shiftIndictmentMap = tenantIdToShiftIndictmentMap.getOrDefault(tenantId, Collections.emptyMap());
        return shiftIndictmentMap.entrySet().stream()
                .filter((e) -> spots.contains(e.getKey().getSpot()))
                .collect(Collectors.toMap((e) -> e.getKey().getId(),
                        (e) -> e.getValue()));
    }

    @Override
    public Map<Long, IndictmentView> getLatestEmployeeIndictmentMap(Integer tenantId, Integer pageNumber, Integer numberOfItemsPerPage) {
        final Pagination pagination = Pagination.of(pageNumber, numberOfItemsPerPage);
        final Set<Employee> employees = new HashSet<>(entityManager.createNamedQuery("Employee.findAll", Employee.class)
                .setParameter("tenantId", tenantId)
                .setMaxResults(pagination.getNumberOfItemsPerPage())
                .setFirstResult(pagination.getFirstResultIndex())
                .getResultList());

        final Map<Employee, IndictmentView> employeeIndictmentMap = tenantIdToEmployeeIndictmentMap.getOrDefault(tenantId, Collections.emptyMap());
        return employeeIndictmentMap.entrySet().stream()
                .filter((e) -> employees.contains(e.getKey()))
                .collect(Collectors.toMap((e) -> e.getKey().getId(),
                        (e) -> e.getValue()));
    }

    private Map<Shift, IndictmentView> getShiftIndictmentMap(int tenantId) {
        Solver<Roster> solver = solverManager.getSolver(tenantId);
        if (null != solver) {
            Roster roster = solver.getBestSolution();
            try (ScoreDirector<Roster> scoreDirector = solver.getScoreDirectorFactory()
                    .buildScoreDirector()) {
                scoreDirector.setWorkingSolution(roster);
                Map<Shift, IndictmentView> indictmentMap = new HashMap<>();
                scoreDirector.getIndictmentMap().forEach((k, v) -> {
                    if (k instanceof Shift) {
                        indictmentMap.put((Shift) k, new IndictmentView(v));
                    }
                });
                tenantIdToShiftIndictmentMap.put(tenantId, indictmentMap);
                tenantIdToShiftIndictmentMapUpdateTime.put(tenantId, getLastUpdateTime(tenantId));
                return indictmentMap;
            }
        } else {
            List<List<Long>> lastUpdateTime = getLastUpdateTime(tenantId);
            List<List<Long>> lastMapUpdateTime = tenantIdToShiftIndictmentMapUpdateTime.getOrDefault(tenantId, Collections.emptyList());
            if (lastUpdateTime.equals(lastMapUpdateTime)) {
                return tenantIdToShiftIndictmentMap.get(tenantId);
            }
            throw new OutOfDateException(tenantIdToShiftIndictmentMap.get(tenantId));
        }
    }

    private Map<Employee, IndictmentView> getEmployeeIndictmentMap(int tenantId) {
        Solver<Roster> solver = solverManager.getSolver(tenantId);
        if (null != solver) {
            Roster roster = solver.getBestSolution();
            try (ScoreDirector<Roster> scoreDirector = solver.getScoreDirectorFactory()
                    .buildScoreDirector()) {
                scoreDirector.setWorkingSolution(roster);
                Map<Employee, IndictmentView> indictmentMap = new HashMap<>();
                scoreDirector.getIndictmentMap().forEach((k, v) -> {
                    if (k instanceof Employee) {
                        indictmentMap.put((Employee) k, new IndictmentView(v));
                    }
                });
                tenantIdToEmployeeIndictmentMap.put(tenantId, indictmentMap);
                tenantIdToEmployeeIndictmentMapUpdateTime.put(tenantId, getLastUpdateTime(tenantId));
                return indictmentMap;
            }
        } else {
            List<List<Long>> lastUpdateTime = getLastUpdateTime(tenantId);
            List<List<Long>> lastMapUpdateTime = tenantIdToEmployeeIndictmentMapUpdateTime.getOrDefault(tenantId, Collections.emptyList());
            if (lastUpdateTime.equals(lastMapUpdateTime)) {
                return tenantIdToEmployeeIndictmentMap.get(tenantId);
            }
            throw new OutOfDateException(tenantIdToEmployeeIndictmentMap.get(tenantId));
        }
    }

    private List<List<Long>> getLastUpdateTime(int tenantId) {
        return Arrays.asList(super.getTableCountAndVersionSum(tenantId, entityManager, Shift.class),
                super.getTableCountAndVersionSum(tenantId, entityManager, EmployeeAvailability.class));
    }
}
