/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client.admin;

import javax.inject.Inject;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.app.spinner.LoadingSpinner;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.common.NotificationSystem;
import org.optaweb.employeerostering.gwtui.client.pages.Page;
import org.optaweb.employeerostering.gwtui.client.tenant.NewTenantForm;
import org.optaweb.employeerostering.shared.admin.AdminRestServiceBuilder;

@Templated
public class AdminPage implements Page {

    @Inject
    @DataField("reset-application-button")
    private HTMLButtonElement resetApplicationButton;
    
    @Inject
    @DataField("add-tenant-button")
    private HTMLButtonElement addTenantButton;
    
    @Inject
    private LoadingSpinner loadingSpinner;
    
    @Inject
    private NotificationSystem notificationSystem;
    
    @Inject
    private ManagedInstance<NewTenantForm> newTenantForm;

    @EventHandler("reset-application-button")
    private void resetApplication(@ForEvent("click") MouseEvent e) {
        loadingSpinner.showFor("reset-application");
        AdminRestServiceBuilder.resetApplication(null, FailureShownRestCallback.onSuccess((success) -> {
            loadingSpinner.hideFor("reset-application");
            notificationSystem.notify("Application was reset successfully", "Application was reset successfully, please refresh the page.");
        }));
    }
    

    @EventHandler("add-tenant-button")
    private void addTenant(@ForEvent("click") MouseEvent e) {
        newTenantForm.get();
    }
}
