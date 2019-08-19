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

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.roster.Roster;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.service.common.AbstractRestService;
import org.optaweb.employeerostering.service.employee.EmployeeAvailabilityRepository;
import org.optaweb.employeerostering.service.employee.EmployeeRepository;
import org.optaweb.employeerostering.service.shift.ShiftRepository;
import org.optaweb.employeerostering.service.skill.SkillRepository;
import org.optaweb.employeerostering.service.spot.SpotRepository;
import org.optaweb.employeerostering.service.tenant.RosterParametrizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RosterService extends AbstractRestService {

    private final RosterStateRepository rosterStateRepository;
    private final SkillRepository skillRepository;
    private final SpotRepository spotRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeAvailabilityRepository employeeAvailabilityRepository;
    private final ShiftRepository shiftRepository;
    private final RosterParametrizationRepository rosterParametrizationRepository;

    public RosterService(RosterStateRepository rosterStateRepository, SkillRepository skillRepository,
                         SpotRepository spotRepository, EmployeeRepository employeeRepository,
                         EmployeeAvailabilityRepository employeeAvailabilityRepository,
                         ShiftRepository shiftRepository,
                         RosterParametrizationRepository rosterParametrizationRepository) {
        this.rosterStateRepository = rosterStateRepository;
        this.skillRepository = skillRepository;
        this.spotRepository = spotRepository;
        this.employeeRepository = employeeRepository;
        this.employeeAvailabilityRepository = employeeAvailabilityRepository;
        this.shiftRepository = shiftRepository;
        this.rosterParametrizationRepository = rosterParametrizationRepository;
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

    // TODO: Add getShiftRosterView() methods once SolverManager and IndictmentUtils are added

    // ************************************************************************
    // AvailabilityRosterView
    // ************************************************************************

    // TODO: Add getAvailabilityRosterView() methods once SolverManager and IndictmentUtils are added

    // ************************************************************************
    // Other
    // ************************************************************************

    @Transactional
    public Roster buildRoster(Integer tenantId) {
        ZoneId zoneId = getRosterState(tenantId).getTimeZone();
        List<Skill> skillList = skillRepository.findAllByTenantId(tenantId);
        List<Spot> spotList = spotRepository.findAllByTenantId(tenantId);
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
}
