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

package org.optaplanner.openshift.employeerostering.shared.shift;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

@Entity
@NamedQueries({
        @NamedQuery(name = "Shift.findAll",
                query = "select distinct sa from Shift sa" +
                        " left join fetch sa.spot s" +
                        " left join fetch sa.timeSlot t" +
                        " left join fetch sa.employee e" +
                        " where sa.tenantId = :tenantId" +
                        " order by t.startDateTime, s.name, e.name"),
})
@PlanningEntity(movableEntitySelectionFilter = MovableShiftFilter.class)
public class Shift extends AbstractPersistable {

    @NotNull
    @ManyToOne
    private Spot spot;
    @NotNull
    @ManyToOne
    private TimeSlot timeSlot;

    private boolean lockedByUser = false;

    @ManyToOne
    @PlanningVariable(valueRangeProviderRefs = "employeeRange")
    private Employee employee = null;

    @SuppressWarnings("unused")
    public Shift() {
    }

    public Shift(Integer tenantId, Spot spot, TimeSlot timeSlot) {
        super(tenantId);
        this.timeSlot = timeSlot;
        this.spot = spot;
    }

    public Shift(ShiftView shiftView, Spot spot, TimeSlot timeSlot) {
        super(shiftView);
        this.timeSlot = timeSlot;
        this.spot = spot;
        this.lockedByUser = shiftView.isLockedByUser();
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
