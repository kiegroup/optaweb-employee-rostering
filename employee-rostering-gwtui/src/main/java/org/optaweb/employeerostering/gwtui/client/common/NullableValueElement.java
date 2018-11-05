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

import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;

import com.google.gwt.user.client.TakesValue;
import elemental2.dom.Event;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLLabelElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Templated
public abstract class NullableValueElement<T> implements TakesValue<Optional<T>> {

    @Inject
    @DataField("has-value-checkbox")
    private HTMLInputElement hasValueCheckbox;

    @Inject
    @DataField("value")
    protected HTMLInputElement valueInput;

    @Inject
    @DataField("label")
    private HTMLLabelElement label;

    private Optional<T> value;

    protected Function<String, T> valueConverter;

    @EventHandler("has-value-checkbox")
    public void onHasValueCheckboxUpdate(@ForEvent("change") Event e) {
        valueInput.disabled = !hasValueCheckbox.checked;
        if (valueInput.disabled) {
            value = Optional.empty();
            valueInput.required = false;
        } else {
            value = getValueFromInput();
            valueInput.required = true;
        }
    }

    @EventHandler("value")
    public void onValueChange(@ForEvent("change") Event e) {
        value = getValueFromInput();
    }

    public void setup(T value, String labelText) {
        this.label.innerHTML = labelText;
        setValue(Optional.ofNullable(value));
    }

    @Override
    public void setValue(Optional<T> value) {
        this.value = value;
        if (value.isPresent()) {
            hasValueCheckbox.checked = true;
            valueInput.required = true;
            valueInput.disabled = false;
            valueInput.value = value.get().toString();
        } else {
            hasValueCheckbox.checked = false;
            valueInput.required = false;
            valueInput.disabled = true;
            valueInput.value = "";
        }
    }

    @Override
    public Optional<T> getValue() {
        return value;
    }

    public boolean reportValidity() {
        return !hasValueCheckbox.checked || valueInput.reportValidity();
    }

    private Optional<T> getValueFromInput() {
        if (!hasValueCheckbox.checked || valueInput.value.isEmpty() || !valueInput.checkValidity()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(valueConverter.apply(valueInput.value));
        }
    }

    public HTMLInputElement getValueInput() {
        return valueInput;
    }
}
