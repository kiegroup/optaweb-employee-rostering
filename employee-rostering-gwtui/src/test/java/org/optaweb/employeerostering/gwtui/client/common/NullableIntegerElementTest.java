/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
import java.util.regex.Pattern;

import com.google.gwtmockito.GwtMockitoTestRunner;
import elemental2.dom.Event;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLLabelElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class NullableIntegerElementTest {

    @Mock
    private HTMLInputElement hasValueCheckbox;

    @Mock
    private HTMLInputElement valueInput;

    @Mock
    private HTMLLabelElement label;

    @Mock
    private Event event;

    @InjectMocks
    private NullableIntegerElement nullableIntegerElement;

    @Before
    public void setup() throws Exception {
        nullableIntegerElement.init();
        when(valueInput.checkValidity()).thenAnswer((args) -> {
            return Pattern.matches(valueInput.pattern, valueInput.value);
        });
        when(valueInput.reportValidity()).thenAnswer((args) -> {
            return Pattern.matches(valueInput.pattern, valueInput.value);
        });
    }

    @Test
    public void testEmptyValueIsValidIfHasValueIsFalse() {
        hasValueCheckbox.checked = false;
        valueInput.value = "";
        nullableIntegerElement.onValueChange(event);
        assertTrue(nullableIntegerElement.reportValidity());
        assertEquals(Optional.empty(), nullableIntegerElement.getValue());
    }

    @Test
    public void testEmptyValueIsInvalidIfHasValueIsTrue() {
        hasValueCheckbox.checked = true;
        valueInput.value = "";
        nullableIntegerElement.onValueChange(event);
        assertFalse(nullableIntegerElement.reportValidity());
    }

    @Test
    public void testValidValueIsValidIfHasValueIsFalse() {
        hasValueCheckbox.checked = false;
        valueInput.value = "10";
        nullableIntegerElement.onValueChange(event);
        assertTrue(nullableIntegerElement.reportValidity());
        assertEquals(Optional.empty(), nullableIntegerElement.getValue());
    }

    @Test
    public void testInvalidValueIsValidIfHasValueIsFalse() {
        hasValueCheckbox.checked = false;
        valueInput.value = "ABC";
        nullableIntegerElement.onValueChange(event);
        assertTrue(nullableIntegerElement.reportValidity());
        assertEquals(Optional.empty(), nullableIntegerElement.getValue());
    }

    @Test
    public void testInvalidValueIsInvalidIfHasValueIsTrue() {
        hasValueCheckbox.checked = true;
        valueInput.value = "ABC";
        nullableIntegerElement.onValueChange(event);
        assertFalse(nullableIntegerElement.reportValidity());
    }

    @Test
    public void testValidValueIsValidIfHasValueIsTrue() {
        hasValueCheckbox.checked = true;
        valueInput.value = "10";
        nullableIntegerElement.onValueChange(event);
        assertTrue(nullableIntegerElement.reportValidity());
        assertEquals(Optional.of(10), nullableIntegerElement.getValue());
    }

    @Test
    public void testNegativeValueIsInvalid() {
        hasValueCheckbox.checked = true;
        valueInput.value = "-2";
        nullableIntegerElement.onValueChange(event);
        assertFalse(nullableIntegerElement.reportValidity());
    }

    @Test
    public void testLeadingZeroValueIsInvalid() {
        hasValueCheckbox.checked = true;
        valueInput.value = "02";
        nullableIntegerElement.onValueChange(event);
        assertFalse(nullableIntegerElement.reportValidity());
    }
}
