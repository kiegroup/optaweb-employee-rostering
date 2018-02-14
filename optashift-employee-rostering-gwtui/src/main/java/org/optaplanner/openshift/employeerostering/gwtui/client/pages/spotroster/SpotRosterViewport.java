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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.grid.CssGridLines;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.FiniteLinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Orientation;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.BlobView;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

import static java.util.Collections.singletonList;
import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Orientation.HORIZONTAL;

public class SpotRosterViewport extends Viewport<LocalDateTime> {

    private final Integer tenantId;
    private final CssGridLines gridLines;
    private final Supplier<ShiftBlobView> blobViewSupplier;
    private final List<Lane<LocalDateTime>> lanes;
    private final FiniteLinearScale<LocalDateTime> scale;

    public SpotRosterViewport(final Integer tenantId,
                              final Supplier<ShiftBlobView> blobViewSupplier,
                              final FiniteLinearScale<LocalDateTime> scale,
                              final List<Lane<LocalDateTime>> lanes) {

        this.scale = scale;
        this.lanes = lanes;
        this.tenantId = tenantId;
        this.blobViewSupplier = blobViewSupplier;
        this.gridLines = new CssGridLines();
    }

    @Override
    public void drawGridLinesAt(final IsElement target) {
        gridLines.draw(target, this);
    }

    @Override
    public Lane<LocalDateTime> newLane() {
        return new SpotLane(new Spot(tenantId, "New spot", new HashSet<>()),
                            new ArrayList<>(singletonList(new SubLane<>(new ArrayList<>()))));
    }

    @Override
    public Blob<LocalDateTime> newBlob(final Lane<LocalDateTime> lane, final LocalDateTime start) {

        // Casting is preferable to avoid over-use of generics in the Viewport class
        final SpotLane spotLane = (SpotLane) lane;
        final TimeSlot timeSlot = new TimeSlot(tenantId, start, start.plusHours(8L));

        final Shift shift = new Shift(tenantId, spotLane.getSpot(), timeSlot);

        //TODO: Create shift
        // ShiftRestServiceBuilder.addShift(tenantId, new ShiftView(shift), onSuccess(shift::setId));

        return new ShiftBlob(scale, shift);
    }

    @Override
    public BlobView<LocalDateTime, ?> newBlobView() {
        return blobViewSupplier.get();
    }

    @Override
    public List<Lane<LocalDateTime>> getLanes() {
        return lanes;
    }

    @Override
    public Long getGridPixelSizeInScreenPixels() {
        return 20L;
    }

    @Override
    public Orientation getOrientation() {
        return HORIZONTAL;
    }

    @Override
    public FiniteLinearScale<LocalDateTime> getScale() {
        return scale;
    }
}
