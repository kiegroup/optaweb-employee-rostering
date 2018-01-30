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

import javax.validation.constraints.NotNull;

import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

public class ShiftView extends AbstractPersistable {

    private Long rotationEmployeeId;
    @NotNull
    private Long spotId;
    @NotNull
    private Long timeSlotId;

    private boolean lockedByUser = false;

    private Long employeeId = null;

    @SuppressWarnings("unused")
    public ShiftView() {}

    public ShiftView(Integer tenantId, Spot spot, TimeSlot timeSlot) {
        this(tenantId, spot, timeSlot, null);
    }

    public ShiftView(Integer tenantId, Spot spot, TimeSlot timeSlot, Employee rotationEmployee) {
        super(tenantId);
        this.spotId = spot.getId();
        this.timeSlotId = timeSlot.getId();
        this.rotationEmployeeId = (rotationEmployee == null) ? null : rotationEmployee.getId();
    }

    public ShiftView(Shift shift) {
        super(shift);
        this.spotId = shift.getSpot().getId();
        this.timeSlotId = shift.getTimeSlot().getId();
        this.lockedByUser = shift.isLockedByUser();
        this.rotationEmployeeId = (shift.getRotationEmployee() == null) ? null : shift.getRotationEmployee().getId();
        this.employeeId = (shift.getEmployee() == null) ? null : shift.getEmployee().getId();
    }

    @Override
    public String toString() {
        return spotId + " " + timeSlotId;
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

    public Long getTimeSlotId() {
        return timeSlotId;
    }

    public void setTimeSlotId(Long timeSlotId) {
        this.timeSlotId = timeSlotId;
    }

    public boolean isLockedByUser() {
        return lockedByUser;
    }

    public void setLockedByUser(boolean lockedByUser) {
        this.lockedByUser = lockedByUser;
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
