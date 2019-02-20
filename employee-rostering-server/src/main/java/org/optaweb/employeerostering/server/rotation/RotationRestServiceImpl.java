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

package org.optaweb.employeerostering.server.rotation;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.optaweb.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeRestService;
import org.optaweb.employeerostering.shared.roster.RosterRestService;
import org.optaweb.employeerostering.shared.roster.RosterState;
import org.optaweb.employeerostering.shared.rotation.RotationRestService;
import org.optaweb.employeerostering.shared.rotation.ShiftTemplate;
import org.optaweb.employeerostering.shared.rotation.view.ShiftTemplateView;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.spot.SpotRestService;

public class RotationRestServiceImpl extends AbstractRestServiceImpl
        implements
        RotationRestService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private RosterRestService rosterRestService;

    @Inject
    private SpotRestService spotRestService;

    @Inject
    private EmployeeRestService employeeRestService;

    @Override
    public List<ShiftTemplateView> getShiftTemplateList(Integer tenantId) {
        RosterState rosterState = rosterRestService.getRosterState(tenantId);
        return entityManager.createNamedQuery("ShiftTemplate.findAll", ShiftTemplate.class)
                .setParameter("tenantId", tenantId)
                .getResultList()
                .stream()
                .map(st -> new ShiftTemplateView(rosterState.getRotationLength(), st))
                .collect(Collectors.toList());
    }

    @Override
    public ShiftTemplateView getShiftTemplate(Integer tenantId, Long id) {
        RosterState rosterState = rosterRestService.getRosterState(tenantId);
        ShiftTemplate shiftTemplate = entityManager.find(ShiftTemplate.class, id);
        if (shiftTemplate == null) {
            throw new EntityNotFoundException("No ShiftTemplate entity found with ID (" + id + ").");
        }
        validateTenantIdParameter(tenantId, shiftTemplate);
        return new ShiftTemplateView(rosterState.getRotationLength(), shiftTemplate);
    }

    @Override
    @Transactional
    public ShiftTemplateView addShiftTemplate(Integer tenantId, ShiftTemplateView shiftTemplateView) {
        RosterState rosterState = rosterRestService.getRosterState(tenantId);
        Spot spot = spotRestService.getSpot(tenantId, shiftTemplateView.getSpotId());
        Employee employee;
        if (shiftTemplateView.getRotationEmployeeId() != null) {
            employee = employeeRestService.getEmployee(tenantId, shiftTemplateView.getRotationEmployeeId());
        } else {
            employee = null;
        }
        ShiftTemplate shiftTemplate = new ShiftTemplate(rosterState.getRotationLength(), shiftTemplateView, spot, employee);
        validateTenantIdParameter(tenantId, shiftTemplate);
        entityManager.persist(shiftTemplate);
        return new ShiftTemplateView(rosterState.getRotationLength(), shiftTemplate);
    }

    @Override
    @Transactional
    public ShiftTemplateView updateShiftTemplate(Integer tenantId, ShiftTemplateView shiftTemplateView) {
        RosterState rosterState = rosterRestService.getRosterState(tenantId);
        Spot spot = spotRestService.getSpot(tenantId, shiftTemplateView.getSpotId());
        Employee employee;
        if (shiftTemplateView.getRotationEmployeeId() != null) {
            employee = employeeRestService.getEmployee(tenantId, shiftTemplateView.getRotationEmployeeId());
        } else {
            employee = null;
        }
        ShiftTemplate shiftTemplate = new ShiftTemplate(rosterState.getRotationLength(), shiftTemplateView, spot, employee);
        validateTenantIdParameter(tenantId, shiftTemplate);
        shiftTemplate = entityManager.merge(shiftTemplate);
        // Flush to increase version number before we duplicate it to ShiftTemplateView
        entityManager.flush();

        return new ShiftTemplateView(rosterState.getRotationLength(), shiftTemplate);
    }

    @Override
    @Transactional
    public Boolean removeShiftTemplate(Integer tenantId, Long id) {
        ShiftTemplate shiftTemplate = entityManager.find(ShiftTemplate.class, id);
        if (shiftTemplate == null) {
            return false;
        }
        validateTenantIdParameter(tenantId, shiftTemplate);
        entityManager.remove(shiftTemplate);
        return true;
    }
}
