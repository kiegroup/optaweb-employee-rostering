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

package org.optaweb.employeerostering.service.rotation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.rotation.ShiftTemplate;
import org.optaweb.employeerostering.domain.rotation.view.ShiftTemplateView;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.service.common.AbstractRestService;
import org.optaweb.employeerostering.service.employee.EmployeeService;
import org.optaweb.employeerostering.service.roster.RosterService;
import org.optaweb.employeerostering.service.spot.SpotService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Service
public class RotationService extends AbstractRestService {

    private final ShiftTemplateRepository shiftTemplateRepository;
    private final RosterService rosterService;
    private final SpotService spotService;
    private final EmployeeService employeeService;

    public RotationService(ShiftTemplateRepository shiftTemplateRepository, RosterService rosterService,
                           SpotService spotService, EmployeeService employeeService) {
        this.shiftTemplateRepository = shiftTemplateRepository;

        this.rosterService = rosterService;
        Assert.notNull(rosterService, "rosterService must not be null.");

        this.spotService = spotService;
        Assert.notNull(spotService, "spotService must not be null.");

        this.employeeService = employeeService;
        Assert.notNull(employeeService, "employeeService must not be null.");
    }

    @Transactional
    public List<ShiftTemplateView> getShiftTemplateList(Integer tenantId) {
        RosterState rosterState = rosterService.getRosterState(tenantId);
        return shiftTemplateRepository.findAllByTenantId(tenantId)
                .stream()
                .map(st -> new ShiftTemplateView(rosterState.getRotationLength(), st))
                .collect(Collectors.toList());
    }

    @Transactional
    public ShiftTemplateView getShiftTemplate(Integer tenantId, Long id) {
        RosterState rosterState = rosterService.getRosterState(tenantId);
        ShiftTemplate shiftTemplate = shiftTemplateRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No ShiftTemplate entity found with ID (" + id + ")."));

        validateTenantIdParameter(tenantId, shiftTemplate);
        return new ShiftTemplateView(rosterState.getRotationLength(), shiftTemplate);
    }

    @Transactional
    public ShiftTemplateView createShiftTemplate(Integer tenantId, ShiftTemplateView shiftTemplateView) {
        RosterState rosterState = rosterService.getRosterState(tenantId);
        Spot spot = spotService.getSpot(tenantId, shiftTemplateView.getSpotId());
        Employee employee;

        if (shiftTemplateView.getRotationEmployeeId() != null) {
            employee = employeeService.getEmployee(tenantId, shiftTemplateView.getRotationEmployeeId());
        } else {
            employee = null;
        }

        ShiftTemplate shiftTemplate = new ShiftTemplate(rosterState.getRotationLength(), shiftTemplateView, spot,
                                                        employee);
        validateTenantIdParameter(tenantId, shiftTemplate);
        shiftTemplateRepository.save(shiftTemplate);
        return new ShiftTemplateView(rosterState.getRotationLength(), shiftTemplate);
    }

    @Transactional
    public ShiftTemplateView updateShiftTemplate(Integer tenantId, ShiftTemplateView shiftTemplateView) {
        RosterState rosterState = rosterService.getRosterState(tenantId);
        Spot spot = spotService.getSpot(tenantId, shiftTemplateView.getSpotId());
        Employee employee;

        if (shiftTemplateView.getRotationEmployeeId() != null) {
            employee = employeeService.getEmployee(tenantId, shiftTemplateView.getRotationEmployeeId());
        } else {
            employee = null;
        }

        ShiftTemplate newShiftTemplate = new ShiftTemplate(rosterState.getRotationLength(), shiftTemplateView, spot,
                                                           employee);
        validateTenantIdParameter(tenantId, newShiftTemplate);

        ShiftTemplate oldShiftTemplate = shiftTemplateRepository
                .findById(newShiftTemplate.getId())
                .orElseThrow(() -> new EntityNotFoundException("ShiftTemplate entity with ID (" +
                                                                       newShiftTemplate.getId() + ") not found."));

        if (!oldShiftTemplate.getTenantId().equals(newShiftTemplate.getTenantId())) {
            throw new IllegalStateException("ShiftTemplate entity with tenantId (" + oldShiftTemplate.getTenantId() +
                                                    ") cannot change tenants.");
        }

        oldShiftTemplate.setRotationEmployee(employee);
        oldShiftTemplate.setSpot(spot);
        oldShiftTemplate.setStartDayOffset(newShiftTemplate.getStartDayOffset());
        oldShiftTemplate.setEndDayOffset(newShiftTemplate.getEndDayOffset());
        oldShiftTemplate.setStartTime(newShiftTemplate.getStartTime());
        oldShiftTemplate.setEndTime(newShiftTemplate.getEndTime());

        // Flush to increase version number before we duplicate it to ShiftTemplateView
        ShiftTemplate updatedShiftTemplate = shiftTemplateRepository.saveAndFlush(oldShiftTemplate);

        return new ShiftTemplateView(rosterState.getRotationLength(), updatedShiftTemplate);
    }

    @Transactional
    public Boolean deleteShiftTemplate(Integer tenantId, Long id) {
        Optional<ShiftTemplate> shiftTemplateOptional = shiftTemplateRepository.findById(id);

        if (!shiftTemplateOptional.isPresent()) {
            return false;
        }

        validateTenantIdParameter(tenantId, shiftTemplateOptional.get());
        shiftTemplateRepository.deleteById(id);
        return true;
    }
}
