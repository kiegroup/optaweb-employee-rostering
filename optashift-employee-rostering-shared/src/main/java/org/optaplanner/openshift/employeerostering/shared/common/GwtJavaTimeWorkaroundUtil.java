package org.optaplanner.openshift.employeerostering.shared.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

import com.google.gwt.core.client.JsDate;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.i18n.shared.DateTimeFormat.PredefinedFormat;

// WORKAROUND: Our client-side emulated java.time library lacks many features. This class contains methods to work-around these lack of feature
// REMOVE ME when GWT supports java.time or when we update our supersources at
// "/optashift-employee-rostering-gwtui/src/main/super/org/optaplanner/openshift/employeerostering/gwtui/emulate"
public class GwtJavaTimeWorkaroundUtil {

    public static LocalDateTime toLocalDateTime(OffsetDateTime time) {
        return LocalDateTime.of(toLocalDate(time), toLocalTime(time));
    }
    
    public static LocalDateTime toLocalDateTimeInZone(OffsetDateTime dateTime, ZoneId zoneId) {
        return LocalDateTime.ofEpochSecond(dateTime.toEpochSecond(), dateTime.getNano(),
                                           zoneId.getRules().getOffset(dateTime.toInstant()));
    }

    public static LocalDate toLocalDate(OffsetDateTime time) {
        return LocalDate.of(time.getYear(), time.getMonthValue(), time.getDayOfMonth());
    }

    public static LocalTime toLocalTime(OffsetDateTime time) {
        return toLocalTime(time.toOffsetTime());
    }

    public static LocalTime toLocalTime(OffsetTime time) {
        return LocalTime.of(time.getHour(), time.getMinute(), time.getSecond(), time.getNano());
    }

    public static OffsetDateTime toOffsetDateTime(LocalDate date, OffsetTime offsetTime) {
        return OffsetDateTime.of(date.atTime(toLocalTime(offsetTime)), offsetTime.getOffset());
    }

    public static boolean doTimeslotsIntersect(OffsetDateTime start1, OffsetDateTime end1, OffsetDateTime start2, OffsetDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }

    public static boolean doTimeslotsIntersect(LocalDate date, OffsetTime start1, OffsetTime end1, OffsetDateTime start2, OffsetDateTime end2) {
        return doTimeslotsIntersect(start1.atDate(date), end1.atDate(date), start2, end2);
    }

    public static Date toDate(OffsetDateTime offsetDateTime) {
        DateTimeFormat offsetDateTimeConvertor = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
        return offsetDateTimeConvertor.parse(offsetDateTimeToISO(offsetDateTime));
    }

    public static Date toDateAsLocalTime(OffsetDateTime offsetDateTime) {
        return toDate(toLocalDateTime(offsetDateTime));
    }

    public static Date toDate(LocalDateTime localDateTime) {
        DateTimeFormat offsetDateTimeConvertor = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
        return offsetDateTimeConvertor.parse(offsetDateTimeToISO(OffsetDateTime.of(localDateTime, ZoneOffset.ofTotalSeconds(60 * getOffsetInMinutes(localDateTime.toLocalDate(), ZoneOffset.UTC)))));
    }

    public static String offsetDateTimeToISO(OffsetDateTime offsetDateTime) {
        return toLocalDate(offsetDateTime).toString() + "T" + getGWTTime(toLocalTime(offsetDateTime)) + offsetDateTime.getOffset().toString();
    }

    public static String getGWTTime(LocalTime time) {
        return time.getHour() + ":" + time.getMinute() + ":" + time.getSecond() + "." + time.getNano();
    }

    public static int getOffsetInMinutes(LocalDate date, ZoneOffset offset) {
        return (offset.getTotalSeconds() / 60) - JsDate.create(date.getYear(), date.getMonthValue(), date.getDayOfMonth()).getTimezoneOffset();
    }

}
