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

import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.LinearScale;

public class PositiveHoursScale implements LinearScale<LocalDateTime> {

    private final LocalDateTime start;
    private final LocalDateTime end;

    public PositiveHoursScale(final LocalDateTime start, final LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public Long to(final LocalDateTime dateValue) {
        final LocalDateTime date;

        if (dateValue.isBefore(start)) {
            date = start;
        } else if (dateValue.isAfter(end)) {
            date = end;
        } else {
            date = dateValue;
        }

        return Duration.between(instantOf(start), instantOf(date)).getSeconds() / 60 / 60;
    }

    @Override
    public LocalDateTime from(final Long value) {
        final LocalDateTime date = start.plusHours(value);

        if (date.isBefore(start)) {
            return start;
        } else if (date.isAfter(end)) {
            return end;
        } else {
            return date;
        }
    }

    private Instant instantOf(final LocalDateTime value) {
        return value.toInstant(ZoneOffset.UTC); //FIXME: Configurable?
    }
}



