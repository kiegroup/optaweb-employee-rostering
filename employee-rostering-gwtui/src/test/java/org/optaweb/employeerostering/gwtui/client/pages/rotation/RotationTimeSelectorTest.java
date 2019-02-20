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

package org.optaweb.employeerostering.gwtui.client.pages.rotation;

import java.time.LocalTime;

import com.google.gwtmockito.GwtMockitoTestRunner;
import org.gwtbootstrap3.client.ui.IntegerBox;
import org.gwtbootstrap3.client.ui.form.validator.DecimalMaxValidator;
import org.gwtbootstrap3.client.ui.form.validator.DecimalMinValidator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.optaweb.employeerostering.gwtui.client.common.LocalTimePicker;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.shared.roster.RosterState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(GwtMockitoTestRunner.class)
public class RotationTimeSelectorTest {

    @Mock
    private IntegerBox dayOffsetPicker;

    @Mock
    private LocalTimePicker timePicker;

    @Mock
    private TenantStore tenantStore;

    private RotationTimeSelector testedRotationTimeSelector;

    private Integer dayOffset;

    private LocalTime localTime;

    private final int ROTATION_LENGTH = 7;

    @Before
    public void setUp() throws Exception {
        doAnswer(v -> {
            dayOffset = v.getArgument(0);
            return null;
        }).when(dayOffsetPicker).setValue(any());

        when(dayOffsetPicker.getValue()).thenAnswer(v -> {
            return dayOffset;
        });

        doAnswer(v -> {
            localTime = v.getArgument(0);
            return null;
        }).when(timePicker).setValue(any());

        when(timePicker.getValue()).thenAnswer(v -> {
            return localTime;
        });

        testedRotationTimeSelector = spy(new RotationTimeSelector(dayOffsetPicker, timePicker, tenantStore));
        RosterState rs = new RosterState();
        rs.setRotationLength(ROTATION_LENGTH);
        testedRotationTimeSelector.setRotationLength(rs);
    }

    @Test
    public void testInit() {
        verify(dayOffsetPicker).addValidator(any(DecimalMinValidator.class));
        verify(dayOffsetPicker).addValidator(any(DecimalMaxValidator.class));
        verify(testedRotationTimeSelector).setDayOffset(0);
        assertThat(testedRotationTimeSelector.getRotationLength()).isEqualTo(ROTATION_LENGTH);
    }

    @Test
    public void testGetSetDayOffset() {
        InOrder callOrder = inOrder(dayOffsetPicker);
        assertThat(testedRotationTimeSelector.getDayOffset()).isEqualTo(0);
        callOrder.verify(dayOffsetPicker).getValue();
        testedRotationTimeSelector.setDayOffset(5);
        callOrder.verify(dayOffsetPicker).setValue(eq(6));
        assertThat(testedRotationTimeSelector.getDayOffset()).isEqualTo(5);
        callOrder.verify(dayOffsetPicker).getValue();
        testedRotationTimeSelector.setDayOffset(7);
        callOrder.verify(dayOffsetPicker).setValue(eq(1));
        assertThat(testedRotationTimeSelector.getDayOffset()).isEqualTo(0);
        callOrder.verify(dayOffsetPicker).getValue();
        testedRotationTimeSelector.setDayOffset(10);
        callOrder.verify(dayOffsetPicker).setValue(eq(4));
        assertThat(testedRotationTimeSelector.getDayOffset()).isEqualTo(3);
        callOrder.verify(dayOffsetPicker).getValue();
    }

    @Test
    public void testGetSetTime() {
        InOrder callOrder = inOrder(timePicker);
        testedRotationTimeSelector.setTime(LocalTime.MIDNIGHT);
        callOrder.verify(timePicker).setValue(eq(LocalTime.MIDNIGHT));
        assertThat(testedRotationTimeSelector.getTime()).isEqualTo(LocalTime.MIDNIGHT);
        callOrder.verify(timePicker).getValue();

        testedRotationTimeSelector.setTime(LocalTime.NOON);
        callOrder.verify(timePicker).setValue(eq(LocalTime.NOON));
        assertThat(testedRotationTimeSelector.getTime()).isEqualTo(LocalTime.NOON);
        callOrder.verify(timePicker).getValue();
    }

    @Test
    public void testValidReportValidity() {
        when(timePicker.reportValidity()).thenReturn(true);
        when(dayOffsetPicker.validate(true)).thenReturn(true);

        boolean isValid = testedRotationTimeSelector.reportValidity();

        assertThat(isValid).isTrue();
        verify(timePicker).reportValidity();
        verify(dayOffsetPicker).validate(true);
    }

    @Test
    public void testInvalidTimeReportValidity() {
        when(timePicker.reportValidity()).thenReturn(false);
        when(dayOffsetPicker.validate(true)).thenReturn(true);

        boolean isValid = testedRotationTimeSelector.reportValidity();

        assertThat(isValid).isFalse();
        verify(timePicker).reportValidity();
    }

    @Test
    public void testInvalidOffsetReportValidity() {
        when(timePicker.reportValidity()).thenReturn(true);
        when(dayOffsetPicker.validate(true)).thenReturn(false);

        boolean isValid = testedRotationTimeSelector.reportValidity();

        assertThat(isValid).isFalse();
        verify(dayOffsetPicker).validate(true);
    }
}
