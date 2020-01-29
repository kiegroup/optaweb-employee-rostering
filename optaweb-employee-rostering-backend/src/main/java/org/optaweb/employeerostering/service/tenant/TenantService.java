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

package org.optaweb.employeerostering.service.tenant;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.roster.view.RosterStateView;
import org.optaweb.employeerostering.domain.tenant.RosterParametrization;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.domain.tenant.view.RosterParametrizationView;
import org.optaweb.employeerostering.service.common.AbstractRestService;
import org.optaweb.employeerostering.service.employee.EmployeeAvailabilityRepository;
import org.optaweb.employeerostering.service.employee.EmployeeRepository;
import org.optaweb.employeerostering.service.roster.RosterStateRepository;
import org.optaweb.employeerostering.service.rotation.ShiftTemplateRepository;
import org.optaweb.employeerostering.service.shift.ShiftRepository;
import org.optaweb.employeerostering.service.skill.SkillRepository;
import org.optaweb.employeerostering.service.spot.SpotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService extends AbstractRestService {

    private final TenantRepository tenantRepository;

    private final RosterParametrizationRepository rosterParametrizationRepository;

    private final RosterStateRepository rosterStateRepository;

    private final ShiftRepository shiftRepository;

    private final EmployeeAvailabilityRepository employeeAvailabilityRepository;

    private final ShiftTemplateRepository shiftTemplateRepository;

    private final EmployeeRepository employeeRepository;

    private final SpotRepository spotRepository;

    private final SkillRepository skillRepository;

    public TenantService(TenantRepository tenantRepository,
                         RosterParametrizationRepository rosterParametrizationRepository,
                         RosterStateRepository rosterStateRepository,
                         ShiftRepository shiftRepository,
                         EmployeeAvailabilityRepository employeeAvailabilityRepository,
                         ShiftTemplateRepository shiftTemplateRepository,
                         EmployeeRepository employeeRepository,
                         SpotRepository spotRepository,
                         SkillRepository skillRepository) {
        this.tenantRepository = tenantRepository;
        this.rosterParametrizationRepository = rosterParametrizationRepository;
        this.rosterStateRepository = rosterStateRepository;
        this.shiftRepository = shiftRepository;
        this.employeeAvailabilityRepository = employeeAvailabilityRepository;
        this.shiftTemplateRepository = shiftTemplateRepository;
        this.employeeRepository = employeeRepository;
        this.spotRepository = spotRepository;
        this.skillRepository = skillRepository;
    }

    // ************************************************************************
    // Tenant
    // ************************************************************************

    public RosterState convertFromRosterStateView(RosterStateView rosterStateView) {
        RosterState rosterState = new RosterState(rosterStateView.getTenantId(),
                                                  rosterStateView.getPublishNotice(),
                                                  rosterStateView.getFirstDraftDate(),
                                                  rosterStateView.getPublishLength(),
                                                  rosterStateView.getDraftLength(),
                                                  rosterStateView.getUnplannedRotationOffset(),
                                                  rosterStateView.getRotationLength(),
                                                  rosterStateView.getLastHistoricDate(),
                                                  rosterStateView.getTimeZone());
        rosterState.setTenant(rosterStateView.getTenant());
        return rosterState;
    }

    @Transactional
    public List<Tenant> getTenantList() {
        return tenantRepository.findAll();
    }

    @Transactional
    public Tenant getTenant(Integer id) {
        return tenantRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No Tenant entity found with ID (" + id + ")."));
    }

    @Transactional
    public Tenant createTenant(RosterStateView initialRosterStateView) {
        RosterState initialRosterState = convertFromRosterStateView(initialRosterStateView);

        Tenant databaseTenant = tenantRepository.save(initialRosterState.getTenant());
        initialRosterState.setTenant(databaseTenant);
        initialRosterState.setTenantId(databaseTenant.getId());

        RosterParametrization rosterParametrization = new RosterParametrization();
        rosterParametrization.setTenantId(databaseTenant.getId());

        rosterStateRepository.save(initialRosterState);
        rosterParametrizationRepository.save(rosterParametrization);
        return databaseTenant;
    }

    @Transactional
    public Boolean deleteTenant(Integer id) {
        // Dependency order: Shift, EmployeeAvailability, ShiftTemplate,
        // Employee, Spot, Skill,
        // RosterParametrization, RosterState

        shiftRepository.deleteForTenant(id);
        employeeAvailabilityRepository.deleteForTenant(id);
        shiftTemplateRepository.deleteForTenant(id);
        employeeRepository.deleteForTenant(id);
        spotRepository.deleteForTenant(id);
        skillRepository.deleteForTenant(id);
        rosterParametrizationRepository.deleteForTenant(id);
        rosterStateRepository.deleteForTenant(id);
        tenantRepository.deleteById(id);
        return true;
    }

    // ************************************************************************
    // RosterParametrization
    // ************************************************************************

    @Transactional
    public RosterParametrization getRosterParametrization(Integer tenantId) {
        return rosterParametrizationRepository
                .findByTenantId(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("No RosterParametrization entity found with tenantId ("
                                                                       + tenantId + ")."));
    }

    @Transactional
    public RosterParametrization updateRosterParametrization(RosterParametrizationView rosterParametrizationView) {
        RosterParametrization oldRosterParametrization = rosterParametrizationRepository
                .findByTenantId(rosterParametrizationView.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException("RosterParametrization entity with tenantId (" +
                                                                       rosterParametrizationView.getTenantId() +
                                                                       ") not found."));
        if (!oldRosterParametrization.getTenantId().equals(rosterParametrizationView.getTenantId())) {
            throw new IllegalStateException("RosterParametrization entity with tenantId (" +
                                                    oldRosterParametrization.getTenantId() +
                                                    ") cannot change tenants.");
        }

        oldRosterParametrization.setDesiredTimeSlotWeight(rosterParametrizationView.getDesiredTimeSlotWeight());
        oldRosterParametrization.setRotationEmployeeMatchWeight(
                rosterParametrizationView.getRotationEmployeeMatchWeight());
        oldRosterParametrization.setUndesiredTimeSlotWeight(
                rosterParametrizationView.getUndesiredTimeSlotWeight());
        oldRosterParametrization.setWeekStartDay(rosterParametrizationView.getWeekStartDay());
        return rosterParametrizationRepository.save(oldRosterParametrization);
    }

    public List<ZoneId> getSupportedTimezones() {
        return ZoneId.getAvailableZoneIds().stream()
                .sorted().map(zoneId -> ZoneId.of(zoneId))
                .collect(Collectors.toList());
    }
}
