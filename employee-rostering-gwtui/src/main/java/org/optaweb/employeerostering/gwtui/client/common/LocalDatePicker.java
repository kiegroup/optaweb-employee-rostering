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

package org.optaweb.employeerostering.gwtui.client.common;

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
import elemental2.dom.HTMLInputElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;

@Templated
public class LocalDatePicker implements TakesValue<LocalDate>, HasValueChangeHandlers<LocalDate> {

    @Inject
    @DataField("date-picker")
    private HTMLInputElement datePicker;
    
    private Collection<ValueChangeHandler<LocalDate>> valueChangeHandlers;
    
    @PostConstruct
    public void init() {
        valueChangeHandlers = new HashSet<>();
    }

    public boolean reportValidity() {
        return datePicker.reportValidity();
    }
    
    @Override
    public void setValue(LocalDate value) {
        datePicker.valueAsDate = new Date(Date.parse(value.toString()));
    }

    @Override
    public LocalDate getValue() {
        Date date;
        if (reportValidity()) {
            date = datePicker.valueAsDate;
            return GwtJavaTimeWorkaroundUtil.toLocalDate(OffsetDateTime.parse(date.toISOString()));
        } else {
            throw new ValidationException(datePicker.validationMessage);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fireEvent(GwtEvent<?> event) {
        if (event.getAssociatedType().equals(ValueChangeEvent.getType())) {
            valueChangeHandlers.forEach(h -> h.onValueChange((ValueChangeEvent<LocalDate>) event));
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<LocalDate> handler) {
        valueChangeHandlers.add(handler);
        return () -> valueChangeHandlers.remove(handler);
    }
    
    @EventHandler("date-picker")
    public void onDateChange(@ForEvent("change") Event e) {
        ValueChangeEvent.fire(this, getValue());
    }
}
