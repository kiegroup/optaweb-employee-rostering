package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import javax.inject.Inject;
import javax.validation.ValidationException;

import com.google.gwt.user.client.TakesValue;
import elemental2.core.Date;
import elemental2.dom.HTMLInputElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;

@Templated
public class LocalDateTimePicker implements TakesValue<LocalDateTime> {

    @Inject
    @DataField("date-picker")
    private HTMLInputElement datePicker;

    @Inject
    @DataField("time-picker")
    private HTMLInputElement timePicker;

    @Override
    public void setValue(LocalDateTime value) {
        datePicker.valueAsDate = new Date(Date.parse(value.toLocalDate().toString()));
        timePicker.value = value.toLocalTime().toString();
    }

    @Override
    public LocalDateTime getValue() {
        Date date;
        datePicker.checkValidity();
        timePicker.checkValidity();
        if (datePicker.validity.valid && timePicker.validity.valid) {
            date = datePicker.valueAsDate;
            LocalTime localTime = LocalTime.parse(timePicker.value);
            LocalDate localDate = GwtJavaTimeWorkaroundUtil.toLocalDate(OffsetDateTime.parse(date.toISOString()));
            return localDate.atTime(localTime);
        } else {
            datePicker.reportValidity();
            timePicker.reportValidity();
            if (!datePicker.validity.valid) {
                throw new ValidationException(datePicker.validationMessage);
            } else {
                throw new ValidationException(timePicker.validationMessage);
            }
        }
    }
}
