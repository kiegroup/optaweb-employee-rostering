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

package org.optaweb.employeerostering.domain.spot;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.skill.Skill;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"tenantId", "name"}),
        @UniqueConstraint(columnNames = {"id"})})
public class Spot extends AbstractPersistable {

    @NotNull
    @Size(min = 1, max = 120)
    @Pattern(regexp = "^(?!\\s).*(?<!\\s)$", message = "Name should not contain any leading or trailing whitespaces")
    private String name;

    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "SpotRequiredSkillSet",
            joinColumns = @JoinColumn(name = "spotId", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "skillId", referencedColumnName = "id")
    )
    private Set<Skill> requiredSkillSet;

    private Boolean covidWard;

    @SuppressWarnings("unused")
    public Spot() {
    }

    public Spot(Integer tenantId, String name, Set<Skill> requiredSkillSet, Boolean covidWard) {
        super(tenantId);
        this.name = name;
        this.requiredSkillSet = requiredSkillSet;
        this.covidWard = covidWard;
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

    public Set<Skill> getRequiredSkillSet() {
        return requiredSkillSet;
    }

    public void setRequiredSkillSet(Set<Skill> requiredSkillSet) {
        this.requiredSkillSet = requiredSkillSet;
    }

    public Boolean isCovidWard() {
        return covidWard;
    }

    public void setCovidWard(Boolean covidWard) {
        this.covidWard = covidWard;
    }
}
