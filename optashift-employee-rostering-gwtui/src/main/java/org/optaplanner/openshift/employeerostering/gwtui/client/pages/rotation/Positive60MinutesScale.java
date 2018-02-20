/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.rotation;

import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.FiniteLinearScale;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class PositiveMinutesScale implements FiniteLinearScale<Long> {

    private final Long end;

    public PositiveMinutesScale(final Long end) {
        this.end = end;
    }

    @Override
    public Long toGridPixelsWithFactor1(final Long valueInScaleUnits) {
        return min(max(0, valueInScaleUnits), end);
    }

    @Override
    public Long toScaleUnitsWithFactor1(final Long valueInGridPixels) {
        return min(max(0, valueInGridPixels), end);
    }

    @Override
    public Long getEndInScaleUnits() {
        return end;
    }

    @Override
    public Long factor() {
        return 60L;
    }
}



