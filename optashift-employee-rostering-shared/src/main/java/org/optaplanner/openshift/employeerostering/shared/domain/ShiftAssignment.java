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

package org.optaplanner.openshift.employeerostering.shared.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.domain.solver.MovableShiftAssignmentFilter;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property="id")
@PlanningEntity(movableEntitySelectionFilter = MovableShiftAssignmentFilter.class)
public class ShiftAssignment extends AbstractPersistable {

    private Spot spot;
    private TimeSlot timeSlot;

    private boolean lockedByUser = false;

    @PlanningVariable(valueRangeProviderRefs = "employeeRange")
    private Employee employee = null;

    @SuppressWarnings("unused")
    public ShiftAssignment() {
    }

    public ShiftAssignment(Long id, Spot spot, TimeSlot timeSlot) {
        super(id);
        this.timeSlot = timeSlot;
        this.spot = spot;
    }

    @Override
    public String toString() {
        return spot + " " + timeSlot;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Spot getSpot() {
        return spot;
    }

    public void setSpot(Spot spot) {
        this.spot = spot;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public boolean isLockedByUser() {
        return lockedByUser;
    }

    public void setLockedByUser(boolean lockedByUser) {
        this.lockedByUser = lockedByUser;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

}
