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

package org.optaplanner.openshift.employeerostering.server.shift;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.optaplanner.openshift.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaplanner.openshift.employeerostering.server.lang.parser.ShiftFileParser;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;
import org.optaplanner.openshift.employeerostering.shared.rotation.ShiftTemplate;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestService;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantConfiguration;

public class ShiftRestServiceImpl extends AbstractRestServiceImpl implements ShiftRestService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private ShiftFileParser shiftGenerator;

    @Override
    @Transactional
    public ShiftView getShift(Integer tenantId, Long id) {
        Shift shift = entityManager.find(Shift.class, id);
        validateTenantIdParameter(tenantId, shift);
        return new ShiftView(shift);
    }

    @Override
    @Transactional
    public Long addShift(Integer tenantId, ShiftView shiftView) {
        Shift shift = convertFromView(tenantId, shiftView);
        entityManager.persist(shift);
        return shift.getId();
    }

    @Override
    @Transactional
    public Shift updateShift(Integer tenantId, ShiftView shiftView) {
        Shift shift = convertFromView(tenantId, shiftView);
        return entityManager.merge(shift);
    }

    private Shift convertFromView(Integer tenantId, ShiftView shiftView) {
        validateTenantIdParameter(tenantId, shiftView);
        Spot spot = entityManager.find(Spot.class, shiftView.getSpotId());
        validateTenantIdParameter(tenantId, spot);

        Long rotationEmployeeId = shiftView.getRotationEmployeeId();
        Employee rotationEmployee = null;
        if (rotationEmployeeId != null) {
            rotationEmployee = entityManager.find(Employee.class, rotationEmployeeId);
            if (rotationEmployee == null) {
                throw new IllegalArgumentException("ShiftView (" + shiftView
                                                           + ") has an non-existing employeeId (" + rotationEmployeeId + ").");
            }
            validateTenantIdParameter(tenantId, rotationEmployee);
        }

        Shift shift = new Shift(shiftView, spot, shiftView.getStartDateTime(), shiftView.getEndDateTime(),
                rotationEmployee);
        shift.setPinnedByUser(shiftView.isLockedByUser());
        Long employeeId = shiftView.getEmployeeId();
        if (employeeId != null) {
            Employee employee = entityManager.find(Employee.class, employeeId);
            if (employee == null) {
                throw new IllegalArgumentException("ShiftView (" + shiftView
                                                           + ") has an non-existing employeeId (" + employeeId + ").");
            }
            validateTenantIdParameter(tenantId, employee);
            shift.setEmployee(employee);
        }
        return shift;
    }

    @Override
    @Transactional
    public Boolean removeShift(Integer tenantId, Long id) {
        Shift shift = entityManager.find(Shift.class, id);
        if (shift == null) {
            return false;
        }
        validateTenantIdParameter(tenantId, shift);
        entityManager.remove(shift);
        return true;
    }

    @Override
    @Transactional
    public List<Long> addShiftsFromTemplate(Integer tenantId, Integer lengthInDays) {
        Collection<ShiftTemplate> shiftTemplates = getTemplate(tenantId);

        TenantConfiguration tenantConfiguration = entityManager.find(Tenant.class, tenantId).getConfiguration();
        ShiftFileParser.ParserOut parserOutput = shiftGenerator.parse(tenantId, tenantConfiguration, null,
                lengthInDays, shiftTemplates);

        List<Shift> shifts = parserOutput.getShiftOutputList();
        List<EmployeeAvailability> employeeAvailabilities = parserOutput.getEmployeeAvailabilityOutputList();
        List<Long> out = new ArrayList<>();
        for (Shift shift : shifts) {
            entityManager.persist(shift);
            out.add(shift.getId());
        }
        for (EmployeeAvailability availability : employeeAvailabilities) {
            entityManager.persist(availability);
        }
        return out;
    }

    @Override
    public List<ShiftView> getShifts(Integer tenantId) {
        return getAllShifts(tenantId).stream().map((s) -> new ShiftView(s)).collect(Collectors.toList());
    }

    private List<Shift> getAllShifts(Integer tenantId) {
        TypedQuery<Shift> q = entityManager.createNamedQuery("Shift.findAll", Shift.class);
        q.setParameter("tenantId", tenantId);
        return q.getResultList();
    }

    @Override
    public Collection<ShiftTemplate> getTemplate(Integer tenantId) {
        TypedQuery<ShiftTemplate> q = entityManager.createNamedQuery("ShiftTemplate.findAll", ShiftTemplate.class);
        q.setParameter("tenantId", tenantId);
        return q.getResultList();
    }

    @Override
    @Transactional
    public void updateTemplate(Integer tenantId, Collection<ShiftTemplate> shifts) {
        Collection<ShiftTemplate> oldShiftTemplates = getTemplate(tenantId);
        oldShiftTemplates.forEach((s) -> entityManager.remove(s));
        shifts.forEach((s) -> entityManager.persist(s));
    }
}
