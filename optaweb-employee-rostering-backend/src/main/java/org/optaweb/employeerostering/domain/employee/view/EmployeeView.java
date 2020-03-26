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

package org.optaweb.employeerostering.domain.employee.view;

import java.util.Set;

import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.employee.CovidRiskType;
import org.optaweb.employeerostering.domain.skill.Skill;

public class EmployeeView extends AbstractPersistable {

    private String name;

    private Contract contract;

    private Set<Skill> skillProficiencySet;

    private CovidRiskType covidRiskType;

    @SuppressWarnings("unused")
    public EmployeeView() {
    }

    public EmployeeView(Integer tenantId, String name, Contract contract,
                        Set<Skill> skillProficiencySet, CovidRiskType covidRiskType) {
        super(tenantId);
        this.name = name;
        this.contract = contract;
        this.skillProficiencySet = skillProficiencySet;
        this.covidRiskType = covidRiskType;
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
}
