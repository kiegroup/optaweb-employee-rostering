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

package org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.demo;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.function.Function;

import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.FiniteLinearScale;

public class Positive2HoursScale implements FiniteLinearScale<LocalDateTime> {

    private final LocalDateTime start;
    private final LocalDateTime end;

    public Positive2HoursScale(final LocalDateTime start, final Function<LocalDateTime, LocalDateTime> end) {
        this.start = start;
        this.end = end.apply(start);
    }

    @Override
    public Long toGridPixelsWithFactor1(final LocalDateTime valueInScaleUnits) {
        final LocalDateTime date;

        if (valueInScaleUnits.isBefore(start)) {
            date = start;
        } else if (valueInScaleUnits.isAfter(end)) {
            date = end;
        } else {
            date = valueInScaleUnits;
        }

        return Duration.between(instantOf(start), instantOf(date)).getSeconds() / 60 / 60;
    }

    @Override
    public LocalDateTime fromGridPixelsWithFactor1(final Long valueInGridPixels) {

        final LocalDateTime date = start.plusHours(valueInGridPixels);

        if (date.isBefore(start)) {
            return start;
        } else if (date.isAfter(end)) {
            return end;
        } else {
            return date;
        }
    }

    @Override
    public Long factor() {
        return 2L;
    }

    @Override
    public LocalDateTime getEnd() {
        return end;
    }

    private Instant instantOf(final LocalDateTime value) {
        return value.toInstant(ZoneOffset.UTC); //FIXME: Configurable?
    }
}



