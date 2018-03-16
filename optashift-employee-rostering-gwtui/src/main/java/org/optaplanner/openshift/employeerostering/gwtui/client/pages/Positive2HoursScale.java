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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;

import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.LinearScale;

public class Positive2HoursScale implements LinearScale<OffsetDateTime> {

    private final OffsetDateTime start;
    private final OffsetDateTime end;

    public Positive2HoursScale(final OffsetDateTime start, final OffsetDateTime end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Long toGridPixelsWithFactor1(final OffsetDateTime valueInScaleUnits) {
        final OffsetDateTime dateTime;

        if (valueInScaleUnits.isBefore(start)) {
            dateTime = start;
        } else if (valueInScaleUnits.isAfter(end)) {
            dateTime = end;
        } else {
            dateTime = valueInScaleUnits;
        }

        // .get(ChronoUnit.HOURS) does not work
        return Duration.between(instantOf(start), instantOf(dateTime)).getSeconds() / 60 / 60;
    }

    @Override
    public OffsetDateTime toScaleUnitsWithFactor1(final Long valueInGridPixels) {

        final OffsetDateTime date = start.plusHours(valueInGridPixels);

        if (date.isBefore(start)) {
            return start;
        } else if (date.isAfter(end)) {
            return end;
        } else {
            return date;
        }
    }

    @Override
    public OffsetDateTime getEndInScaleUnits() {
        return end;
    }

    @Override
    public Long factor() {
        return 2L;
    }

    private Instant instantOf(final OffsetDateTime value) {
        return value.toInstant();
    }
}
