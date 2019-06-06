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

package org.optaweb.employeerostering.domain;

import java.util.Objects;

public class Skill extends AbstractPersistable {

    private final Integer tenantId;
    private final String name;

    public Skill(Integer tenantId, String name) {
        this.tenantId = Objects.requireNonNull(tenantId);
        this.name = Objects.requireNonNull(name);
    }

    public Integer tenantId() {
        return tenantId;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Skill skill = (Skill) o;
        return tenantId.compareTo(skill.tenantId) == 0 &&
                name.compareTo(skill.name) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, name);
    }

    @Override
    public String toString() {
        return "Skill{" +
                "tenantId=" + tenantId +
                "name=" + name +
                '}';
    }
}
