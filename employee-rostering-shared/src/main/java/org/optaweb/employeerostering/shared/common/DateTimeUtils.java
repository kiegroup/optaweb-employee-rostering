/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.shared.common;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

public class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static boolean doTimeslotsIntersect(OffsetDateTime start1, OffsetDateTime end1, OffsetDateTime start2, OffsetDateTime end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

    public static boolean doTimeslotsIntersect(LocalDate date, OffsetTime start1, OffsetTime end1, OffsetDateTime start2, OffsetDateTime end2) {
        return doTimeslotsIntersect(start1.atDate(date), end1.atDate(date), start2, end2);
    }

    public static long dayOf(OffsetDateTime dateTime) {
        OffsetDateTime epochDate = OffsetDateTime.ofInstant(Instant.ofEpochMilli(0), dateTime.getOffset());
        return Duration.between(epochDate, dateTime).toDays();
    }

    public static long weekOf(DayOfWeek weekStarting, OffsetDateTime dateTime) {
        OffsetDateTime epochDate = OffsetDateTime.ofInstant(Instant.ofEpochMilli(0), dateTime.getOffset());
        OffsetDateTime firstDate = epochDate.plusDays((long) weekStarting.getValue() - epochDate.getDayOfWeek().getValue()).minusDays(1);
        return Duration.between(firstDate, dateTime).toDays() / 7;
    }

    public static long monthOf(OffsetDateTime dateTime) {
        return dateTime.getYear() * 12L + dateTime.getMonthValue();
    }

    public static long yearOf(OffsetDateTime dateTime) {
        return dateTime.getYear();
    }
}
