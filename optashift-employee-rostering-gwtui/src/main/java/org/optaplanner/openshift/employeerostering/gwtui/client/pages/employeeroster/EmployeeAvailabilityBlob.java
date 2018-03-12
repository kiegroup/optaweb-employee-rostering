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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.employeeroster;

import java.time.OffsetDateTime;

import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.LinearScale;
import org.optaplanner.openshift.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;

public class EmployeeAvailabilityBlob implements Blob<OffsetDateTime> {

    private final LinearScale<OffsetDateTime> scale;
    private EmployeeAvailability availability;
    private Long sizeInGridPixels;

    private Long positionInGridPixelsCache = null;
    private Long endPositionInGridPixelsCache = null;

    EmployeeAvailabilityBlob(final LinearScale<OffsetDateTime> scale, final EmployeeAvailability availability) {
        this.availability = availability;
        this.scale = scale;
        this.sizeInGridPixels = scale.toGridPixels(availability.getEndTime().atDate(availability.getDate())) - scale.toGridPixels(getPositionInScaleUnits());
    }

    @Override
    public long getSizeInGridPixels() {
        return sizeInGridPixels;
    }

    @Override
    public OffsetDateTime getPositionInScaleUnits() {
        return availability.getStartTime().atDate(availability.getDate());
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
        availability.setDate(GwtJavaTimeWorkaroundUtil.toLocalDate(start));
        availability.setStartTime(start.toOffsetTime());
    }

    @Override
    public void setSizeInGridPixels(final long sizeInGridPixels) {
        this.sizeInGridPixels = sizeInGridPixels;
        OffsetDateTime end = getEndPositionInScaleUnits();
        availability.setEndTime(end.toOffsetTime());
    }

    public String getLabel() {
        return availability.getState().toString() + ": " + availability.getStartTime() + "-" + availability.getEndTime();
    }

    public EmployeeAvailability getAvailability() {
        return availability;
    }

    public void setShift(final EmployeeAvailability shift) {
        this.availability = shift;
    }

    @Override
    public LinearScale<OffsetDateTime> getScale() {
        return scale;
    }
}
