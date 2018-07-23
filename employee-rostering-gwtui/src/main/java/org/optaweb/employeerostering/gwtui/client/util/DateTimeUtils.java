/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

public class DateTimeUtils {

    public static int SECONDS_PER_MINUTE = 60;
    public static int MINUTES_PER_HOUR = 60;
    public static int HOURS_PER_DAY = 24;

    // We don't have Temporal, so we kinda need to do every combination...
    public static int daysBetween(LocalDate a, OffsetDateTime b) {
        return (int) (Duration.between(OffsetDateTime.of(a.atTime(LocalTime.MIDNIGHT), b.getOffset()), b).getSeconds() / (SECONDS_PER_MINUTE * MINUTES_PER_HOUR * HOURS_PER_DAY));
    }

    public static LocalTime getLocalTimeOf(OffsetDateTime dateTime) {
        return LocalTime.of(dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond(), dateTime.getNano());
    }

}
