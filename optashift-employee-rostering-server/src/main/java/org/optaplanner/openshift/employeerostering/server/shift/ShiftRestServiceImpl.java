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
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.optaplanner.openshift.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaplanner.openshift.employeerostering.server.lang.parser.ShiftFileParser;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeGroup;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestService;
import org.optaplanner.openshift.employeerostering.shared.file.FileService;
import org.optaplanner.openshift.employeerostering.shared.lang.parser.ParserException;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.BaseDateDefinitions;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.EnumOrCustom;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.RepeatMode;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.ShiftInfo;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.ShiftTemplate;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestService;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestService;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlotState;

public class ShiftRestServiceImpl extends AbstractRestServiceImpl implements ShiftRestService {

    @PersistenceContext
    private EntityManager entityManager;
    
    @Inject
    private SpotRestService spotRestService;

    @Inject
    private EmployeeRestService employeeRestService;

    @Inject
    private FileService fileService;

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
    public void updateShift(Integer tenantId, ShiftView shiftView) {
        Shift shift = convertFromView(tenantId, shiftView);
        entityManager.merge(shift);
    }

    private Shift convertFromView(Integer tenantId, ShiftView shiftView) {
        validateTenantIdParameter(tenantId, shiftView);
        Spot spot = entityManager.find(Spot.class, shiftView.getSpotId());
        validateTenantIdParameter(tenantId, spot);
        TimeSlot timeSlot = entityManager.find(TimeSlot.class, shiftView.getTimeSlotId());
        validateTenantIdParameter(tenantId, timeSlot);
        Shift shift = new Shift(shiftView, spot, timeSlot);
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
            String startDateString, String endDateString, String fileName) throws Exception {

        String data = fileService.getFileData(tenantId, fileName);
        LocalDateTime startDate = LocalDateTime.parse(startDateString);
        LocalDateTime endDate = LocalDateTime.parse(endDateString);

        Map<Long, List<Spot>> spotGroupMap = new HashMap<>();
        Map<Long, List<Employee>> employeeGroupMap = new HashMap<>();
        spotRestService.getSpotGroups(tenantId).forEach((g) -> spotGroupMap.put(g.getId(), g.getSpots()));
        employeeRestService.getEmployeeGroups(tenantId).forEach((g) -> employeeGroupMap.put(g.getId(), g
                .getEmployees()));
        employeeGroupMap.put(EmployeeGroup.ALL_GROUP_ID, employeeRestService.getEmployeeList(tenantId));

        try {
            ShiftFileParser.ParserOut parserOutput = ShiftFileParser.parse(tenantId,
                    spotRestService.getSpotList(tenantId),
                    employeeRestService.getEmployeeList(tenantId),
                    spotGroupMap,
                    employeeGroupMap,
                    startDate,
                    endDate,
                    data);
            List<Shift> shifts = parserOutput.getShiftsOut();

            List<EmployeeAvailability> employeeAvailabilities = parserOutput.getEmployeeAvailabilityOut();

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
    public void createTemplate(Integer tenantId, String name, Collection<ShiftInfo> shifts) {
        ShiftTemplate template = new ShiftTemplate();
        template.setBaseDateType(new EnumOrCustom(false, BaseDateDefinitions.SAME_AS_START_DATE.toString()));
        long weeksInShifts = 1;
        /*Math.round(Math.ceil(Duration.between(LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC),
        shifts.stream()
        .max((a, b) -> a.getEndTime().compareTo(b.getEndTime()))
        .get().getEndTime()).toDays() / 7.0))*/;
        template.setRepeatType(new EnumOrCustom(false, RepeatMode.NONE.toString()));
        template.setUniversalExceptions(Collections.emptyList());
        template.setShifts(shifts.stream().collect(Collectors.toList()));

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            fileService.writeFile(tenantId, name, objectMapper.writeValueAsString(template));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Encountered an error when generating the JSON output", e);
        }
    }

}
