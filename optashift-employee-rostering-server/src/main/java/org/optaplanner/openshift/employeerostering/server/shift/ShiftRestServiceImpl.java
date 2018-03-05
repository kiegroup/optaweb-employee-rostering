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
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestService;
import org.optaplanner.openshift.employeerostering.shared.lang.parser.ParserException;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.BaseDateDefinitions;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.EnumOrCustom;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.ShiftInfo;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.ShiftTemplate;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestService;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestService;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlotState;

public class ShiftRestServiceImpl extends AbstractRestServiceImpl implements ShiftRestService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private SpotRestService spotRestService;

    @Inject
    private EmployeeRestService employeeRestService;

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
        TimeSlot timeSlot = entityManager.find(TimeSlot.class, shiftView.getTimeSlotId());
        validateTenantIdParameter(tenantId, timeSlot);

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

        Shift shift = new Shift(shiftView, spot, timeSlot, rotationEmployee);
        shift.setLockedByUser(shiftView.isLockedByUser());
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
    public List<Long> addShiftsFromTemplate(Integer tenantId,
                                            String startDateString, String endDateString) throws Exception {
        ShiftTemplate template = getTemplate(tenantId);

        if (null == template) {
            throw new IllegalStateException("You cannot add shifts if you don't have a template!");
        }

        LocalDateTime startDate = LocalDateTime.parse(startDateString);
        LocalDateTime endDate = LocalDateTime.parse(endDateString);

        try {
            ShiftFileParser.ParserOut parserOutput = ShiftFileParser.parse(tenantId,
                                                                           spotRestService.getSpotList(tenantId),
                                                                           employeeRestService.getEmployeeList(tenantId),
                                                                           startDate,
                                                                           endDate,
                                                                           template);
            List<Shift> shifts = parserOutput.getShiftOutputList();

            List<EmployeeAvailability> employeeAvailabilities = parserOutput.getEmployeeAvailabilityOutputList();

            HashMap<String, TimeSlot> timeSlotMap = new HashMap<>();
            List<Long> out = new ArrayList<Long>();
            for (Shift shift : shifts) {
                if (!timeSlotMap.containsKey(shift.getTimeSlot().toString())) {
                    shift.getTimeSlot().setTimeSlotState(TimeSlotState.DRAFT);
                    entityManager.persist(shift.getTimeSlot());
                    timeSlotMap.put(shift.getTimeSlot().toString(), shift.getTimeSlot());
                }
                TimeSlot timeSlot = timeSlotMap.get(shift.getTimeSlot().toString());
                Shift newShift = new Shift(tenantId, shift.getSpot(), timeSlot);
                newShift.setRotationEmployee(shift.getRotationEmployee());
                entityManager.persist(newShift);
                out.add(newShift.getId());
            }

            HashSet<String> employeeAvailabilitySet = new HashSet<>();
            for (EmployeeAvailability availability : employeeAvailabilities) {
                if (!employeeAvailabilitySet.contains(availability.toString())) {
                    TimeSlot timeSlot = timeSlotMap.get(availability.getTimeSlot().toString());
                    availability.setTimeSlot(timeSlot);
                    if (null != availability.getState()) {
                        entityManager.persist(availability);
                    }
                    employeeAvailabilitySet.add(availability.toString());
                }
            }
            return out;
        } catch (ParserException e) {
            throw new Exception(e.getMessage());
        }
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
    public ShiftTemplate getTemplate(Integer tenantId) {
        TypedQuery<ShiftTemplate> q = entityManager.createNamedQuery("ShiftTemplate.get", ShiftTemplate.class);
        q.setParameter("tenantId", tenantId);
        List<ShiftTemplate> result = q.getResultList();
        if (result.isEmpty()) {
            return null;
        } else if (1 != result.size()) {
            throw new IllegalStateException("Each tenant can only have 1 template! Found " + result.size()
                                                    + "templates!");
        } else {
            return result.get(0);
        }
    }

    @Override
    @Transactional
    public void createTemplate(Integer tenantId, Collection<ShiftInfo> shifts) {
        Tenant tenant = entityManager.find(Tenant.class, tenantId);
        if (null == tenant) {
            throw new IllegalStateException("Tenant " + tenantId + " does not exist!");
        }
        ShiftTemplate old = getTemplate(tenantId);
        ShiftTemplate template = (null != old) ? old : new ShiftTemplate();
        template.setBaseDateType(new EnumOrCustom(tenantId, false, BaseDateDefinitions.WEEK_AFTER_START_DATE
                .toString()));
        long weeksInShifts = tenant.getConfiguration().getTemplateDuration();
        template.setRepeatType(new EnumOrCustom(tenantId, true, "0:" + weeksInShifts + ":0:0"));
        template.setUniversalExceptionList(Collections.emptyList());
        template.setShiftList(shifts.stream().collect(Collectors.toList()));
        template.setTenantId(tenantId);

        entityManager.merge(template);
    }
}
