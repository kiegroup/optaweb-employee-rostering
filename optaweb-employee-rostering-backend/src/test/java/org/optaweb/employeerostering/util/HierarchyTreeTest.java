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

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HierarchyTreeTest {

    private HierarchyTree<Integer, String> tested;

    @Before
    public void setup() {
        // tested is the divisibility Hierarchy Tree;
        // a is above b iff a divides b
        tested = new HierarchyTree<>((a, b) -> {
            if (a == b) {
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
        assertEquals(Optional.empty(), tested.getHierarchyClassValue(2));
        assertEquals(Optional.empty(), tested.getHierarchyClassValue(4));
        assertEquals(Optional.empty(), tested.getHierarchyClassValue(6));
        assertEquals(Optional.empty(), tested.getHierarchyClassValue(3));

        tested.putInHierarchy(4, "4");
        assertEquals(Optional.empty(), tested.getHierarchyClassValue(2));
        assertEquals(Optional.of("4"), tested.getHierarchyClassValue(4));
        assertEquals(Optional.of("4"), tested.getHierarchyClassValue(12));
        assertEquals(Optional.empty(), tested.getHierarchyClassValue(3));

        tested.putInHierarchy(12, "12");
        assertEquals(Optional.empty(), tested.getHierarchyClassValue(2));
        assertEquals(Optional.of("4"), tested.getHierarchyClassValue(4));
        assertEquals(Optional.of("12"), tested.getHierarchyClassValue(12));
        assertEquals(Optional.empty(), tested.getHierarchyClassValue(3));

        tested.putInHierarchy(3, "3");
        assertEquals(Optional.empty(), tested.getHierarchyClassValue(2));
        assertEquals(Optional.of("4"), tested.getHierarchyClassValue(4));
        assertEquals(Optional.of("12"), tested.getHierarchyClassValue(12));
        assertEquals(Optional.of("3"), tested.getHierarchyClassValue(3));

        tested.putInHierarchy(2, "2");
        assertEquals(Optional.of("2"), tested.getHierarchyClassValue(2));
        assertEquals(Optional.of("4"), tested.getHierarchyClassValue(4));
        assertEquals(Optional.of("12"), tested.getHierarchyClassValue(12));
        assertEquals(Optional.of("3"), tested.getHierarchyClassValue(3));
    }
}
