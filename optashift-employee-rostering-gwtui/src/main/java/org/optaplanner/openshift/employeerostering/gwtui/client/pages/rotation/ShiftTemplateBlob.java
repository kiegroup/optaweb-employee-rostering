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
import java.util.Map;
import java.util.Optional;

import org.optaplanner.openshift.employeerostering.gwtui.client.pages.TimeslotBlob;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.BlobWithTwin;
import org.optaplanner.openshift.employeerostering.shared.common.HasTimeslot;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.rotation.view.ShiftTemplateView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class ShiftTemplateBlob extends TimeslotBlob implements BlobWithTwin<LocalDateTime, ShiftTemplateBlob> {

    private final ShiftTemplateView shift;
    private ShiftTemplateBlob twin;
    private Map<Long, Spot> spotIdToSpotMap;
    private Map<Long, Employee> employeeIdToEmployeeMap;

    ShiftTemplateBlob(final ShiftTemplateView shift,
                      Map<Long, Spot> spotIdToSpotMap,
                      Map<Long, Employee> employeeIdToEmployeeMap,
                      final LinearScale<LocalDateTime> scale) {

        this.shift = shift;
        setScale(scale);
        this.spotIdToSpotMap = spotIdToSpotMap;
        this.employeeIdToEmployeeMap = employeeIdToEmployeeMap;
        this.twin = getUpdatedTwin().orElse(null);
    }

    private ShiftTemplateBlob(final ShiftTemplateView shift,
                              Map<Long, Spot> spotIdToSpotMap,
                              Map<Long, Employee> employeeIdToEmployeeMap,
                              final LinearScale<LocalDateTime> scale,
                              final ShiftTemplateBlob twin) {
        this.shift = shift;
        setScale(scale);
        this.spotIdToSpotMap = spotIdToSpotMap;
        this.employeeIdToEmployeeMap = employeeIdToEmployeeMap;
        this.twin = twin;
    }

    @Override
    public void setPositionInScaleUnits(final LocalDateTime positionInScaleUnits) {
        shift.setDurationBetweenRotationStartAndTemplateStart(
                Duration.between(HasTimeslot.EPOCH, positionInScaleUnits));
    }

    @Override
    public void setSizeInGridPixels(final long sizeInGridPixels) {
        shift.setShiftTemplateDuration(
                Duration.between(getPositionInScaleUnits(), getScale()
                        .toScaleUnits(getPositionInGridPixels() + sizeInGridPixels)));
    }

    @Override
    public Optional<ShiftTemplateBlob> getUpdatedTwin() {

        //FIXME: Maybe it's better to use the Viewport sizeInGridPixels instead of the end of the scale

        final boolean hasAnyPartOffTheGrid = getPositionInScaleUnits().isBefore(HasTimeslot.EPOCH) ||
                getEndPositionInScaleUnits().isAfter(getScale().getEndInScaleUnits());

        if (hasAnyPartOffTheGrid) {
            final ShiftTemplateBlob twin = getTwin().orElseGet(this::newTwin);
            long duration = getEndPositionInGridPixels() - getPositionInGridPixels();
            if (getPositionInScaleUnits().isBefore(HasTimeslot.EPOCH)) {
                long durationBeforeStart = -getPositionInGridPixels();
                twin.setPositionInScaleUnits(getScale().toScaleUnits(getScale().getEndInGridPixels() - durationBeforeStart));
                twin.setSizeInGridPixels(duration);
            } else {
                long durationAfterEnd = getEndPositionInGridPixels() - getScale().getEndInGridPixels();
                twin.setPositionInScaleUnits(getScale().toScaleUnits(durationAfterEnd - duration));
                twin.setSizeInGridPixels(duration);
            }
            return Optional.of(twin);
        } else {
            return Optional.empty();
        }
    }

    public ShiftTemplateBlob newTwin() {

        final ShiftTemplateView shiftTwin = new ShiftTemplateView(
                shift.getTenantId(),
                shift.getSpotId(),
                shift.getDurationBetweenRotationStartAndTemplateStart(),
                shift.getShiftTemplateDuration(),
                shift.getRotationEmployeeId());

        final ShiftTemplateBlob twin = new ShiftTemplateBlob(
                shiftTwin,
                spotIdToSpotMap,
                employeeIdToEmployeeMap,
                getScale(),
                this);

        twin.setPositionInScaleUnits(getPositionInScaleUnits());
        twin.setSizeInGridPixels(getSizeInGridPixels());

        return twin;
    }

    @Override
    public Optional<ShiftTemplateBlob> getTwin() {
        return Optional.ofNullable(twin);
    }

    @Override
    public void setTwin(final ShiftTemplateBlob twin) {
        this.twin = twin;
    }

    public ShiftTemplateView getShiftTemplateView() {
        return shift;
    }

    @Override
    public HasTimeslot getTimeslot() {
        return shift;
    }

    public Spot getSpot() {
        return spotIdToSpotMap.get(shift.getSpotId());
    }

    public Employee getRotationEmployee() {
        return employeeIdToEmployeeMap.get(shift.getRotationEmployeeId());
    }
}
