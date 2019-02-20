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

package org.optaweb.employeerostering.server.shift;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaweb.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaweb.employeerostering.server.common.IndictmentUtils;
import org.optaweb.employeerostering.server.roster.RosterRestServiceImpl;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.shift.Shift;
import org.optaweb.employeerostering.shared.shift.ShiftRestService;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.spot.Spot;

public class ShiftRestServiceImpl extends AbstractRestServiceImpl
        implements
        ShiftRestService {

    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private RosterRestServiceImpl rosterRestService;
    @Inject
    private IndictmentUtils indictmentUtils;

    @Override
    @Transactional
    public ShiftView getShift(Integer tenantId, Long id) {
        Shift shift = entityManager.find(Shift.class, id);
        if (shift == null) {
            throw new EntityNotFoundException("No Shift entity found with ID (" + id + ").");
        }
        validateTenantIdParameter(tenantId, shift);
        Indictment indictment = indictmentUtils.getIndictmentMapForRoster(rosterRestService.buildRoster(tenantId)).get(shift);
        return indictmentUtils.getShiftViewWithIndictment(rosterRestService.getRosterState(tenantId).getTimeZone(),
                                                          shift, indictment);
    }

    @Override
    @Transactional
    public ShiftView addShift(Integer tenantId, ShiftView shiftView) {
        Shift shift = convertFromView(tenantId, shiftView);
        entityManager.persist(shift);
        Indictment indictment = indictmentUtils.getIndictmentMapForRoster(rosterRestService.buildRoster(tenantId)).get(shift);
        return indictmentUtils.getShiftViewWithIndictment(rosterRestService.getRosterState(tenantId).getTimeZone(),
                                                          shift, indictment);
    }

    @Override
    @Transactional
    public ShiftView updateShift(Integer tenantId, ShiftView shiftView) {
        Shift shift = convertFromView(tenantId, shiftView);
        shift = entityManager.merge(shift);

        // Flush to increase version number before we duplicate it to ShiftView
        entityManager.flush();
        Indictment indictment = indictmentUtils.getIndictmentMapForRoster(rosterRestService.buildRoster(tenantId)).get(shift);
        return indictmentUtils.getShiftViewWithIndictment(rosterRestService.getRosterState(tenantId).getTimeZone(),
                                                          shift, indictment);
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
                throw new IllegalArgumentException("ShiftView (" + shiftView + ") has an non-existing employeeId (" + rotationEmployeeId + ").");
            }
            validateTenantIdParameter(tenantId, rotationEmployee);
        }

        Shift shift = new Shift(rosterRestService.getRosterState(tenantId).getTimeZone(), shiftView, spot,
                                rotationEmployee);
        shift.setPinnedByUser(shiftView.isPinnedByUser());
        Long employeeId = shiftView.getEmployeeId();
        if (employeeId != null) {
            Employee employee = entityManager.find(Employee.class, employeeId);
            if (employee == null) {
                throw new IllegalArgumentException("ShiftView (" + shiftView + ") has an non-existing employeeId (" + employeeId + ").");
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
    public List<ShiftView> getShiftList(Integer tenantId) {
        Map<Object, Indictment> indictmentMap = indictmentUtils.getIndictmentMapForRoster(rosterRestService.buildRoster(tenantId));
        return getAllShifts(tenantId).stream()
                .map(s -> indictmentUtils.getShiftViewWithIndictment(rosterRestService.getRosterState(tenantId).getTimeZone(),
                                                                     s, indictmentMap.get(s)))
                .collect(Collectors.toList());
    }

    private List<Shift> getAllShifts(Integer tenantId) {
        TypedQuery<Shift> q = entityManager.createNamedQuery("Shift.findAll", Shift.class);
        q.setParameter("tenantId", tenantId);
        return q.getResultList();
    }
}
