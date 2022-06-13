package org.optaweb.employeerostering.service.tenant;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.Validator;

import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.roster.view.RosterStateView;
import org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.domain.tenant.view.RosterConstraintConfigurationView;
import org.optaweb.employeerostering.service.common.AbstractRestService;
import org.optaweb.employeerostering.service.employee.EmployeeAvailabilityRepository;
import org.optaweb.employeerostering.service.employee.EmployeeRepository;
import org.optaweb.employeerostering.service.roster.RosterStateRepository;
import org.optaweb.employeerostering.service.rotation.TimeBucketRepository;
import org.optaweb.employeerostering.service.shift.ShiftRepository;
import org.optaweb.employeerostering.service.skill.SkillRepository;
import org.optaweb.employeerostering.service.spot.SpotRepository;

@ApplicationScoped
public class TenantService extends AbstractRestService {

    TenantRepository tenantRepository;

    RosterConstraintConfigurationRepository rosterConstraintConfigurationRepository;

    RosterStateRepository rosterStateRepository;

    ShiftRepository shiftRepository;

    EmployeeAvailabilityRepository employeeAvailabilityRepository;

    TimeBucketRepository timeBucketRepository;

    EmployeeRepository employeeRepository;

    SpotRepository spotRepository;

    SkillRepository skillRepository;

    @Inject
    public TenantService(Validator validator,
            TenantRepository tenantRepository,
            RosterConstraintConfigurationRepository rosterConstraintConfigurationRepository,
            RosterStateRepository rosterStateRepository,
            ShiftRepository shiftRepository,
            EmployeeAvailabilityRepository employeeAvailabilityRepository,
            TimeBucketRepository timeBucketRepository,
            EmployeeRepository employeeRepository,
            SpotRepository spotRepository,
            SkillRepository skillRepository) {
        super(validator);
        this.tenantRepository = tenantRepository;
        this.rosterConstraintConfigurationRepository = rosterConstraintConfigurationRepository;
        this.rosterStateRepository = rosterStateRepository;
        this.shiftRepository = shiftRepository;
        this.employeeAvailabilityRepository = employeeAvailabilityRepository;
        this.timeBucketRepository = timeBucketRepository;
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
        return tenantRepository.findAllTenants();
    }

    @Transactional
    public Tenant getTenant(Integer id) {
        return tenantRepository
                .findByIdOptional(id)
                .orElseThrow(() -> new EntityNotFoundException("No Tenant entity found with ID (" + id + ")."));
    }

    @Transactional
    public Tenant createTenant(RosterStateView initialRosterStateView) {
        RosterState initialRosterState = convertFromRosterStateView(initialRosterStateView);

        Tenant databaseTenant = initialRosterState.getTenant();
        tenantRepository.persist(databaseTenant);
        initialRosterState.setTenant(databaseTenant);
        initialRosterState.setTenantId(databaseTenant.getId());

        RosterConstraintConfiguration rosterConstraintConfiguration = new RosterConstraintConfiguration();
        rosterConstraintConfiguration.setTenantId(databaseTenant.getId());

        rosterStateRepository.persist(initialRosterState);
        rosterConstraintConfigurationRepository.persist(rosterConstraintConfiguration);
        return databaseTenant;
    }

    @Transactional
    public Boolean deleteTenant(Integer id) {
        // Dependency order: Shift, EmployeeAvailability, ShiftTemplate,
        // Employee, Spot, Skill,
        // RosterConstraintConfiguration, RosterState

        shiftRepository.deleteForTenant(id);
        employeeAvailabilityRepository.deleteForTenant(id);
        timeBucketRepository.deleteForTenant(id);
        employeeRepository.deleteForTenant(id);
        spotRepository.deleteForTenant(id);
        skillRepository.deleteForTenant(id);
        rosterConstraintConfigurationRepository.deleteForTenant(id);
        rosterStateRepository.deleteForTenant(id);
        tenantRepository.delete("id", id);
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
        rosterConstraintConfigurationRepository.persist(oldRosterConstraintConfiguration);
        return oldRosterConstraintConfiguration;
    }

    public List<ZoneId> getSupportedTimezones() {
        return ZoneId.getAvailableZoneIds().stream()
                .sorted().map(ZoneId::of)
                .collect(Collectors.toList());
    }
}
