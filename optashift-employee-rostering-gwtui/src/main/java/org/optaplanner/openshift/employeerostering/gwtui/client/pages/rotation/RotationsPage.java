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

import javax.inject.Inject;

import elemental2.promise.Promise;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.view.ViewportView;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;

import static org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback.onSuccess;
import static org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils.resolve;

@Templated
public class RotationsPage implements Page {

    @Inject
    @DataField("viewport")
    private ViewportView<Long> viewportView;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private RotationViewportFactory rotationViewportFactory;

    @Override
    public Promise<Void> beforeOpen() {
        return new Promise<>((resolve, reject) -> {
            ShiftRestServiceBuilder.getTemplate(tenantStore.getCurrentTenantId(), onSuccess(template -> {
                viewportView.setViewport(rotationViewportFactory.getViewport(template));
                resolve.onInvoke(resolve());
            }));
        });
    }
}
