package org.optaplanner.openshift.employeerostering.shared.common;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

public class TimeSlotUtils {

    public static LocalTime toLocalTime(OffsetDateTime time) {
        return LocalTime.of(time.getHour(), time.getMinute(), time.getSecond(), time.getNano());
    }

    public static boolean doTimeslotsIntersect(OffsetDateTime start1, OffsetDateTime end1, OffsetDateTime start2, OffsetDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    public static boolean doTimeslotsIntersect(LocalDate date, OffsetTime start1, OffsetTime end1, OffsetDateTime start2, OffsetDateTime end2) {
        return doTimeslotsIntersect(start1.atDate(date), end1.atDate(date), start2, end2);
    }
}
