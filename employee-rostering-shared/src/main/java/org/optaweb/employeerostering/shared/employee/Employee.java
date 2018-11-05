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

package org.optaweb.employeerostering.shared.employee;

import java.util.Collection;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.optaweb.employeerostering.shared.common.AbstractPersistable;
import org.optaweb.employeerostering.shared.contract.Contract;
import org.optaweb.employeerostering.shared.skill.Skill;

@Entity
@NamedQueries({
        @NamedQuery(name = "Employee.findAll",
                query = "select e from Employee e" +
                        " where e.tenantId = :tenantId" +
                        " order by LOWER(e.name)"),
        @NamedQuery(name = "Employee.deleteForTenant",
                query = "delete from Employee e where e.tenantId = :tenantId")
})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"tenantId", "name"}))
public class Employee extends AbstractPersistable {

    @NotNull
    @Size(min = 1, max = 120)
    @Pattern(regexp = "^(?!\\s).*(?<!\\s)$", message = "Name should not contain any leading or trailing whitespaces")
    private String name;

    @NotNull
    @ManyToOne
    private Contract contract;

    //@JsonManagedReference
    @NotNull
    @ManyToMany(fetch = FetchType.EAGER) // TODO Eager fetching bloats the EmployeeAvailability.findAll query
    @JoinTable(name = "EmployeeSkillProficiencySet",
            joinColumns = @JoinColumn(name = "employeeId", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "skillId", referencedColumnName = "id")
    )
    private Set<Skill> skillProficiencySet;

    @SuppressWarnings("unused")
    public Employee() {
    }

    public Employee(Integer tenantId, String name, Contract contract, Set<Skill> skillProficiencySet) {
        super(tenantId);
        this.name = name;
        this.contract = contract;
        this.skillProficiencySet = skillProficiencySet;
    }

    public boolean hasSkill(Skill skill) {
        return skillProficiencySet.contains(skill);
    }

    public boolean hasSkills(Collection<Skill> skills) {
        return skillProficiencySet.containsAll(skills);
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Skill> getSkillProficiencySet() {
        return skillProficiencySet;
    }

    public void setSkillProficiencySet(Set<Skill> skillProficiencySet) {
        this.skillProficiencySet = skillProficiencySet;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Employee) {
            Employee other = (Employee) o;
            return this.name.equals(other.getName()) &&
                    this.contract.equals(other.getContract()) &&
                    this.skillProficiencySet.equals(other.getSkillProficiencySet());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31 * ((31 * name.hashCode()) ^ contract.hashCode()) ^ skillProficiencySet.hashCode();
    }
}
