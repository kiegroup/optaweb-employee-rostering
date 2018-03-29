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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.MouseEvent;
import elemental2.promise.Promise;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.spinner.LoadingSpinner;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view.ViewportView;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.DateTimeUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterState;
import org.optaplanner.openshift.employeerostering.shared.rotation.ShiftTemplate;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback.onSuccess;

@Templated
public class RotationPage implements Page {

    @Inject
    @DataField("viewport")
    private ViewportView<OffsetDateTime> viewportView;

    @Inject
    @DataField("configuration")
    private RotationConfigurationView rotationsConfigurationView;

    @Inject
    @DataField("save-button")
    private HTMLButtonElement saveButton;

    @Inject
    @DataField("refresh-button")
    private HTMLButtonElement refreshButton;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private RotationViewportFactory rotationViewportFactory;

    @Inject
    private LoadingSpinner loadingSpinner;

    @Inject
    private PromiseUtils promiseUtils;

    private Viewport<OffsetDateTime> viewport;

    @Override
    public Promise<Void> beforeOpen() {
        return refresh();
    }

    public void onTenantChanged(final @Observes TenantStore.TenantChange tenant) {
        refresh();
    }

    public Promise<Void> refresh() {

        loadingSpinner.showFor("rotation-page");

        return fetchShiftTemplate().then(shiftTemplate -> {
            return promiseUtils.manage(fetchSpotList().then((spotList) -> {
                return promiseUtils.manage(fetchRosterState().then(rosterState -> {
                    final Map<Spot, List<Shift>> shiftsBySpot = buildShiftList(shiftTemplate, rosterState).stream()
                            .collect(groupingBy(Shift::getSpot));
                    viewport = rotationViewportFactory.getViewport(rosterState, shiftsBySpot, spotList);
                    viewportView.setViewport(viewport);
                    loadingSpinner.hideFor("rotation-page");
                    return promiseUtils.resolve();
                }));
            }));

        }).catch_(i -> {
            promiseUtils.getDefaultCatch().onInvoke(i);
            loadingSpinner.hideFor("rotation-page");
            return promiseUtils.resolve();
        });
    }

    private Promise<Collection<ShiftTemplate>> fetchShiftTemplate() {
        return promiseUtils.promise((resolve, reject) -> {
            ShiftRestServiceBuilder.getShiftTemplateList(tenantStore.getCurrentTenantId(), onSuccess(resolve::onInvoke));
        });
    }

    private Promise<RosterState> fetchRosterState() {
        return new Promise<>((resolve, reject) -> {
            RosterRestServiceBuilder.getRosterState(tenantStore.getCurrentTenantId(), onSuccess(resolve::onInvoke));
        });
    }

    private List<Shift> buildShiftList(final Collection<ShiftTemplate> shiftTemplate, final RosterState rosterState) {
        final AtomicLong id = new AtomicLong(0L);
        return shiftTemplate.stream()
                .map(shiftInfo -> newShift(id.getAndIncrement(), shiftInfo, rosterState)).collect(toList());
    }

    private Shift newShift(final Long id,
                           final ShiftTemplate shift,
                           final RosterState rosterState) {
        final Shift newShift = new Shift(
                tenantStore.getCurrentTenantId(),
                shift.getSpot(),
                getStartDateTime(shift),
                getEndDateTime(shift, rosterState.getRotationLength()),
                shift.getRotationEmployee());

        newShift.setId(id);

        return newShift;
    }

    public static LocalDate getBaseDate() {
        return LocalDate.of(0, 1, 1);
    }

    public static OffsetDateTime getBaseDateTime() {
        return OffsetDateTime.of(getBaseDate().atTime(LocalTime.MIDNIGHT), ZoneOffset.UTC);
    }

    private OffsetDateTime getStartDateTime(ShiftTemplate shift) {
        return OffsetDateTime.of(getBaseDate()
                .plusDays(shift.getStartDayOffset())
                .atTime(shift.getStartTime()), ZoneOffset.UTC);
    }

    private OffsetDateTime getEndDateTime(ShiftTemplate shift, int rotationLength) {
        if (shift.getEndDayOffset() < shift.getStartDayOffset()) {
            return OffsetDateTime.of(getBaseDate().plusDays(rotationLength)
                    .plusDays(shift.getEndDayOffset())
                    .atTime(shift.getEndTime()), ZoneOffset.UTC);
        } else {
            return OffsetDateTime.of(getBaseDate()
                    .plusDays(shift.getEndDayOffset())
                    .atTime(shift.getEndTime()), ZoneOffset.UTC);
        }
    }

    private int getOffsetStartDay(Shift shift) {
        return DateTimeUtils.daysBetween(getBaseDate(), shift.getStartDateTime());
    }

    private int getOffsetEndDay(Shift shift) {
        return DateTimeUtils.daysBetween(getBaseDate(), shift.getEndDateTime());
    }

    @EventHandler("save-button")
    private void onSaveClicked(final @ForEvent("click") MouseEvent e) {
        save();
        e.preventDefault();
    }

    @EventHandler("refresh-button")
    private void onRefreshClicked(final @ForEvent("click") MouseEvent e) {
        refresh();
        e.preventDefault();
    }

    private void save() {

        final List<ShiftTemplate> newShiftInfoList = viewport.getLanes().stream()
                .flatMap(lane -> lane.getSubLanes().stream())
                .flatMap(subLane -> subLane.getBlobs().stream())
                .filter(blob -> blob.getPositionInGridPixels() >= 0) //Removes left-most twins
                .map(blob -> ((ShiftBlob) blob).getShift())
                .map(this::newShiftTemplate)
                .collect(toList());

        ShiftRestServiceBuilder.updateShiftTemplate(
                tenantStore.getCurrentTenantId(),
                newShiftInfoList,
                onSuccess(i -> refresh()));
    }

    private ShiftTemplate newShiftTemplate(final Shift shift) {
        return new ShiftTemplate(tenantStore.getCurrentTenantId(),
                shift.getSpot(), getOffsetStartDay(shift), DateTimeUtils.getLocalTimeOf(shift.getStartDateTime()),
                getOffsetEndDay(shift), DateTimeUtils.getLocalTimeOf(shift.getEndDateTime()), shift.getRotationEmployee());
    }

    private Promise<List<Spot>> fetchSpotList() {
        return promiseUtils.promise((res, rej) -> {
            SpotRestServiceBuilder.getSpotList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(spotList -> res.onInvoke(spotList)));
        });
    }
}
