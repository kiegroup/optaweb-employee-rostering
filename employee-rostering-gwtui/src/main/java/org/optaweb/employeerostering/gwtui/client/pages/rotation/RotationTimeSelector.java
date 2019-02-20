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

import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.IntegerBox;
import org.gwtbootstrap3.client.ui.form.validator.BlankValidator;
import org.gwtbootstrap3.client.ui.form.validator.DecimalMaxValidator;
import org.gwtbootstrap3.client.ui.form.validator.DecimalMinValidator;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.common.LocalTimePicker;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaweb.employeerostering.shared.roster.RosterState;

@Templated
public class RotationTimeSelector {

    @DataField("day-offset")
    private IntegerBox dayOffsetPicker;

    @DataField("time")
    private LocalTimePicker timePicker;

    private int rotationLength;

    private int offset;

    @Inject
    public RotationTimeSelector(IntegerBox dayOffsetPicker, LocalTimePicker timePicker, TenantStore tenantStore) {
        offset = 0;
        this.dayOffsetPicker = dayOffsetPicker;
        this.timePicker = timePicker;
        RosterRestServiceBuilder.getRosterState(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(this::setRotationLength));
    }

    protected void setRotationLength(RosterState rs) {
        rotationLength = rs.getRotationLength();
        dayOffsetPicker.addValidator(new BlankValidator<Integer>());
        dayOffsetPicker.addValidator(new DecimalMinValidator<Integer>(1));
        dayOffsetPicker.addValidator(new DecimalMaxValidator<Integer>(rotationLength));
        setDayOffset(offset);
    }

    public boolean reportValidity() {
        return dayOffsetPicker.validate(true) && timePicker.reportValidity();
    }

    public void setDayOffset(int dayOffset) {
        offset = dayOffset;
        dayOffsetPicker.setValue((dayOffset % rotationLength) + 1);
    }

    public void setTime(LocalTime time) {
        timePicker.setValue(time);
    }

    public int getDayOffset() {
        return dayOffsetPicker.getValue() - 1;
    }

    public int getRotationLength() {
        return rotationLength;
    }

    public LocalTime getTime() {
        return timePicker.getValue();
    }
}
