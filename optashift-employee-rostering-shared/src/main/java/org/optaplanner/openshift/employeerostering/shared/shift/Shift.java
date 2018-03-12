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

import java.time.OffsetDateTime;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

@Entity
@NamedQueries({
               @NamedQuery(name = "Shift.findAll",
                           query = "select distinct sa from Shift sa" +
                                   " left join fetch sa.spot s" +
                                   " left join fetch sa.rotationEmployee re" +
                                   " left join fetch sa.employee e" +
                                   " where sa.tenantId = :tenantId" +
                                   " order by sa.startDateTime, s.name, e.name"),
})
@PlanningEntity(movableEntitySelectionFilter = MovableShiftFilter.class)
public class Shift extends AbstractPersistable {

    @ManyToOne
    private Employee rotationEmployee;
    @NotNull
    @ManyToOne
    private Spot spot;

    @NotNull
    private OffsetDateTime startDateTime;
    @NotNull
    private OffsetDateTime endDateTime;

    @PlanningPin
    private boolean pinnedByUser = false;

    @ManyToOne
    @PlanningVariable(valueRangeProviderRefs = "employeeRange")
    private Employee employee = null;

    @SuppressWarnings("unused")
    public Shift() {}

    public Shift(Integer tenantId, Spot spot, OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        this(tenantId, spot, startDateTime, endDateTime, null);
    }

    public Shift(Integer tenantId, Spot spot, OffsetDateTime startDateTime, OffsetDateTime endDateTime, Employee rotationEmployee) {
        super(tenantId);
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.spot = spot;
        this.rotationEmployee = rotationEmployee;
    }

    public Shift(ShiftView shiftView, Spot spot, OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        this(shiftView, spot, startDateTime, endDateTime, null);
    }

    public Shift(ShiftView shiftView, Spot spot, OffsetDateTime startDateTime, OffsetDateTime endDateTime, Employee rotationEmployee) {
        super(shiftView);
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.spot = spot;
        this.pinnedByUser = shiftView.isPinnedByUser();
        this.rotationEmployee = rotationEmployee;
    }

    @Override
    public String toString() {
        return spot + " " + startDateTime + "-" + endDateTime;
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

    public void setPinnedByUser(boolean lockedByUser) {
        this.pinnedByUser = lockedByUser;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Employee getRotationEmployee() {
        return rotationEmployee;
    }

    public void setRotationEmployee(Employee rotationEmployee) {
        this.rotationEmployee = rotationEmployee;
    }

}
