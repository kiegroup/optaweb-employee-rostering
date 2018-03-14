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

import java.time.OffsetDateTime;
import java.util.Optional;

import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.LinearScale;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;

public class ShiftBlob implements Blob<OffsetDateTime> {

    private final LinearScale<OffsetDateTime> scale;
    private Shift shift;
    private Long sizeInGridPixels;

    private Long positionInGridPixelsCache = null;
    private Long endPositionInGridPixelsCache = null;

    ShiftBlob(final LinearScale<OffsetDateTime> scale, final Shift shift) {
        this.shift = shift;
        this.scale = scale;
        this.sizeInGridPixels = scale.toGridPixels(shift.getEndDateTime()) - scale.toGridPixels(getPositionInScaleUnits());
    }

    @Override
    public long getSizeInGridPixels() {
        return sizeInGridPixels;
    }

    @Override
    public OffsetDateTime getPositionInScaleUnits() {
        return shift.getStartDateTime();
    }

    @Override
    public long getEndPositionInGridPixels() {

        //Collision performance optimization
        if (endPositionInGridPixelsCache == null) {
            endPositionInGridPixelsCache = Blob.super.getEndPositionInGridPixels();
        }

        return endPositionInGridPixelsCache;
    }

    @Override
    public long getPositionInGridPixels() {

        //Collision performance optimization
        if (positionInGridPixelsCache == null) {
            positionInGridPixelsCache = Blob.super.getPositionInGridPixels();
        }

        return positionInGridPixelsCache;
    }

    @Override
    public void setPositionInScaleUnits(final OffsetDateTime start) {
        positionInGridPixelsCache = null;
        endPositionInGridPixelsCache = null;
        shift.setStartDateTime(start);
    }

    @Override
    public void setSizeInGridPixels(final long sizeInGridPixels) {
        this.sizeInGridPixels = sizeInGridPixels;
        shift.setEndDateTime(getEndPositionInScaleUnits());
    }

    public String getLabel() {
        return Optional.ofNullable(shift.getEmployee())
                .map(Employee::getName)
                .orElse("Unassigned");
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(final Shift shift) {
        this.shift = shift;
    }

    @Override
    public LinearScale<OffsetDateTime> getScale() {
        return scale;
    }
}
