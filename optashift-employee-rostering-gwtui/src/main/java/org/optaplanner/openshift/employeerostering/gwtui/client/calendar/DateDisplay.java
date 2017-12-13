package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants;
import org.osgi.service.component.annotations.Component;

public enum DateDisplay {
    WEEKS_FROM_EPOCH,
    WEEK_STARTING,
    WEEK_ENDING;

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
                        "A fatal error has occurred in the application, please forward the following\n"
                                + "message and stack trace to the current maintainer of the project:\n\n"
                                + "DataDisplay." + this.name() + " is missing a case in"
                                + " DateDisplay.format(LocalDateTime,int,TranslationService)");
        }
    }
}
