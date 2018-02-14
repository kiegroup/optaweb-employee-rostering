/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.spotroster;

import java.time.LocalDateTime;
import java.util.Optional;

import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.FiniteLinearScale;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;

public class ShiftBlob implements Blob<LocalDateTime> {

    private final Shift shift;
    private final FiniteLinearScale<LocalDateTime> scale;
    private Long sizeInGridPixels;

    public ShiftBlob(final FiniteLinearScale<LocalDateTime> scale, final Shift shift) {
        this.shift = shift;
        this.scale = scale;
        this.sizeInGridPixels = scale.toGridPixels(shift.getTimeSlot().getEndDateTime()) - scale.toGridPixels(getPosition());
    }

    @Override
    public Long getSizeInGridPixels() {
        return sizeInGridPixels;
    }

    @Override
    public LocalDateTime getPosition() {
        return shift.getTimeSlot().getStartDateTime();
    }

    @Override
    public void setPosition(final LocalDateTime start) {
        shift.getTimeSlot().setStartDateTime(start);
    }

    @Override
    public void setSizeInGridPixels(final Long sizeInGridPixels) {
        this.sizeInGridPixels = sizeInGridPixels;
        shift.getTimeSlot().setEndDateTime(getEndPosition(scale));
    }

    public String getLabel() {
        return Optional.ofNullable(shift.getEmployee())
                .map(Employee::getName)
                .orElse("U" + " [" + getPosition() + " ~ " + getEndPosition(scale) + ", " + getSizeInGridPixels() + "]");
    }

    public Shift getShift() {
        return shift;
    }
}
