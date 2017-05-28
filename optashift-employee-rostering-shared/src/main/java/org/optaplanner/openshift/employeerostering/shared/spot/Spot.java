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

package org.optaplanner.openshift.employeerostering.shared.spot;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;

//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property="id")
@Entity
@NamedQueries({
        @NamedQuery(name = "Spot.findAll", query = "SELECT s FROM Spot s WHERE s.tenantId = :tenantId"),
})
public class Spot extends AbstractPersistable {

    @NotNull @Size(min = 1, max = 120)
    private String name;
    @ManyToOne(fetch = FetchType.EAGER)
    @NotNull
    private Skill requiredSkill;

    @SuppressWarnings("unused")
    public Spot() {
    }

    public Spot(Integer tenantId, String name, Skill requiredSkill) {
        super(tenantId);
        this.name = name;
        this.requiredSkill = requiredSkill;
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

    public Skill getRequiredSkill() {
        return requiredSkill;
    }

    public void setRequiredSkill(Skill requiredSkill) {
        this.requiredSkill = requiredSkill;
    }

}
