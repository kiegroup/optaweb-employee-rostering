/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.openshift.employeerostering.shared.roster.view;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftAssignmentView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

public class SpotRosterView implements Serializable {

    @NotNull
    protected Integer tenantId;

    protected LocalDate startDate; // inclusive
    protected LocalDate endDate; // inclusive

    protected List<Spot> spotList;
    protected List<Employee> employeeList;
    protected List<TimeSlot> timeSlotList;
    protected Map<Long, Map<Long, List<ShiftAssignmentView>>> spotIdToTimeSlotIdToShiftAssignmentViewListMap;

    @SuppressWarnings("unused")
    public SpotRosterView() {
    }

    public SpotRosterView(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public SpotRosterView(Integer tenantId, LocalDate startDate, LocalDate endDate) {
        this(tenantId);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return startDate + " to " + endDate;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<Spot> getSpotList() {
        return spotList;
    }

    public void setSpotList(List<Spot> spotList) {
        this.spotList = spotList;
    }

    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    public List<TimeSlot> getTimeSlotList() {
        return timeSlotList;
    }

    public void setTimeSlotList(List<TimeSlot> timeSlotList) {
        this.timeSlotList = timeSlotList;
    }

    public Map<Long, Map<Long, List<ShiftAssignmentView>>> getSpotIdToTimeSlotIdToShiftAssignmentViewListMap() {
        return spotIdToTimeSlotIdToShiftAssignmentViewListMap;
    }

    public void setSpotIdToTimeSlotIdToShiftAssignmentViewListMap(Map<Long, Map<Long, List<ShiftAssignmentView>>> spotIdToTimeSlotIdToShiftAssignmentViewListMap) {
        this.spotIdToTimeSlotIdToShiftAssignmentViewListMap = spotIdToTimeSlotIdToShiftAssignmentViewListMap;
    }

}
