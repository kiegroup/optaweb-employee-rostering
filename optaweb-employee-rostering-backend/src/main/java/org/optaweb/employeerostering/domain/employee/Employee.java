/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.domain.employee;

import java.util.Collection;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.skill.Skill;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"tenantId", "name"}))
public class Employee extends AbstractPersistable {

    @NotNull
    @Size(min = 1, max = 120)
    @Pattern(regexp = "^(?!\\s).*(?<!\\s)$", message = "Name should not contain any leading or trailing whitespaces")
    private String name;

    @NotNull
    @ManyToOne
    private Contract contract;

    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "EmployeeSkillProficiencySet",
            joinColumns = @JoinColumn(name = "employeeId", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "skillId", referencedColumnName = "id")
    )
    private Set<Skill> skillProficiencySet;

    private CovidRiskType covidRiskType;

    @SuppressWarnings("unused")
    public Employee() {
    }

    public Employee(Integer tenantId, String name, Contract contract,
                    Set<Skill> skillProficiencySet, CovidRiskType covidRiskType) {
        super(tenantId);
        this.name = name;
        this.contract = contract;
        this.skillProficiencySet = skillProficiencySet;
        this.covidRiskType = covidRiskType;
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

    public CovidRiskType getCovidRiskType() {
        return covidRiskType;
    }

    public void setCovidRiskType(CovidRiskType covidRiskType) {
        this.covidRiskType = covidRiskType;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Employee) {
            Employee other = (Employee) o;
            return this.name.equals(other.getName()) &&
                    this.contract.equals(other.getContract()) &&
                    this.skillProficiencySet.equals(other.getSkillProficiencySet()) &&
                    this.covidRiskType.equals(other.covidRiskType);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 31 * (31 * ((31 * name.hashCode()) ^ contract.hashCode())
                ^ skillProficiencySet.hashCode()) ^ covidRiskType.hashCode();
    }
}
