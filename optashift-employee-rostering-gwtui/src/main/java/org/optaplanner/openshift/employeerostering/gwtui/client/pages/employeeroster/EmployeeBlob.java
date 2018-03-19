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
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;

public class EmployeeBlob implements Blob<OffsetDateTime> {

    private final LinearScale<OffsetDateTime> scale;
    private EmployeeAvailability employeeAvailability;
    private Shift shift;
    private Long sizeInGridPixels;

    private Long positionInGridPixelsCache = null;
    private Long endPositionInGridPixelsCache = null;

    public EmployeeBlob(final LinearScale<OffsetDateTime> scale, final Shift shift) {
        this(scale, null, shift);
    }

    public EmployeeBlob(final LinearScale<OffsetDateTime> scale, final EmployeeAvailability employeeAvailability) {
        this(scale, employeeAvailability, null);
    }

    private EmployeeBlob(final LinearScale<OffsetDateTime> scale, final EmployeeAvailability employeeAvailability, final Shift shift) {
        this.employeeAvailability = employeeAvailability;
        this.shift = shift;
        this.scale = scale;
        this.sizeInGridPixels = scale.toGridPixels(getEndDateTime()) - scale.toGridPixels(getStartDateTime());
    }

    private OffsetDateTime getStartDateTime() {
        if (null != shift) {
            return shift.getStartDateTime();
        } else {
            return GwtJavaTimeWorkaroundUtil.toOffsetDateTime(employeeAvailability.getDate(), employeeAvailability.getStartTime());
        }
    }

    private OffsetDateTime getEndDateTime() {
        if (null != shift) {
            return shift.getEndDateTime();
        } else {
            return GwtJavaTimeWorkaroundUtil.toOffsetDateTime(employeeAvailability.getDate(), employeeAvailability.getEndTime());
        }
    }

    private void setStartDateTime(OffsetDateTime startDateTime) {
        if (null != shift) {
            shift.setStartDateTime(startDateTime);
        } else {
            employeeAvailability.setDate(GwtJavaTimeWorkaroundUtil.toLocalDate(startDateTime));
            employeeAvailability.setStartTime(startDateTime.toOffsetTime());
        }
    }

    private void setEndDateTime(OffsetDateTime endDateTime) {
        if (null != shift) {
            shift.setEndDateTime(endDateTime);
        } else {
            employeeAvailability.setDate(GwtJavaTimeWorkaroundUtil.toLocalDate(endDateTime));
            employeeAvailability.setEndTime(endDateTime.toOffsetTime());
        }
    }

    @Override
    public long getSizeInGridPixels() {
        return sizeInGridPixels;
    }

    @Override
    public OffsetDateTime getPositionInScaleUnits() {
        return getStartDateTime();
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
        setStartDateTime(start);
    }

    @Override
    public void setSizeInGridPixels(final long sizeInGridPixels) {
        this.sizeInGridPixels = sizeInGridPixels;
        OffsetDateTime end = getEndPositionInScaleUnits();
        setEndDateTime(end);
    }

    public String getLabel() {
        if (shift != null) {
            return shift.getSpot().toString() + ": " + getStartDateTime() + "-" + getEndDateTime();
        } else {
            return employeeAvailability.getState().toString() + ":" + getStartDateTime() + "-" + getEndDateTime();
        }

    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(final Shift shift) {
        this.shift = shift;
    }

    public EmployeeAvailability getEmployeeAvailability() {
        return employeeAvailability;
    }

    public void setEmployeeAvailability(final EmployeeAvailability employeeAvailability) {
        this.employeeAvailability = employeeAvailability;
    }

    @Override
    public LinearScale<OffsetDateTime> getScale() {
        return scale;
    }
}
