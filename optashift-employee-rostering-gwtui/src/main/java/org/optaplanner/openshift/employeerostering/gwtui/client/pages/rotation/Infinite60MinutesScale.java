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

import java.time.Duration;
import java.time.OffsetDateTime;

import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.LinearScale;

public class Infinite60MinutesScale implements LinearScale<OffsetDateTime> {

    private final OffsetDateTime startDateTime;
    private final OffsetDateTime endDateTime;

    public Infinite60MinutesScale(final OffsetDateTime startDateTime, final OffsetDateTime endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    @Override
    public Long toGridPixelsWithFactor1(final OffsetDateTime valueInScaleUnits) {
        return Duration.between(startDateTime, valueInScaleUnits).getSeconds() / 60;
    }

    @Override
    public OffsetDateTime toScaleUnitsWithFactor1(final Long valueInGridPixels) {
        return startDateTime.plusMinutes(valueInGridPixels);
    }

    @Override
    public OffsetDateTime getEndInScaleUnits() {
        return endDateTime;
    }

    @Override
    public Long factor() {
        return 60L;
    }
}
