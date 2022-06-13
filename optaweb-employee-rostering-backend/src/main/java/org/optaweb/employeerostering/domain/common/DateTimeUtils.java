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
