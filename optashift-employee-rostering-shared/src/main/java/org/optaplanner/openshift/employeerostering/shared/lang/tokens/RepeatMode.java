package org.optaplanner.openshift.employeerostering.shared.lang.tokens;

/**
 * Specifies when shifts should be repeated.
 */
public enum RepeatMode {
    /**
     * Shifts are repeated everyday. If used,
     * all shifts should be between {@code LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)}
     * and {@code LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC).plusDays(1)}
     */
    DAY(1, 0, 0, 0),
    /**
     * Shifts are repeated week. If used,
     * all shifts should be between {@code LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC)}
     * and {@code LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC).plusDays(7)}
     */
    WEEK(0, 1, 0, 0),
    /**
     * Shifts are repeated every month. Should only be used
     * for shifts that fall on specific days every month.
     * Ex: the 1st and 15th of each month
     */
    MONTH(0, 0, 1, 0),
    /**
     * Shifts are repeated every year. This does NOT add
     * another shift for leap years
     */
    YEAR(0, 0, 0, 1),
    /**
     * Shifts are not repeated.
     */
    NONE(0, 0, 0, 0);

    public long daysUntilRepeat, weeksUntilRepeat, monthsUntilRepeat, yearsUntilRepeat;

    RepeatMode(long days, long weeks, long months, long years) {
        this.daysUntilRepeat = days;
        this.weeksUntilRepeat = weeks;
        this.monthsUntilRepeat = months;
        this.yearsUntilRepeat = years;
    }
}