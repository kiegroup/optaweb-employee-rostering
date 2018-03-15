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

package org.optaplanner.openshift.employeerostering.shared.employee.view;

import java.time.LocalDate;
import java.time.OffsetTime;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;

public class EmployeeAvailabilityView extends AbstractPersistable {

    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDate date;
    @NotNull
    private OffsetTime startTime;
    @NotNull
    private OffsetTime endTime;

    private EmployeeAvailabilityState state;

    @SuppressWarnings("unused")
    public EmployeeAvailabilityView() {}

    public EmployeeAvailabilityView(Integer tenantId, Employee employee, LocalDate date, OffsetTime startTime, OffsetTime endTime, EmployeeAvailabilityState state) {
        super(tenantId);
        this.employeeId = employee.getId();
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.state = state;
    }

    public EmployeeAvailabilityView(EmployeeAvailability employeeAvailability) {
        super(employeeAvailability);
        this.employeeId = employeeAvailability.getEmployee().getId();
        this.date = employeeAvailability.getDate();
        this.startTime = employeeAvailability.getStartTime();
        this.endTime = employeeAvailability.getEndTime();
        this.state = employeeAvailability.getState();
    }

    @Override
    public String toString() {
        return employeeId + " " + date + ":" + startTime + "-" + endTime;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public OffsetTime getStartTime() {
        return startTime;
    }

    public void setStartTime(OffsetTime startTime) {
        this.startTime = startTime;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public OffsetTime getEndTime() {
        return endTime;
    }

    public void setEndTime(OffsetTime endTime) {
        this.endTime = endTime;
    }

    public EmployeeAvailabilityState getState() {
        return state;
    }

    public void setState(EmployeeAvailabilityState state) {
        this.state = state;
    }

}
