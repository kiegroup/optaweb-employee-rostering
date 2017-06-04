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

package org.optaplanner.openshift.employeerostering.shared.employee;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

@Entity
@NamedQueries({
        @NamedQuery(name = "EmployeeAvailability.findAll",
                query = "select distinct ea from EmployeeAvailability ea" +
                        " left join fetch ea.employee e left join fetch ea.timeSlot t" +
                        " where e.tenantId = :tenantId" +
                        " order by e.name, t.startDateTime"),
})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"tenantId", "employee_id", "timeSlot_id"}))
public class EmployeeAvailability extends AbstractPersistable {

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    private Employee employee;
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    private TimeSlot timeSlot;
    @NotNull
    private EmployeeAvailabilityState employeeAvailabilityState;

    @SuppressWarnings("unused")
    public EmployeeAvailability() {
    }

    public EmployeeAvailability(Integer tenantId, Employee employee, TimeSlot timeSlot) {
        super(tenantId);
        this.employee = employee;
        this.timeSlot = timeSlot;
    }

    @Override
    public String toString() {
        return employee + " " + timeSlot;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public EmployeeAvailabilityState getEmployeeAvailabilityState() {
        return employeeAvailabilityState;
    }

    public void setEmployeeAvailabilityState(EmployeeAvailabilityState employeeAvailabilityState) {
        this.employeeAvailabilityState = employeeAvailabilityState;
    }

}
