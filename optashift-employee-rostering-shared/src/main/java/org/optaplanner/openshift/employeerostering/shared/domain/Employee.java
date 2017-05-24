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

package org.optaplanner.openshift.employeerostering.shared.domain;

import java.util.Set;

import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;

//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property="id")
public class Employee extends AbstractPersistable {

    private String name;
    private Set<Skill> skillSet;

    private Set<TimeSlot> unavailableTimeSlotSet;

    @SuppressWarnings("unused")
    public Employee() {
    }

    public Employee(Long id, String name, Set<Skill> skillSet) {
        super(id);
        this.name = name;
        this.skillSet = skillSet;
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

    public Set<Skill> getSkillSet() {
        return skillSet;
    }

    public void setSkillSet(Set<Skill> skillSet) {
        this.skillSet = skillSet;
    }

    public Set<TimeSlot> getUnavailableTimeSlotSet() {
        return unavailableTimeSlotSet;
    }

    public void setUnavailableTimeSlotSet(Set<TimeSlot> unavailableTimeSlotSet) {
        this.unavailableTimeSlotSet = unavailableTimeSlotSet;
    }

}
