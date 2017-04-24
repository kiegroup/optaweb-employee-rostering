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

import com.fasterxml.jackson.annotation.JsonProperty;

public class Spot {

    private String name;
    private Skill requiredSkill;

    @SuppressWarnings("unused")
    public Spot() {
    }

    public Spot(String name, Skill requiredSkill) {
        this.name = name;
        this.requiredSkill = requiredSkill;
    }

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

    @Override
    public String toString() {
        return name;
    }

}
