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

package org.optaweb.employeerostering.util;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HierarchyTreeTest {

    private HierarchyTree<Integer, String> tested;

    @BeforeEach
    public void setup() {
        // tested is the divisibility Hierarchy Tree;
        // a is above b iff a divides b
        tested = new HierarchyTree<>((a, b) -> {
            if (a.equals(b)) {
                return HierarchyTree.HierarchyRelationship.IS_THE_SAME_AS;
            } else if (a % b == 0) {
                return HierarchyTree.HierarchyRelationship.IS_BELOW;
            } else if (b % a == 0) {
                return HierarchyTree.HierarchyRelationship.IS_ABOVE;
            } else {
                return HierarchyTree.HierarchyRelationship.HAS_NO_DIRECT_RELATION;
            }
        });
    }

    @Test
    public void testPutInHierarchyAndGetHierarchyClassValue() {
        assertThat(tested.getHierarchyClassValue(2)).isEmpty();
        assertThat(tested.getHierarchyClassValue(4)).isEmpty();
        assertThat(tested.getHierarchyClassValue(6)).isEmpty();
        assertThat(tested.getHierarchyClassValue(3)).isEmpty();

        tested.putInHierarchy(4, "4");
        assertThat(tested.getHierarchyClassValue(2)).isEmpty();
        assertThat(tested.getHierarchyClassValue(4)).hasValue("4");
        assertThat(tested.getHierarchyClassValue(12)).hasValue("4");
        assertThat(tested.getHierarchyClassValue(3)).isEmpty();

        tested.putInHierarchy(12, "12");
        assertThat(tested.getHierarchyClassValue(2)).isEmpty();
        assertThat(tested.getHierarchyClassValue(4)).hasValue("4");
        assertThat(tested.getHierarchyClassValue(12)).hasValue("12");
        assertThat(tested.getHierarchyClassValue(3)).isEmpty();

        tested.putInHierarchy(3, "3");
        assertThat(tested.getHierarchyClassValue(2)).isEmpty();
        assertThat(tested.getHierarchyClassValue(4)).hasValue("4");
        assertThat(tested.getHierarchyClassValue(12)).hasValue("12");
        assertThat(tested.getHierarchyClassValue(3)).hasValue("3");

        tested.putInHierarchy(2, "2");
        assertThat(tested.getHierarchyClassValue(2)).hasValue("2");
        assertThat(tested.getHierarchyClassValue(4)).hasValue("4");
        assertThat(tested.getHierarchyClassValue(12)).hasValue("12");
        assertThat(tested.getHierarchyClassValue(3)).hasValue("3");
    }
}
