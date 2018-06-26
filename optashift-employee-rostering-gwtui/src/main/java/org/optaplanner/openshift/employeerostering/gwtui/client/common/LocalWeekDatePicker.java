package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.validation.ValidationException;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.TakesValue;
import elemental2.core.Date;
import elemental2.dom.Event;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;

@Templated
public class LocalWeekDatePicker implements TakesValue<LocalDate>, HasValueChangeHandlers<LocalDate> {

    @Inject
    @DataField("date-picker")
    private HTMLInputElement datePicker;

    @Inject
    @DataField("previous-week-button")
    private HTMLButtonElement previousWeekButton;

    @Inject
    @DataField("next-week-button")
    private HTMLButtonElement nextWeekButton;

    private Collection<ValueChangeHandler<LocalDate>> valueChangeHandlers;

    // TODO: Load this from server
    private static final DayOfWeek START_OF_WEEK = DayOfWeek.SUNDAY;

    @PostConstruct
    public void init() {
        valueChangeHandlers = new HashSet<>();
    }

    @Override
    public void setValue(LocalDate value) {
        datePicker.valueAsDate = new Date(Date.parse(getFirstDayOfWeek(value).toString()));
    }

    @Override
    public LocalDate getValue() {
        Date date;
        datePicker.checkValidity();
        if (datePicker.validity.valid) {
            try {
                date = datePicker.valueAsDate;
            } catch (Exception e) {
                // Browser does not support input type date/time; need to convert String to Date
                date = new Date(Date.parse(datePicker.value));
            }
            return getFirstDayOfWeek(GwtJavaTimeWorkaroundUtil.toLocalDate(OffsetDateTime.parse(date.toISOString())));
        } else {
            datePicker.reportValidity();
            throw new ValidationException(datePicker.validationMessage);
        }
    }

    private LocalDate getFirstDayOfWeek(LocalDate dayInWeek) {
        while (dayInWeek.getDayOfWeek() != START_OF_WEEK) {
            dayInWeek = dayInWeek.minusDays(1);
        }
        return dayInWeek;
    }

    @EventHandler("date-picker")
    public void onDateChange(@ForEvent("change") Event e) {
        datePicker.valueAsDate = new Date(Date.parse(getFirstDayOfWeek(getValue()).toString()));
        ValueChangeEvent.fire(this, getValue());
    }

    @EventHandler("previous-week-button")
    public void onPreviousWeekButtonClick(@ForEvent("click") MouseEvent e) {
        setValue(getValue().minusDays(7));
        ValueChangeEvent.fire(this, getValue());
    }

    @EventHandler("next-week-button")
    public void onNextWeekButtonClick(@ForEvent("click") MouseEvent e) {
        setValue(getValue().plusDays(7));
        ValueChangeEvent.fire(this, getValue());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fireEvent(GwtEvent<?> event) {
        if (event.getAssociatedType().equals(ValueChangeEvent.getType())) {
            valueChangeHandlers.forEach(handler -> handler.onValueChange((ValueChangeEvent<LocalDate>) event));
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<LocalDate> handler) {
        valueChangeHandlers.add(handler);
        return () -> valueChangeHandlers.remove(handler);
    }
}
