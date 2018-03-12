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

package org.optaplanner.openshift.employeerostering.shared.shift.view;

import java.time.OffsetDateTime;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class ShiftView extends AbstractPersistable {

    private Long rotationEmployeeId;
    @NotNull
    private Long spotId;
    @NotNull
    private OffsetDateTime startDateTime;
    @NotNull
    private OffsetDateTime endDateTime;

    private boolean pinnedByUser = false;

    private Long employeeId = null;

    @SuppressWarnings("unused")
    public ShiftView() {}

    public ShiftView(Integer tenantId, Spot spot, OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        this(tenantId, spot, startDateTime, endDateTime, null);
    }

    public ShiftView(Integer tenantId, Spot spot, OffsetDateTime startDateTime, OffsetDateTime endDateTime, Employee rotationEmployee) {
        super(tenantId);
        this.spotId = spot.getId();
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.rotationEmployeeId = (rotationEmployee == null) ? null : rotationEmployee.getId();
    }

    public ShiftView(Shift shift) {
        super(shift);
        this.spotId = shift.getSpot().getId();
        this.startDateTime = shift.getStartDateTime();
        this.endDateTime = shift.getEndDateTime();
        this.pinnedByUser = shift.isPinnedByUser();
        this.rotationEmployeeId = (shift.getRotationEmployee() == null) ? null : shift.getRotationEmployee().getId();
        this.employeeId = (shift.getEmployee() == null) ? null : shift.getEmployee().getId();
    }

    @Override
    public String toString() {
        return spotId + " " + startDateTime + "-" + endDateTime;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Long getSpotId() {
        return spotId;
    }

    public void setSpotId(Long spotId) {
        this.spotId = spotId;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(OffsetDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(OffsetDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public boolean isPinnedByUser() {
        return pinnedByUser;
    }

    public void setPinnedByUser(boolean pinnedByUser) {
        this.pinnedByUser = pinnedByUser;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getRotationEmployeeId() {
        return rotationEmployeeId;
    }

    public void setRotationEmployeeId(Long rotationEmployeeId) {
        this.rotationEmployeeId = rotationEmployeeId;
    }

}
