package org.optaplanner.openshift.employeerostering.gwtui.client.util;

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
