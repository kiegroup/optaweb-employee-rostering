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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.beta;

import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.FiniteLinearScale;

class Finite1to1LinearScaleFrom0To implements FiniteLinearScale<Long> {

    private Long end;

    Finite1to1LinearScaleFrom0To(final Long end) {
        this.end = end;
    }

    @Override
    public Long toGridPixelsWithFactor1(final Long valueInScaleUnits) {
        return valueInScaleUnits;
    }

    @Override
    public Long fromGridPixelsWithFactor1(final Long valueInGridPixels) {
        return valueInGridPixels;
    }

    @Override
    public Long factor() {
        return 1L;
    }

    @Override
    public Long getEnd() {
        return end;
    }
}
