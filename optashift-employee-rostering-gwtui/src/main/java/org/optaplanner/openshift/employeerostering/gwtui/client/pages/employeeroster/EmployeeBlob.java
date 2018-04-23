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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.DateTimeUtils.MomentZoneId;
import org.optaplanner.openshift.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;

public class EmployeeBlob implements Blob<LocalDateTime> {

    private final LinearScale<LocalDateTime> scale;
    private EmployeeAvailability employeeAvailability;
    private Shift shift;
    private Long sizeInGridPixels;

    private Long positionInGridPixelsCache = null;
    private Long endPositionInGridPixelsCache = null;
    private MomentZoneId zoneId;

    public EmployeeBlob(final LinearScale<LocalDateTime> scale, final Shift shift) {
        this(scale, null, shift);
    }

    public EmployeeBlob(final LinearScale<LocalDateTime> scale, final EmployeeAvailability employeeAvailability) {
        this(scale, employeeAvailability, null);
    }

    private EmployeeBlob(final LinearScale<LocalDateTime> scale, final EmployeeAvailability employeeAvailability, final Shift shift) {
        this.employeeAvailability = employeeAvailability;
        this.shift = shift;
        this.scale = scale;
        this.sizeInGridPixels = scale.toGridPixels(getEndDateTime()) - scale.toGridPixels(getStartDateTime());
    }

    private LocalDateTime getStartDateTime() {
        if (null != shift) {
            return GwtJavaTimeWorkaroundUtil.toLocalDateTime(shift.getStartDateTime());
        } else {
            return GwtJavaTimeWorkaroundUtil.toLocalDateTime(employeeAvailability.getStartDateTime());
        }
    }

    private LocalDateTime getEndDateTime() {
        if (null != shift) {
            return GwtJavaTimeWorkaroundUtil.toLocalDateTime(shift.getEndDateTime());
        } else {
            return GwtJavaTimeWorkaroundUtil.toLocalDateTime(employeeAvailability.getEndDateTime());
        }
    }

    private void setStartDateTime(LocalDateTime startDateTime) {
        if (null != shift) {
            shift.setStartDateTime(OffsetDateTime.of(startDateTime, zoneId.getRules().getOffset(startDateTime)));
        } else {
            employeeAvailability.setStartDateTime(OffsetDateTime.of(startDateTime, zoneId.getRules().getOffset(startDateTime)));
        }
    }

    private void setEndDateTime(LocalDateTime endDateTime) {
        if (null != shift) {
            shift.setEndDateTime(OffsetDateTime.of(endDateTime, zoneId.getRules().getOffset(endDateTime)));
        } else {
            employeeAvailability.setEndDateTime(OffsetDateTime.of(endDateTime, zoneId.getRules().getOffset(endDateTime)));
        }
    }

    @Override
    public long getSizeInGridPixels() {
        return sizeInGridPixels;
    }

    @Override
    public LocalDateTime getPositionInScaleUnits() {
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
    public void setPositionInScaleUnits(final LocalDateTime start) {
        positionInGridPixelsCache = null;
        endPositionInGridPixelsCache = null;
        setStartDateTime(start);
    }

    @Override
    public void setSizeInGridPixels(final long sizeInGridPixels) {
        this.sizeInGridPixels = sizeInGridPixels;
        LocalDateTime end = getEndPositionInScaleUnits();
        setEndDateTime(end);
    }

    public String getLabel() {
        if (shift != null) {
            return shift.getSpot().toString() + ": " + getStartDateTime() + "-" + getEndDateTime();
        } else {
            return employeeAvailability.getState().toString();
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
    public LinearScale<LocalDateTime> getScale() {
        return scale;
    }

    public EmployeeBlob withZoneId(MomentZoneId zoneId) {
        this.zoneId = zoneId;
        return this;
    }
}
