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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.rotation;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.BlobWithTwin;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;

public class ShiftBlob implements BlobWithTwin<OffsetDateTime, ShiftBlob> {

    private final Shift shift;
    private final LinearScale<OffsetDateTime> scale;
    private final OffsetDateTime baseDate;
    private Long sizeInGridPixels;
    private ShiftBlob twin;

    private Long positionInGridPixelsCache;
    private Long endPositionInGridPixelsCache;

    ShiftBlob(final Shift shift,
              final OffsetDateTime baseDate,
              final LinearScale<OffsetDateTime> scale) {

        this.shift = shift;
        this.scale = scale;
        this.baseDate = baseDate;
        this.sizeInGridPixels = getInitialSizeInGridPixels();
        this.twin = getUpdatedTwin().orElse(null);
    }

    private ShiftBlob(final Shift shift,
                      final OffsetDateTime baseDate,
                      final LinearScale<OffsetDateTime> scale,
                      final ShiftBlob twin) {

        this.shift = shift;
        this.scale = scale;
        this.baseDate = baseDate;
        this.sizeInGridPixels = getInitialSizeInGridPixels();
        this.twin = twin;
    }

    private long getInitialSizeInGridPixels() {
        return scale.toGridPixels(shift.getEndDateTime()) - getPositionInGridPixels();
    }

    @Override
    public OffsetDateTime getPositionInScaleUnits() {
        return shift.getStartDateTime();
    }

    @Override
    public long getEndPositionInGridPixels() {

        //Collision performance optimization
        if (endPositionInGridPixelsCache == null) {
            endPositionInGridPixelsCache = BlobWithTwin.super.getEndPositionInGridPixels();
        }

        return endPositionInGridPixelsCache;
    }

    @Override
    public long getPositionInGridPixels() {

        //Collision performance optimization
        if (positionInGridPixelsCache == null) {
            positionInGridPixelsCache = BlobWithTwin.super.getPositionInGridPixels();
        }

        return positionInGridPixelsCache;
    }

    @Override
    public void setPositionInScaleUnits(final OffsetDateTime positionInScaleUnits) {
        shift.setStartDateTime(positionInScaleUnits);
        // invalidate the cache
        positionInGridPixelsCache = null;
    }

    @Override
    public long getSizeInGridPixels() {
        return sizeInGridPixels;
    }

    @Override
    public void setSizeInGridPixels(final long sizeInGridPixels) {
        this.sizeInGridPixels = sizeInGridPixels;
        shift.setEndDateTime(scale.toScaleUnits(getPositionInGridPixels() + sizeInGridPixels));
        // invalidate the cache
        endPositionInGridPixelsCache = null;
    }

    @Override
    public Optional<ShiftBlob> getUpdatedTwin() {

        //FIXME: Maybe it's better to use the Viewport sizeInGridPixels instead of the end of the scale

        final boolean hasAnyPartOffTheGrid = getPositionInScaleUnits().isBefore(baseDate) ||
                getEndPositionInScaleUnits().isAfter(scale.getEndInScaleUnits());

        if (hasAnyPartOffTheGrid) {
            final ShiftBlob twin = getTwin().orElseGet(this::newTwin);
            long duration = getEndPositionInGridPixels() - getPositionInGridPixels();
            if (getPositionInScaleUnits().isBefore(baseDate)) {
                long durationBeforeStart = -getPositionInGridPixels();
                twin.setPositionInScaleUnits(scale.toScaleUnits(scale.getEndInGridPixels() - durationBeforeStart));
                twin.setSizeInGridPixels(duration);
            } else {
                long durationAfterEnd = getEndPositionInGridPixels() - scale.getEndInGridPixels();
                twin.setPositionInScaleUnits(scale.toScaleUnits(durationAfterEnd - duration));
                twin.setSizeInGridPixels(duration);
            }
            return Optional.of(twin);
        } else {
            return Optional.empty();
        }
    }

    public ShiftBlob newTwin() {

        final Shift shiftTwin = new Shift(
                shift.getTenantId(),
                shift.getSpot(),
                shift.getStartDateTime(),
                shift.getEndDateTime(),
                shift.getRotationEmployee());

        final ShiftBlob twin = new ShiftBlob(
                shiftTwin,
                baseDate,
                scale,
                this);

        twin.setPositionInScaleUnits(getPositionInScaleUnits());
        twin.setSizeInGridPixels(getSizeInGridPixels());

        return twin;
    }

    @Override
    public Optional<ShiftBlob> getTwin() {
        return Optional.ofNullable(twin);
    }

    @Override
    public void setTwin(final ShiftBlob twin) {
        this.twin = twin;
    }

    @Override
    public LinearScale<OffsetDateTime> getScale() {
        return scale;
    }

    public Shift getShift() {
        return shift;
    }
}
