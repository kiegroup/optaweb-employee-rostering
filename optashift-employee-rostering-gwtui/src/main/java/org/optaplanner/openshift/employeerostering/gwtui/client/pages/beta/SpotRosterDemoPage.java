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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.beta;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import elemental2.promise.Promise;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.demo.Positive2HoursScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.demo.ShiftBlob;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.demo.ShiftBlobView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.demo.SpotLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.grid.DefaultGridLines;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.BlobView;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.ViewportView;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Orientation.HORIZONTAL;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback.onSuccess;
import static org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder.getCurrentSpotRosterView;

@Templated("TestGridPage.html")
public class SpotRosterDemoPage implements Page {

    @Inject
    @DataField("viewport")
    private ViewportView<LocalDateTime> viewportView;

    @Inject
    private DefaultGridLines gridLines;

    @Inject
    private ManagedInstance<ShiftBlobView> blobViews;

    @Inject
    private TenantStore tenantStore;

    @Override
    public Promise<Void> beforeOpen() {
        return refresh();
    }

    public void onTenantChanged(final @Observes TenantStore.TenantChange tenant) {
        refresh();
    }

    public Promise<Void> refresh() {
        return fetchSpotRosterView().then(this::initViewportView);
    }

    private Promise<SpotRosterView> fetchSpotRosterView() {
        return new Promise<>((resolve, reject) -> {
            getCurrentSpotRosterView(currentTenantId(), onSuccess(resolve::onInvoke));
        });
    }

    private Promise<Void> initViewportView(final SpotRosterView spotRosterView) {

        final Positive2HoursScale scale =
                new Positive2HoursScale(spotRosterView.getStartDate().atTime(0, 0),
                                        s -> spotRosterView.getEndDate().atTime(0, 0));

        final List<Lane<LocalDateTime>> lanes =
                buildRosterModel(spotRosterView)
                        .entrySet()
                        .stream()
                        .map(e -> new SpotLane(scale, e.getKey(), e.getValue()))
                        .collect(toList());

        viewportView.setViewport(new Viewport<LocalDateTime>(12L, HORIZONTAL, lanes, scale) {

            @Override
            public void drawGridLinesAt(final IsElement target) {
                gridLines.draw(target, this);
            }

            @Override
            public Blob<LocalDateTime> newBlob(final Lane<LocalDateTime> lane,
                                               final LocalDateTime start) {

                // Casting is preferable to avoid over-use of generics in the Viewport class
                final SpotLane spotLane = (SpotLane) lane;

                final Shift newShift = new Shift(
                        currentTenantId(),
                        spotLane.getSpot(),
                        new TimeSlot(currentTenantId(), start, start.plusHours(8L)));

                return new ShiftBlob(newShift, scale);
            }

            @Override
            public BlobView<LocalDateTime, ?> newBlobView() {
                return blobViews.get();
            }
        });

        return PromiseUtils.resolve();
    }

    private Map<Spot, Map<ShiftView, List<TimeSlot>>> buildRosterModel(final SpotRosterView spotRosterView) {

        final Map<Long, Spot> spotsById = spotRosterView.getSpotList().stream().collect(toMap(AbstractPersistable::getId, identity()));
        final Map<Long, TimeSlot> timeSlotsById = spotRosterView.getTimeSlotList().stream().collect(toMap(AbstractPersistable::getId, identity()));

        return spotRosterView.getTimeSlotIdToSpotIdToShiftViewListMap().values().stream()
                .flatMap(s -> s.values().stream())
                .flatMap(Collection::stream)
                .collect(groupingBy((final ShiftView shiftView) -> spotsById.get(shiftView.getSpotId()),
                                    groupingBy((final ShiftView shiftView) -> shiftView,
                                               mapping((final ShiftView shiftView) -> timeSlotsById.get(shiftView.getTimeSlotId()),
                                                       toList()))));
    }

    private Integer currentTenantId() {
        return tenantStore.getCurrentTenantId();
    }
}
