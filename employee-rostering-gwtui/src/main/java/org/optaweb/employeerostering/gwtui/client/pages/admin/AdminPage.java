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

package org.optaweb.employeerostering.gwtui.client.pages.admin;

import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.MouseEvent;
import elemental2.promise.Promise;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.app.spinner.LoadingSpinner;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.EventManager.Event;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.common.KiePager;
import org.optaweb.employeerostering.gwtui.client.common.KieSearchBar;
import org.optaweb.employeerostering.gwtui.client.notification.NotificationFactory;
import org.optaweb.employeerostering.gwtui.client.pages.Page;
import org.optaweb.employeerostering.gwtui.client.resources.i18n.I18nKeys;
import org.optaweb.employeerostering.gwtui.client.tenant.NewTenantForm;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantTableRow;
import org.optaweb.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaweb.employeerostering.shared.admin.AdminRestServiceBuilder;
import org.optaweb.employeerostering.shared.tenant.Tenant;
import org.optaweb.employeerostering.shared.tenant.TenantRestServiceBuilder;

@Templated
public class AdminPage
        implements
        Page {

    @Inject
    @DataField("reset-application-button")
    private HTMLButtonElement resetApplicationButton;

    @Inject
    @DataField("add-tenant-button")
    private HTMLButtonElement addTenantButton;

    @Inject
    @DataField("refresh-button")
    private HTMLButtonElement refreshButton;

    @Inject
    private LoadingSpinner loadingSpinner;

    @Inject
    private NotificationFactory notificationFactory;

    @Inject
    private ManagedInstance<NewTenantForm> newTenantFormFactory;

    @Inject
    @DataField("pager")
    private KiePager<Tenant> pager;

    @Inject
    @DataField("search-bar")
    private KieSearchBar<Tenant> searchBar;

    @Inject
    @DataField("table")
    @ListContainer("table")
    private ListComponent<Tenant, TenantTableRow> table;

    @Inject
    private EventManager eventManager;

    @Inject
    private PromiseUtils promiseUtils;

    @PostConstruct
    protected void initWidget() {
        initTable();
        eventManager.subscribeToEventForever(Event.DATA_INVALIDATION, this::onAnyInvalidationEvent);
    }

    @Override
    public Promise<Void> beforeOpen() {
        return refresh();
    }

    public void onAnyInvalidationEvent(Class<?> dataInvalidated) {
        if (dataInvalidated.equals(Tenant.class)) {
            refresh();
        }
    }

    @EventHandler("refresh-button")
    public void refresh(final @ForEvent("click") MouseEvent e) {
        refresh();
    }

    private void initTable() {
        searchBar.setListToFilter(Collections.emptyList());
        pager.setPresenter(table);
        searchBar.setElementToStringMapping((tenant) -> tenant.getName());
        searchBar.addFilterListener(pager);
    }

    private Promise<Void> refresh() {
        return promiseUtils.promise((res, rej) -> {
            TenantRestServiceBuilder.getTenantList(FailureShownRestCallback
                                                           .onSuccess(newTenantList -> {
                                                               searchBar.setListToFilter(newTenantList);
                                                               res.onInvoke(promiseUtils.resolve());
                                                           }));
        });
    }

    @EventHandler("reset-application-button")
    private void resetApplication(@ForEvent("click") MouseEvent e) {
        loadingSpinner.showFor("reset-application");
        AdminRestServiceBuilder.resetApplication(null, FailureShownRestCallback.onSuccess((success) -> {
            loadingSpinner.hideFor("reset-application");
            notificationFactory.showInfoMessage(I18nKeys.Notifications_resetApplicationSuccessful);
        }));
    }

    @EventHandler("add-tenant-button")
    private void addTenant(@ForEvent("click") MouseEvent e) {
        newTenantFormFactory.get();
    }
}
