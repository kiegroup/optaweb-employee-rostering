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

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import elemental2.promise.Promise;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.ViewportView;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback.onSuccess;
import static org.optaplanner.openshift.employeerostering.gwtui.client.util.TimingUtils.time;
import static org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder.getCurrentSpotRosterView;

@Templated("TestGridPage.html")
public class SpotRosterDemoPage implements Page {

    @Inject
    @DataField("viewport")
    private ViewportView<LocalDateTime> viewportView;

    @Inject
    private SpotRosterViewportFactory spotRosterViewportFactory;

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
            getCurrentSpotRosterView(tenantStore.getCurrentTenantId(), onSuccess(resolve::onInvoke));
        });
    }

    private Promise<Void> initViewportView(final SpotRosterView spotRosterView) {
        viewportView.setViewport(spotRosterViewportFactory.getViewport(spotRosterView));
        return PromiseUtils.resolve();
    }
}
