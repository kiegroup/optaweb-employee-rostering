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

package org.optaweb.employeerostering.shared.employee.view;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.optaweb.employeerostering.shared.common.AbstractPersistable;
import org.optaweb.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;
import org.optaweb.employeerostering.shared.common.HasTimeslot;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeAvailability;
import org.optaweb.employeerostering.shared.employee.EmployeeAvailabilityState;

public class EmployeeAvailabilityView extends AbstractPersistable implements HasTimeslot {

    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDateTime startDateTime;
    @NotNull
    private LocalDateTime endDateTime;

    private EmployeeAvailabilityState state;

    @SuppressWarnings("unused")
    public EmployeeAvailabilityView() {}

    public EmployeeAvailabilityView(Integer tenantId, Employee employee, LocalDateTime startDateTime, LocalDateTime endDateTime, EmployeeAvailabilityState state) {
        super(tenantId);
        this.employeeId = employee.getId();
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.state = state;
    }

    public EmployeeAvailabilityView(ZoneId zoneId, EmployeeAvailability employeeAvailability) {
        super(employeeAvailability);
        this.employeeId = employeeAvailability.getEmployee().getId();
        this.startDateTime = GwtJavaTimeWorkaroundUtil.toLocalDateTimeInZone(employeeAvailability.getStartDateTime(), zoneId);
        this.endDateTime = GwtJavaTimeWorkaroundUtil.toLocalDateTimeInZone(employeeAvailability.getEndDateTime(), zoneId);
        this.state = employeeAvailability.getState();
    }

    @Override
    public String toString() {
        return employeeId + ":" + startDateTime + "-" + endDateTime;
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

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public EmployeeAvailabilityState getState() {
        return state;
    }

    public void setState(EmployeeAvailabilityState state) {
        this.state = state;
    }

    @Override
    @JsonIgnore
    public Duration getDurationBetweenReferenceAndStart() {
        return Duration.between(HasTimeslot.EPOCH, getStartDateTime());
    }

    @Override
    @JsonIgnore
    public Duration getDurationOfTimeslot() {
        return Duration.between(getStartDateTime(), getEndDateTime());
    }

}
