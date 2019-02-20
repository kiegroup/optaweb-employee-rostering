/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.client;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.github.nmorel.gwtjackson.rest.api.RestRequestBuilder;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import elemental2.dom.DomGlobal;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.shared.api.annotations.Bundle;
import org.optaweb.employeerostering.gwtui.client.app.NavigationController;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.notification.NotificationFactory;
import org.optaweb.employeerostering.gwtui.client.pages.Pages;
import org.optaweb.employeerostering.gwtui.client.resources.i18n.I18nKeys;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.shared.tenant.TenantRestServiceBuilder;

@EntryPoint
@Bundle("resources/i18n/OptaWebUIConstants.properties")
public class OptaWebEntryPoint {

    static {
        // Keep in sync with web.xml
        RestRequestBuilder.setDefaultApplicationPath("/rest");
    }

    @Inject
    private NavigationController navigationController;

    @Inject
    private Event<NavigationController.PageChange> pageChangeEvent;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private NotificationFactory notificationFactory;

    private boolean isPageLoaded;

    @PostConstruct
    public void onModuleLoad() {
        isPageLoaded = false;
        final GWT.UncaughtExceptionHandler javascriptLoggerExceptionHandler = GWT.getUncaughtExceptionHandler();
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {

            public void onUncaughtException(Throwable e) {
                javascriptLoggerExceptionHandler.onUncaughtException(e);
                notificationFactory.showError(e);
            }
        });
        healthCheck();
    }

    public void onTenantsReady(final @Observes TenantStore.TenantsReady tenantsReady) {
        //FIXME: We should probably have a better 'home page' than the skills table, but since it's the lightest one to load, that was the chosen one
        if (!isPageLoaded) {
            isPageLoaded = true;
            pageChangeEvent.fire(new NavigationController.PageChange(Pages.Id.SHIFT_ROSTER, () -> {
                DomGlobal.document.getElementById("initial-loading-message").remove();
                DomGlobal.document.body.appendChild(navigationController.getAppElement());
            }));
        }
    }

    public void onNoTenants(final @Observes TenantStore.NoTenants noTenants) {
        if (!isPageLoaded) {
            isPageLoaded = true;
            pageChangeEvent.fire(new NavigationController.PageChange(Pages.Id.ADMIN, () -> {
                DomGlobal.document.getElementById("initial-loading-message").remove();
                DomGlobal.document.body.appendChild(navigationController.getAppElement());
            }));
        }
    }

    private void healthCheck() {
        TenantRestServiceBuilder.getTenantList(FailureShownRestCallback.onSuccess(tenantList -> {
            if (null == tenantList) {
                notificationFactory.showErrorMessage(I18nKeys.OptaWebEntryPoint_cannotContactServer, Window.Location.getHref());
            } else {
                tenantStore.init(); //FIXME: Shouldn't this call be made by the Container once it's annotated with @PostConstruct?
            }
        }));
    }
}
