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

import java.time.Duration;
import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Columns;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;

@Entity
@NamedQueries({
               @NamedQuery(name = "EmployeeAvailability.findAll",
                           query = "select distinct ea from EmployeeAvailability ea" +
                                   " left join fetch ea.employee e" +
                                   " where ea.tenantId = :tenantId" +
                                   " order by e.name, ea.startDateTime"),
               @NamedQuery(name = "EmployeeAvailability.filter",
                           query = "select distinct ea from EmployeeAvailability ea" +
                                   " left join fetch ea.employee e" +
                                   " where ea.tenantId = :tenantId" +
                                   " and ea.endDateTime >= :startDateTime" +
                                   " and ea.startDateTime < :endDateTime" +
                                   " order by e.name, ea.startDateTime"),
               @NamedQuery(name = "EmployeeAvailability.filterWithEmployee",
                           query = "select distinct ea from EmployeeAvailability ea" +
                                   " left join fetch ea.employee e" +
                                   " where ea.tenantId = :tenantId" +
                                   " and ea.employee IN :employeeSet" +
                                   " and ea.endDateTime >= :startDateTime" +
                                   " and ea.startDateTime < :endDateTime" +
                                   " order by e.name, ea.startDateTime")
})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"tenantId", "employee_id", "startDateTime", "endDateTime"}))
public class EmployeeAvailability extends AbstractPersistable {

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    private Employee employee;

    @Type(type = "org.optaplanner.openshift.employeerostering.server.common.jpa.OffsetDateTimeHibernateType")
    @Columns(columns = {@Column(name = "startDateTime"), @Column(name="startDateTimeOffset")})
    @NotNull
    private OffsetDateTime startDateTime;
    @Type(type = "org.optaplanner.openshift.employeerostering.server.common.jpa.OffsetDateTimeHibernateType")
    @Columns(columns = {@Column(name = "endDateTime"), @Column(name="endDateTimeOffset")})
    @NotNull
    private OffsetDateTime endDateTime;

    @NotNull
    private EmployeeAvailabilityState state;

    @SuppressWarnings("unused")
    public EmployeeAvailability() {}

    public EmployeeAvailability(Integer tenantId, Employee employee, OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        super(tenantId);
        this.employee = employee;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public EmployeeAvailability(EmployeeAvailabilityView employeeAvailabilityView, Employee employee) {
        super(employeeAvailabilityView);
        this.employee = employee;
        this.startDateTime = employeeAvailabilityView.getStartDateTime();
        this.endDateTime = employeeAvailabilityView.getEndDateTime();
    }

    @AssertTrue
    public boolean isValid() {
        return getDuration().getSeconds() / (60 * 60) < 28;
    }

    public Duration getDuration() {
        return Duration.between(startDateTime, endDateTime);
    }

    @Override
    public String toString() {
        return employee + ":" + startDateTime + "-" + endDateTime;
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

    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(OffsetDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(OffsetDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public EmployeeAvailabilityState getState() {
        return state;
    }

    public void setState(EmployeeAvailabilityState state) {
        this.state = state;
    }

}
