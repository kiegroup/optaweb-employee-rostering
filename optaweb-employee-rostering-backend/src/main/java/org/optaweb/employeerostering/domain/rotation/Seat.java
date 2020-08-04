/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.domain.rotation;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

import org.optaweb.employeerostering.domain.employee.Employee;

/**
 * A Seat is a shift to create in a time bucket for a particular day
 * in the rotation, with an optional employee as the default employee
 * for said shift.
 */
@Embeddable
public class Seat {

    private Integer dayInRotation;

    @ManyToOne
    private Employee employee;

    public Seat() {

    }

    public Seat(Integer dayInRotation, Employee employee) {
        this.setDayInRotation(dayInRotation);
        this.employee = employee;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Integer getDayInRotation() {
        return dayInRotation;
    }

    public void setDayInRotation(Integer dayInRotation) {
        this.dayInRotation = dayInRotation;
    }
}
