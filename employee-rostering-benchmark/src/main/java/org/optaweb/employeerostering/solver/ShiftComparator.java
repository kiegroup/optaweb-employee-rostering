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

package org.optaweb.employeerostering.solver;

import java.util.Comparator;

import org.optaweb.employeerostering.domain.shift.Shift;

public class ShiftComparator implements Comparator<Shift> {

    private static final Comparator<Shift> DATE_TIME_COMPARATOR = Comparator.comparing(Shift::getStartDateTime)
            .thenComparing(Shift::getEndDateTime);

    @Override
    public int compare(Shift a, Shift b) {
        return DATE_TIME_COMPARATOR.compare(a, b);
    }
}
