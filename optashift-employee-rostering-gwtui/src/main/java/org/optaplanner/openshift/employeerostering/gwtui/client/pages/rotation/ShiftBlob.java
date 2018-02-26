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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.powers.BlobWithTwin;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

import static java.time.ZoneOffset.UTC;

public class ShiftBlob implements BlobWithTwin<Long, ShiftBlob> {

    private final Shift shift;
    private final LinearScale<Long> scale;
    private final LocalDateTime baseDate;
    private Long sizeInGridPixels;
    private ShiftBlob twin;

    ShiftBlob(final Shift shift,
              final LocalDateTime baseDate,
              final LinearScale<Long> scale) {

        this.shift = shift;
        this.scale = scale;
        this.baseDate = baseDate;
        this.sizeInGridPixels = getInitialSizeInGridPixels();
        this.twin = getUpdatedTwin();
    }

    private ShiftBlob(final Shift shift,
                      final LocalDateTime baseDate,
                      final LinearScale<Long> scale,
                      final ShiftBlob twin) {

        this.shift = shift;
        this.scale = scale;
        this.baseDate = baseDate;
        this.sizeInGridPixels = getInitialSizeInGridPixels();
        this.twin = twin;
    }

    private long getInitialSizeInGridPixels() {
        return scale.toGridPixels(minutesAfterBaseDate(shift.getTimeSlot().getEndDateTime())) - getPositionInGridPixels();
    }

    @Override
    public Long getPositionInScaleUnits() {
        return minutesAfterBaseDate(shift.getTimeSlot().getStartDateTime());
    }

    @Override
    public void setPositionInScaleUnits(final Long positionInScaleUnits) {
        shift.getTimeSlot().setStartDateTime(baseDate.plusMinutes(positionInScaleUnits));
    }

    @Override
    public Long getSizeInGridPixels() {
        return sizeInGridPixels;
    }

    @Override
    public void setSizeInGridPixels(final Long sizeInGridPixels) {
        this.sizeInGridPixels = sizeInGridPixels;
        shift.getTimeSlot().setEndDateTime(shift.getTimeSlot().getStartDateTime().plusMinutes(scale.toScaleUnits(sizeInGridPixels)));
    }

    private long minutesAfterBaseDate(final LocalDateTime startDateTime) {
        return Duration.between(baseDate.toInstant(UTC), startDateTime.toInstant(UTC)).getSeconds() / 60;
    }

    @Override
    public ShiftBlob getUpdatedTwin() {

        //FIXME: Maybe it's better to use the Viewport sizeInGridPixels instead of the end of the scale

        final boolean hasAnyPartOffTheGrid =
                getEndPositionInGridPixels() > scale.getEndInGridPixels() ||
                        getPositionInGridPixels() < 0;

        if (hasAnyPartOffTheGrid) {
            final ShiftBlob twin = getTwin().orElseGet(this::newTwin);
            final Long offset = (getPositionInGridPixels() < 0 ? 1 : -1) * scale.getEndInGridPixels();
            twin.setPositionInScaleUnits(scale.toScaleUnits(getPositionInGridPixels() + offset));
            twin.setSizeInGridPixels(sizeInGridPixels);
            return twin;
        } else {
            return null;
        }
    }

    public ShiftBlob newTwin() {

        final TimeSlot timeSlot = new TimeSlot(
                shift.getTimeSlot().getTenantId(),
                shift.getTimeSlot().getStartDateTime(), //intentionally overwritten below
                shift.getTimeSlot().getEndDateTime()); //intentionally overwritten below

        final Shift shiftTwin = new Shift(
                shift.getTenantId(),
                shift.getSpot(),
                timeSlot);

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
    public LinearScale<Long> getScale() {
        return scale;
    }

    public Shift getShift() {
        return shift;
    }
}
