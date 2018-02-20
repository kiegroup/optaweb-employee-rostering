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

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import elemental2.promise.Promise;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.ViewportView;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.IdOrGroup;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.ShiftInfo;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.ShiftTemplate;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotGroup;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils.resolve;

@Templated
public class RotationsPage implements Page {

    @Inject
    @DataField("viewport")
    private ViewportView<Long> viewportView;

    @Inject
    @DataField("configuration")
    private RotationsConfigurationView rotationsConfigurationView;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private RotationViewportFactory rotationViewportFactory;

    private Map<Long, Spot> spotsById;
    private Map<Long, SpotGroup> spotGroupsById;

    @Override
    public Promise<Void> beforeOpen() {
        return refresh();
    }

    public void onTenantChanged(final @Observes TenantStore.TenantChange tenant) {
        refresh();
    }

    public Promise<Void> refresh() {
        return fetchShiftsBySpot().then(shiftsBySpot -> {
            viewportView.setViewport(rotationViewportFactory.getViewport(shiftsBySpot));
            return resolve();
        });
    }

    private Promise<Map<Spot, List<Shift>>> fetchShiftsBySpot() {
        return new Promise<>((resolve, reject) -> {
            SpotRestServiceBuilder.getSpotList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.<List<Spot>>onSuccess(spots -> {
                spotsById = spots.stream().collect(toMap(AbstractPersistable::getId, identity()));

                SpotRestServiceBuilder.getSpotGroups(tenantStore.getCurrentTenantId(), FailureShownRestCallback.<List<SpotGroup>>onSuccess(spotGroups -> {
                    spotGroupsById = spotGroups.stream().collect(toMap(AbstractPersistable::getId, identity()));

                    ShiftRestServiceBuilder.getTemplate(tenantStore.getCurrentTenantId(), FailureShownRestCallback.<ShiftTemplate>onSuccess(shiftTemplate -> {
                        resolve.onInvoke(getShifts(shiftTemplate).stream().collect(groupingBy(Shift::getSpot)));
                        //
                    }).onError(reject::onInvoke).onFailure(reject::onInvoke));
                }).onError(reject::onInvoke).onFailure(reject::onInvoke));
            }).onError(reject::onInvoke).onFailure(reject::onInvoke));
        });
    }

    private List<Shift> getShifts(final ShiftTemplate shiftTemplate) {
        final AtomicLong id = new AtomicLong(0L);
        return shiftTemplate.getShiftList().stream()
                .flatMap(shiftInfo -> getShiftStream(shiftInfo, id.getAndIncrement()))
                .collect(toList());
    }

    private Stream<Shift> getShiftStream(final ShiftInfo shiftInfo, final Long id) {
        return shiftInfo.getSpotList().stream()
                .flatMap(this::getSpots)
                .map(spot -> newShift(id, shiftInfo, spot));
    }

    private Stream<Spot> getSpots(final IdOrGroup spotIdOrGroup) {
        if (spotIdOrGroup.getIsGroup()) {
            return spotGroupsById.get(spotIdOrGroup.getItemId()).getSpots().stream();
        } else {
            return Stream.of(spotsById.get(spotIdOrGroup.getItemId()));
        }
    }

    private Shift newShift(final Long id,
                           final ShiftInfo shift,
                           final Spot spot) {

        final TimeSlot newTimeSlot = new TimeSlot(
                tenantStore.getCurrentTenantId(),
                shift.getStartTime(),
                shift.getEndTime());

        final Shift newShift = new Shift(
                tenantStore.getCurrentTenantId(),
                spot,
                newTimeSlot);

        newShift.setId(id);

        return newShift;
    }
}
