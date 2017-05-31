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

package org.optaplanner.openshift.employeerostering.shared.employee;

import java.util.Set;

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

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "skill_id"}))
public class EmployeeSkillProficiency extends AbstractPersistable {

    @JsonBackReference
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    private Employee employee;
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    private Skill skill;

    @SuppressWarnings("unused")
    public EmployeeSkillProficiency() {
    }

    public EmployeeSkillProficiency(Integer tenantId, Employee employee, Skill skill) {
        super(tenantId);
        this.employee = employee;
        this.skill = skill;
    }

    @Override
    public String toString() {
        return employee + " " + skill;
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

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

}
