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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.MouseEvent;
import elemental2.promise.Promise;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.spinner.LoadingSpinner;
import org.optaplanner.openshift.employeerostering.gwtui.client.header.HeaderView;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.view.ViewportView;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.rotation.view.RotationView;
import org.optaplanner.openshift.employeerostering.shared.rotation.view.ShiftTemplateView;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback.onSuccess;

@Templated
public class RotationPage implements Page {

    @Inject
    @DataField("viewport")
    private ViewportView<LocalDateTime> viewportView;

    @Inject
    @DataField("save-button")
    private HTMLButtonElement saveButton;

    @Inject
    @DataField("refresh-button")
    private HTMLButtonElement refreshButton;

    @Inject
    @DataField("toolbar")
    private HTMLDivElement toolbar;

    @Inject
    private HeaderView headerView;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private RotationViewportFactory rotationViewportFactory;

    @Inject
    private LoadingSpinner loadingSpinner;

    @Inject
    private PromiseUtils promiseUtils;

    private Viewport<LocalDateTime> viewport;

    @Override
    public Promise<Void> beforeOpen() {
        return refresh();
    }

    @Override
    public Promise<Void> onOpen() {
        headerView.addStickyElement(() -> toolbar);
        return promiseUtils.resolve();
    }

    public void onTenantChanged(final @Observes TenantStore.TenantChange tenant) {
        refresh();
    }

    public Promise<Void> refresh() {

        loadingSpinner.showFor("rotation-page");

        return fetchRotationView().then(rotationView -> {
            viewport = rotationViewportFactory.getViewport(rotationView);
            viewportView.setViewport(viewport);
            loadingSpinner.hideFor("rotation-page");
            return promiseUtils.resolve();

        }).catch_(i -> {
            promiseUtils.getDefaultCatch().onInvoke(i);
            loadingSpinner.hideFor("rotation-page");
            return promiseUtils.resolve();
        });
    }

    private Promise<RotationView> fetchRotationView() {
        return promiseUtils.promise((resolve, reject) -> {
            ShiftRestServiceBuilder.getRotation(tenantStore.getCurrentTenantId(), onSuccess(resolve::onInvoke));
        });
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

        final Map<Long, List<ShiftTemplateView>> newSpotIdToShiftTemplateViewListMap = viewport.getLanes().stream()
                .collect(Collectors.toMap(lane -> ((SpotLane) lane).getSpot().getId(),
                        lane -> {
                            List<ShiftTemplateView> shiftTemplates = new ArrayList<>();
                            lane.getSubLanes().forEach(sublane -> {
                                sublane.getBlobs().forEach(blob -> {
                                    ShiftTemplateBlob shiftTemplateBlob = (ShiftTemplateBlob) blob;
                                    if (!shiftTemplateBlob.getTwin().isPresent() ||
                                            shiftTemplateBlob.getTwin().get().getPositionInGridPixels() < shiftTemplateBlob.getPositionInGridPixels()) {
                                        shiftTemplates.add(shiftTemplateBlob.getShiftTemplateView());
                                    }
                                });
                            });
                            return shiftTemplates;
                        }));

        RotationView out = new RotationView();
        out.setTenantId(tenantStore.getCurrentTenantId());
        out.setSpotIdToShiftTemplateViewListMap(newSpotIdToShiftTemplateViewListMap);

        ShiftRestServiceBuilder.updateRotation(
                tenantStore.getCurrentTenantId(),
                out,
                onSuccess(i -> refresh()));
    }
}
