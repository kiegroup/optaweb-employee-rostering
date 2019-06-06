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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

public class SkillTest {

    @Test
    public void constructorParamsMustNotBeNull() {
        assertThatNullPointerException().isThrownBy(() -> new Skill(null, ""));
        assertThatNullPointerException().isThrownBy(() -> new Skill(0, null));
    }

    @Test
    public void skillsAreIdentifiedBasedOnTenantId() {
        final Integer id0 = new Integer(0);
        final Integer id1 = new Integer(1);
        final String name = "test name";

        final Skill skill = new Skill(id0, name);

        // different tenantId
        assertThat(skill).isNotEqualTo(new Skill(id1, name));
        // null
        assertThat(skill).isNotEqualTo(null);
        // same object -> OK
        assertThat(skill).isEqualTo(skill);
        // same properties -> OK
        assertThat(skill).isEqualTo(new Skill(id0, name));
    }
}
