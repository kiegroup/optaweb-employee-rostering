/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.gwtui.client.common;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.TakesValue;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.util.DateTimeUtils;

@Templated
public class LocalWeekDatePicker
        implements
        TakesValue<LocalDate>,
        HasValueChangeHandlers<LocalDate> {

    @Inject
    @DataField("weekly-date-picker")
    private HTMLDivElement weeklyDatePickerElement;

    @Inject
    @DataField("weekly-date-picker-text-field")
    private HTMLInputElement weeklyDatePickerTextField;

    @Inject
    @DataField("previous-week-button")
    private HTMLAnchorElement previousWeekButton;

    @Inject
    @DataField("next-week-button")
    private HTMLAnchorElement nextWeekButton;

    @Inject
    private TranslationService translationService;

    @Inject
    private DateTimeUtils dateTimeUtils;

    private BootstrapDateTimePicker weeklyDatePicker;
    private LocalDate firstDayOfWeek;

    private Collection<ValueChangeHandler<LocalDate>> valueChangeHandlers;

    // TODO: Load this from server
    private static final DayOfWeek START_OF_WEEK = DayOfWeek.SUNDAY;
    private static final String DATE_FORMAT_STRING = "L";

    @PostConstruct
    public void init() {
        valueChangeHandlers = new HashSet<>();
        weeklyDatePicker = BootstrapDateTimePicker.get(weeklyDatePickerElement);
        BootstrapDateTimePicker.BootstrapDateTimePickerOptions options = new BootstrapDateTimePicker.BootstrapDateTimePickerOptions();
        options.format = DATE_FORMAT_STRING;
        options.showTodayButton = true;
        options.allowInputToggle = true;
        BootstrapDateTimePicker.BootstrapDateTimePickerIcons icons = new BootstrapDateTimePicker.BootstrapDateTimePickerIcons();
        icons.today = "today-button-pf";
        options.icons = icons;
        weeklyDatePicker.datetimepicker(options);
        weeklyDatePicker.on("dp.show", () -> {
            JQuery.get(weeklyDatePickerElement).children(".bootstrap-datetimepicker-widget").css("height", "min-content").css("margin-bottom", "-100%")
                    .css("bottom", "unset");
            JQuery.select("tr:has(td.active)").children("td").addClass("active");
            String firstDate = MomentJs.moment(weeklyDatePickerTextField.value, DATE_FORMAT_STRING).day(0).format(DATE_FORMAT_STRING);
            String lastDate = MomentJs.moment(weeklyDatePickerTextField.value, DATE_FORMAT_STRING).day(6).format(DATE_FORMAT_STRING);
            weeklyDatePickerTextField.value = firstDate + " - " + lastDate;
        });
        weeklyDatePicker.on("dp.change", () -> {
            String value = weeklyDatePickerTextField.value;
            LocalDate oldValue = getValue();
            if (value != null && !value.isEmpty()) {
                String firstDate = MomentJs.moment(value, DATE_FORMAT_STRING).day(0).format(DATE_FORMAT_STRING);
                String lastDate = MomentJs.moment(value, DATE_FORMAT_STRING).day(6).format(DATE_FORMAT_STRING);
                setValue(LocalDate.parse(MomentJs.moment(value).day(0).format("YYYY-MM-DD")));
                weeklyDatePickerTextField.value = firstDate + " - " + lastDate;
            }
            ValueChangeEvent.fireIfNotEqual(this, oldValue, firstDayOfWeek);
        });
    }

    @Override
    public void setValue(LocalDate value) {
        if (firstDayOfWeek == null || !firstDayOfWeek.equals(value)) {
            if (value != null) {
                firstDayOfWeek = dateTimeUtils.getFirstDateOfWeek(value);
                ((BootstrapDateTimePicker.BootstrapDateTimePickerData) weeklyDatePicker.data("DateTimePicker")).date(firstDayOfWeek.toString());
                String firstDate = MomentJs.moment(value.toString()).day(0).format(DATE_FORMAT_STRING);
                String lastDate = MomentJs.moment(value.toString()).day(6).format(DATE_FORMAT_STRING);
                weeklyDatePickerTextField.value = firstDate + " - " + lastDate;
                JQuery.select("tr:has(td.active)").children("td").addClass("active");
            } else {
                ((BootstrapDateTimePicker.BootstrapDateTimePickerData) weeklyDatePicker.data("DateTimePicker")).date((String) null);
                weeklyDatePickerTextField.value = "";
            }
        }
    }

    @Override
    public LocalDate getValue() {
        return firstDayOfWeek;
    }

    public LocalDate getFirstDateOfWeek() {
        return firstDayOfWeek;
    }

    public LocalDate getLastDateOfWeek() {
        return firstDayOfWeek.plusDays(6);
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
