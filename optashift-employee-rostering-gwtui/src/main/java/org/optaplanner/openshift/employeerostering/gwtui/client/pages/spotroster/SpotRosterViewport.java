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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.grid.CssGridLines;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.grid.Ticks;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Orientation;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view.BlobView;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.DateTimeUtils.MomentZoneId;
import org.optaplanner.openshift.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

import static java.util.Collections.singletonList;
import static org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Orientation.HORIZONTAL;

public class SpotRosterViewport extends Viewport<LocalDateTime> {

    private final Integer tenantId;
    private final Supplier<ShiftBlobView> blobViewSupplier;
    private final LinearScale<LocalDateTime> scale;
    private final CssGridLines gridLines;
    private final Ticks<LocalDateTime> dateTicks;
    private final Ticks<LocalDateTime> timeTicks;
    private final List<Lane<LocalDateTime>> lanes;
    private final MomentZoneId zoneId;

    SpotRosterViewport(final Integer tenantId,
                       final Supplier<ShiftBlobView> blobViewSupplier,
                       final LinearScale<LocalDateTime> scale,
                       final CssGridLines gridLines,
                       final Ticks<LocalDateTime> dateTicks,
                       final Ticks<LocalDateTime> timeTicks,
                       final MomentZoneId zoneId,
                       final List<Lane<LocalDateTime>> lanes) {

        this.tenantId = tenantId;
        this.blobViewSupplier = blobViewSupplier;
        this.scale = scale;
        this.gridLines = gridLines;
        this.dateTicks = dateTicks;
        this.timeTicks = timeTicks;
        this.zoneId = zoneId;
        this.lanes = lanes;

    }

    @Override
    public void drawGridLinesAt(final IsElement target) {
        gridLines.drawAt(target, this);
    }

    @Override
    public Lane<LocalDateTime> newLane() {
        return new SpotLane(new Spot(tenantId, "New spot", new HashSet<>()),
                new ArrayList<>(singletonList(new SubLane<>())));
    }

    @Override
    public Stream<Blob<LocalDateTime>> newBlob(final Lane<LocalDateTime> lane, final LocalDateTime start) {

        // Casting is preferable to avoid over-use of generics in the Viewport class
        final SpotLane spotLane = (SpotLane) lane;

        final Shift shift = new Shift(tenantId, spotLane.getSpot(), OffsetDateTime.of(start, zoneId.getRules().getOffset(start)),
                OffsetDateTime.of(start.plusHours(8L), zoneId.getRules().getOffset(start.plusHours(8L))));

        return Stream.of(new ShiftBlob(scale, shift));
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
    public LinearScale<LocalDateTime> getScale() {
        return scale;
    }

    @Override
    public void drawDateTicksAt(IsElement target) {
        DateTimeFormat dateFormat = DateTimeFormat.getFormat(PredefinedFormat.DATE_FULL);
        dateTicks.drawAt(target, this, date -> {
            return dateFormat.format(GwtJavaTimeWorkaroundUtil.toDate(date));
        });
    }

    @Override
    public void drawTimeTicksAt(IsElement target) {
        DateTimeFormat timeFormat = DateTimeFormat.getFormat(PredefinedFormat.TIME_SHORT);
        timeTicks.drawAt(target, this, date -> {
            if (date.getHour() == 0) {
                return "";
            }
            return timeFormat.format(GwtJavaTimeWorkaroundUtil.toDate(date));
        });
    }
}
