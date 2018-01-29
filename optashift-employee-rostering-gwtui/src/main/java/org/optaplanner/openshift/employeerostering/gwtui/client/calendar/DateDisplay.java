package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants;

/**
 * Formats a date to be displayed.
 */
public enum DateDisplay {
    /**
     * Display dates as the number of weeks since epoch (used in templates).
     */
    WEEKS_FROM_EPOCH,
    /**
     * Display dates as "Week Starting (first day of week date is contained in)".
     */
    WEEK_STARTING,

    /**
     * Display dates as "Week Ending (last day of week date is contained in)".
     */
    WEEK_ENDING;

    /**
     * Formats a date.
     * @param date The date to format.
     * @param startOfWeekOffset How many days after Monday is the start of the week 
     * (strictly positive) + 1 (i.e. DayOfWeek.getValue() for the first day of a week).
     * @param translator Translates the date.
     * @return The formatted date.
     */
    public String format(LocalDateTime date, int startOfWeekOffset, TranslationService translator) {
        switch (this) {
            case WEEKS_FROM_EPOCH:
                return translator.format(OptaShiftUIConstants.DateDisplay_WEEKS_FROM_EPOCH, 1 + date
                                                                                                    .toEpochSecond(
                                                                                                                   ZoneOffset.UTC) / (60 * 60 * 24 * 7));
            case WEEK_STARTING:
                LocalDateTime weekStart = date;
                while (weekStart.getDayOfWeek().getValue() != startOfWeekOffset) {
                    weekStart = weekStart.minusDays(1);
                }
                return translator.format(OptaShiftUIConstants.DateDisplay_WEEK_STARTING, weekStart.getYear(), weekStart
                                                                                                                       .getMonth().getValue(), weekStart.getDayOfMonth());
            case WEEK_ENDING:
                LocalDateTime weekEnd = date;
                while ((weekEnd.getDayOfWeek().getValue() + 1) % 7 != startOfWeekOffset) {
                    weekEnd = weekEnd.plusDays(1);
                }
                return translator.format(OptaShiftUIConstants.DateDisplay_WEEK_ENDING, weekEnd
                                                                                              .getYear(), weekEnd
                                                                                                                 .getMonth().getValue(), weekEnd.getDayOfMonth());
            default:
                throw new IllegalStateException(
                                                "A fatal error has occurred in the application, please forward the following\n" + "message and stack trace to the current maintainer of the project:\n\n" + "DataDisplay." +
                                                this.name() + " is missing a case in" + " DateDisplay.format(LocalDateTime,int,TranslationService)");
        }
    }
}
