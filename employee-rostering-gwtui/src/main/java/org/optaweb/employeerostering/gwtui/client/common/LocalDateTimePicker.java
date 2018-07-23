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
import org.optaweb.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;

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
