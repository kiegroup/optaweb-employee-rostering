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

import com.google.gwtmockito.GwtMockitoTestRunner;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLInputElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(GwtMockitoTestRunner.class)
public class LocalTimePickerTest {

    @Mock
    private HTMLInputElement datePickerTextField;

    @Mock
    private HTMLDivElement datePickerElement;

    @InjectMocks
    private LocalTimePicker testedLocalTimePicker;

    @Before
    public void setUp() throws Exception {
        testedLocalTimePicker.init();
    }

    // Hard to Unit Test this due to how JS Interops work; since this is a wrapper
    // class for a Javascript Object, the class depends on Javascript (and hence,
    // static methods which we cannot mock).
    @Test
    public void testReportValidity() {
        boolean out = testedLocalTimePicker.reportValidity();
        verify(datePickerTextField).reportValidity();
        assertThat(out).isEqualTo(datePickerTextField.reportValidity());
    }
}
