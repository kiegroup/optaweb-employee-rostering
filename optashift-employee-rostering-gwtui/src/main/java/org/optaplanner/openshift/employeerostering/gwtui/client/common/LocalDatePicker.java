package org.optaplanner.openshift.employeerostering.gwtui.client.common;

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
import org.optaplanner.openshift.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;

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
            try {
                date = datePicker.valueAsDate;
            } catch (Exception e) {
                // Browser does not support input type date/time; need to convert String to Date
                date = new Date(Date.parse(datePicker.value));
            }
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
