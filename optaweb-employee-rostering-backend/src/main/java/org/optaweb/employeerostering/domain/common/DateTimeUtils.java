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

package org.optaweb.employeerostering.domain.common;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.IsoFields;

public class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static LocalDateTime toLocalDateTimeInZone(OffsetDateTime dateTime, ZoneId zoneId) {
        return LocalDateTime.ofEpochSecond(dateTime.toEpochSecond(), dateTime.getNano(),
                                           zoneId.getRules().getOffset(dateTime.toInstant()));
    }

    public static boolean sameWeek(DayOfWeek weekStarting, OffsetDateTime dateTime1, OffsetDateTime dateTime2) {
        // ISO-8601 weeks begin on Monday, so we shift dates that begin on weekStarting to Monday
        // To get a week numbering system that use weekStarting instead of Monday
        int dayDifference = weekStarting.getValue() - 1;
        OffsetDateTime first = dateTime1.minusDays(dayDifference);
        OffsetDateTime second = dateTime2.minusDays(dayDifference);
        return first.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == second.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
                && first.get(IsoFields.WEEK_BASED_YEAR) == second.get(IsoFields.WEEK_BASED_YEAR);
    }
}
