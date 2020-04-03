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
import org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.domain.tenant.view.RosterConstraintConfigurationView;
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

    private final RosterConstraintConfigurationRepository rosterConstraintConfigurationRepository;

    private final RosterStateRepository rosterStateRepository;

    private final ShiftRepository shiftRepository;

    private final EmployeeAvailabilityRepository employeeAvailabilityRepository;

    private final ShiftTemplateRepository shiftTemplateRepository;

    private final EmployeeRepository employeeRepository;

    private final SpotRepository spotRepository;

    private final SkillRepository skillRepository;

    public TenantService(TenantRepository tenantRepository,
                         RosterConstraintConfigurationRepository rosterConstraintConfigurationRepository,
                         RosterStateRepository rosterStateRepository,
                         ShiftRepository shiftRepository,
                         EmployeeAvailabilityRepository employeeAvailabilityRepository,
                         ShiftTemplateRepository shiftTemplateRepository,
                         EmployeeRepository employeeRepository,
                         SpotRepository spotRepository,
                         SkillRepository skillRepository) {
        this.tenantRepository = tenantRepository;
        this.rosterConstraintConfigurationRepository = rosterConstraintConfigurationRepository;
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

        RosterConstraintConfiguration rosterConstraintConfiguration = new RosterConstraintConfiguration();
        rosterConstraintConfiguration.setTenantId(databaseTenant.getId());

        rosterStateRepository.save(initialRosterState);
        rosterConstraintConfigurationRepository.save(rosterConstraintConfiguration);
        return databaseTenant;
    }

    @Transactional
    public Boolean deleteTenant(Integer id) {
        // Dependency order: Shift, EmployeeAvailability, ShiftTemplate,
        // Employee, Spot, Skill,
        // RosterConstraintConfiguration, RosterState

        shiftRepository.deleteForTenant(id);
        employeeAvailabilityRepository.deleteForTenant(id);
        shiftTemplateRepository.deleteForTenant(id);
        employeeRepository.deleteForTenant(id);
        spotRepository.deleteForTenant(id);
        skillRepository.deleteForTenant(id);
        rosterConstraintConfigurationRepository.deleteForTenant(id);
        rosterStateRepository.deleteForTenant(id);
        tenantRepository.deleteById(id);
        return true;
    }

    // ************************************************************************
    // RosterConstraintConfiguration
    // ************************************************************************

    @Transactional
    public RosterConstraintConfiguration getRosterConstraintConfiguration(Integer tenantId) {
        return rosterConstraintConfigurationRepository
                .findByTenantId(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No RosterConstraintConfiguration entity found with tenantId (" + tenantId + ")."));
    }

    @Transactional
    public RosterConstraintConfiguration updateRosterConstraintConfiguration(
            RosterConstraintConfigurationView rosterConstraintConfigurationView) {
        RosterConstraintConfiguration oldRosterConstraintConfiguration = rosterConstraintConfigurationRepository
                .findByTenantId(rosterConstraintConfigurationView.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException("RosterConstraintConfiguration entity with tenantId (" +
                                                                       rosterConstraintConfigurationView.getTenantId() +
                                                                       ") not found."));
        if (!oldRosterConstraintConfiguration.getTenantId().equals(rosterConstraintConfigurationView.getTenantId())) {
            throw new IllegalStateException("RosterConstraintConfiguration entity with tenantId (" +
                                                    oldRosterConstraintConfiguration.getTenantId() +
                                                    ") cannot change tenants.");
        }

        oldRosterConstraintConfiguration.setInoculatedEmployeeOutsideCovidWardMatchWeight(
                rosterConstraintConfigurationView.getInoculatedEmployeeOutsideCovidWard());
        oldRosterConstraintConfiguration.setLowRiskEmployeeInCovidWardMatchWeight(
                rosterConstraintConfigurationView.getLowRiskEmployeeInCovidWard());
        oldRosterConstraintConfiguration.setModerateRiskEmployeeInCovidWardMatchWeight(
                rosterConstraintConfigurationView.getModerateRiskEmployeeInCovidWard());
        oldRosterConstraintConfiguration.setHighRiskEmployeeInCovidWardMatchWeight(
                rosterConstraintConfigurationView.getHighRiskEmployeeInCovidWard());
        oldRosterConstraintConfiguration.setExtremeRiskEmployeeInCovidWardMatchWeight(
                rosterConstraintConfigurationView.getExtremeRiskEmployeeInCovidWard());
        oldRosterConstraintConfiguration.setUniformDistributionOfInoculatedHoursMatchWeight(
                rosterConstraintConfigurationView.getUniformDistributionOfInoculated());
        oldRosterConstraintConfiguration.setMaximizeInoculatedHoursMatchWeight(
                rosterConstraintConfigurationView.getMaximizeInoculatedHours());
        oldRosterConstraintConfiguration.setMigrationBetweenCovidAndNonCovidWardMatchWeight(
                rosterConstraintConfigurationView.getMigrationBetweenCovidAndNonCovidWard());

        oldRosterConstraintConfiguration.setWeekStartDay(rosterConstraintConfigurationView.getWeekStartDay());
        oldRosterConstraintConfiguration.setRequiredSkill(rosterConstraintConfigurationView.getRequiredSkill());
        oldRosterConstraintConfiguration.setUnavailableTimeSlot(
                rosterConstraintConfigurationView.getUnavailableTimeSlot());
        oldRosterConstraintConfiguration.setNoOverlappingShifts(
                rosterConstraintConfigurationView.getNoOverlappingShifts());
        oldRosterConstraintConfiguration.setNoMoreThan2ConsecutiveShifts(
                rosterConstraintConfigurationView.getNoMoreThan2ConsecutiveShifts());
        oldRosterConstraintConfiguration.setBreakBetweenNonConsecutiveShiftsAtLeast10Hours(
                rosterConstraintConfigurationView.getBreakBetweenNonConsecutiveShiftsAtLeast10Hours());
        oldRosterConstraintConfiguration.setContractMaximumDailyMinutes(rosterConstraintConfigurationView
                                                                                .getContractMaximumDailyMinutes());
        oldRosterConstraintConfiguration.setContractMaximumWeeklyMinutes(rosterConstraintConfigurationView
                                                                                 .getContractMaximumWeeklyMinutes());
        oldRosterConstraintConfiguration.setContractMaximumMonthlyMinutes(rosterConstraintConfigurationView
                                                                                  .getContractMaximumMonthlyMinutes());
        oldRosterConstraintConfiguration.setContractMaximumYearlyMinutes(rosterConstraintConfigurationView
                                                                                 .getContractMaximumYearlyMinutes());

        oldRosterConstraintConfiguration.setAssignEveryShift(rosterConstraintConfigurationView
                                                                     .getAssignEveryShift());

        oldRosterConstraintConfiguration.setUndesiredTimeSlot(rosterConstraintConfigurationView
                                                                      .getUndesiredTimeSlot());
        oldRosterConstraintConfiguration.setDesiredTimeSlot(rosterConstraintConfigurationView
                                                                    .getDesiredTimeSlot());
        oldRosterConstraintConfiguration.setNotRotationEmployee(rosterConstraintConfigurationView
                                                                        .getNotRotationEmployee());
        return rosterConstraintConfigurationRepository.save(oldRosterConstraintConfiguration);
    }

    public List<ZoneId> getSupportedTimezones() {
        return ZoneId.getAvailableZoneIds().stream()
                .sorted().map(zoneId -> ZoneId.of(zoneId))
                .collect(Collectors.toList());
    }
}
