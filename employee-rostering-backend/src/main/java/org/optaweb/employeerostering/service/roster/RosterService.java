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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.roster.Pagination;
import org.optaweb.employeerostering.domain.roster.Roster;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.roster.view.ShiftRosterView;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.service.common.AbstractRestService;
import org.optaweb.employeerostering.service.common.IndictmentUtils;
import org.optaweb.employeerostering.service.employee.EmployeeAvailabilityRepository;
import org.optaweb.employeerostering.service.employee.EmployeeRepository;
import org.optaweb.employeerostering.service.shift.ShiftRepository;
import org.optaweb.employeerostering.service.skill.SkillRepository;
import org.optaweb.employeerostering.service.solver.WannabeSolverManager;
import org.optaweb.employeerostering.service.spot.SpotRepository;
import org.optaweb.employeerostering.service.tenant.RosterParametrizationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RosterService extends AbstractRestService {

    private RosterStateRepository rosterStateRepository;
    private SkillRepository skillRepository;
    private SpotRepository spotRepository;
    private EmployeeRepository employeeRepository;
    private EmployeeAvailabilityRepository employeeAvailabilityRepository;
    private ShiftRepository shiftRepository;
    private RosterParametrizationRepository rosterParametrizationRepository;
    private WannabeSolverManager solverManager;
    private IndictmentUtils indictmentUtils;

    public RosterService(RosterStateRepository rosterStateRepository, SkillRepository skillRepository,
                         SpotRepository spotRepository, EmployeeRepository employeeRepository,
                         EmployeeAvailabilityRepository employeeAvailabilityRepository,
                         ShiftRepository shiftRepository,
                         RosterParametrizationRepository rosterParametrizationRepository,
                         WannabeSolverManager solverManager, IndictmentUtils indictmentUtils) {
        this.rosterStateRepository = rosterStateRepository;
        this.skillRepository = skillRepository;
        this.spotRepository = spotRepository;
        this.employeeRepository = employeeRepository;
        this.employeeAvailabilityRepository = employeeAvailabilityRepository;
        this.shiftRepository = shiftRepository;
        this.rosterParametrizationRepository = rosterParametrizationRepository;
        this.solverManager = solverManager;
        this.indictmentUtils = indictmentUtils;
    }

    // ************************************************************************
    // RosterState
    // ************************************************************************

    @Transactional
    public RosterState getRosterState(Integer tenantId) {
        RosterState rosterState = rosterStateRepository
                .findByTenantId(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("No RosterState entity found with tenantId (" +
                                                                       tenantId + ")."));
        validateTenantIdParameter(tenantId, rosterState);
        return rosterState;
    }

    // ************************************************************************
    // ShiftRosterView
    // ************************************************************************

    @Transactional
    public ShiftRosterView getCurrentShiftRosterView(Integer tenantId, Integer pageNumber,
                                                     Integer numberOfItemsPerPage) {
        RosterState rosterState = getRosterState(tenantId);
        LocalDate startDate = rosterState.getFirstPublishedDate();
        LocalDate endDate = rosterState.getFirstUnplannedDate();
        return getShiftRosterView(tenantId, startDate, endDate, Pagination.of(pageNumber, numberOfItemsPerPage));
    }

    @Transactional
    public ShiftRosterView getShiftRosterView(final Integer tenantId, Integer pageNumber, Integer numberOfItemsPerPage,
                                              final String startDateString,
                                              final String endDateString) {

        return getShiftRosterView(tenantId, LocalDate.parse(startDateString), LocalDate.parse(endDateString),
                                  Pagination.of(pageNumber, numberOfItemsPerPage));
    }

    @Transactional
    private ShiftRosterView getShiftRosterView(final Integer tenantId,
                                               final LocalDate startDate,
                                               final LocalDate endDate,
                                               final Pagination pagination) {

        Pageable spotPage = PageRequest.of(pagination.getPageNumber(), pagination.getNumberOfItemsPerPage());
        final List<Spot> spots = spotRepository.findAllByTenantId(tenantId, spotPage);

        return getShiftRosterView(tenantId, startDate, endDate, spots);
    }

    @Transactional
    public ShiftRosterView getShiftRosterViewFor(Integer tenantId, String startDateString, String endDateString,
                                                 List<Spot> spots) {
        LocalDate startDate = LocalDate.parse(startDateString);
        LocalDate endDate = LocalDate.parse(endDateString);
        if (null == spots) {
            throw new IllegalArgumentException("spots is null!");
        }

        return getShiftRosterView(tenantId, startDate, endDate, spots);
    }

    @Transactional
    private ShiftRosterView getShiftRosterView(Integer tenantId, LocalDate startDate, LocalDate endDate,
                                               List<Spot> spotList) {
        ShiftRosterView shiftRosterView = new ShiftRosterView(tenantId, startDate, endDate);
        shiftRosterView.setSpotList(spotList);
        List<Employee> employeeList = employeeRepository.findAllByTenantId(tenantId);
        shiftRosterView.setEmployeeList(employeeList);

        Set<Spot> spotSet = new HashSet<>(spotList);
        ZoneId timeZone = getRosterState(tenantId).getTimeZone();

        List<Shift> shiftList = shiftRepository.filterWithSpots(tenantId, spotSet,
                                                                startDate.atStartOfDay(timeZone).toOffsetDateTime(),
                                                                endDate.atStartOfDay(timeZone).toOffsetDateTime());

        Map<Long, List<ShiftView>> spotIdToShiftViewListMap = new LinkedHashMap<>(spotList.size());
        // TODO FIXME race condition solverManager's bestSolution might differ from the one we just fetched, so the
        //  score might be inaccurate
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

    // TODO: Add getAvailabilityRosterView() methods once SolverManager and IndictmentUtils are added

    // ************************************************************************
    // Roster
    // ************************************************************************

    @Transactional
    public Roster buildRoster(Integer tenantId) {
        ZoneId zoneId = getRosterState(tenantId).getTimeZone();
        List<Skill> skillList = skillRepository.findAllByTenantId(tenantId);
        List<Spot> spotList = spotRepository.findAllByTenantId(tenantId, PageRequest.of(0, Integer.MAX_VALUE));
        List<Employee> employeeList = employeeRepository.findAllByTenantId(tenantId);
        List<EmployeeAvailability> employeeAvailabilityList = employeeAvailabilityRepository.findAllByTenantId(tenantId)
                .stream()
                .map(ea -> ea.inTimeZone(zoneId))
                .collect(Collectors.toList());
        List<Shift> shiftList = shiftRepository.findAllByTenantId(tenantId)
                .stream()
                .map(s -> s.inTimeZone(zoneId))
                .collect(Collectors.toList());

        // TODO fill in the score too - do we inject a ScoreDirectorFactory?
        return new Roster((long) tenantId, tenantId, skillList, spotList, employeeList, employeeAvailabilityList,
                          rosterParametrizationRepository.findByTenantId(tenantId).get(), getRosterState(tenantId),
                          shiftList);
    }

    @Transactional
    public void updateShiftsOfRoster(Roster newRoster) {
        Integer tenantId = newRoster.getTenantId();
        // TODO HACK avoids optimistic locking exception while solve(), but it circumvents optimistic locking completely
        Map<Long, Employee> employeeIdMap = employeeRepository.findAllByTenantId(tenantId)
                .stream()
                .collect(Collectors.toMap(Employee::getId, Function.identity()));

        Map<Long, Shift> shiftIdMap = shiftRepository.findAllByTenantId(tenantId)
                .stream()
                .collect(Collectors.toMap(Shift::getId, Function.identity()));

        for (Shift shift : newRoster.getShiftList()) {
            Shift attachedShift = shiftIdMap.get(shift.getId());
            if (attachedShift == null) {
                continue;
            }
            attachedShift.setEmployee((shift.getEmployee() == null) ?
                                              null : employeeIdMap.get(shift.getEmployee().getId()));
        }
    }

    // ************************************************************************
    // Solver methods
    // ************************************************************************

    // ************************************************************************
    // Publishing/Provisioning methods
    // ************************************************************************

    // TODO: Implement PublishAndProvision()
}
