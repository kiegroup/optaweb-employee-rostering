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

package org.optaweb.employeerostering.gwtui.client.pages.contract;

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
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaweb.employeerostering.shared.contract.Contract;
import org.optaweb.employeerostering.shared.contract.ContractRestServiceBuilder;

@Templated
public class ContractsPage
        implements
        Page {

    @Inject
    @DataField("add-contract-button")
    private HTMLButtonElement addContractButton;

    @Inject
    @DataField("refresh-button")
    private HTMLButtonElement refreshButton;

    @Inject
    private LoadingSpinner loadingSpinner;

    @Inject
    private NotificationFactory notificationFactory;

    @Inject
    private ManagedInstance<ContractForm> contractFormFactory;

    @Inject
    @DataField("pager")
    private KiePager<Contract> pager;

    @Inject
    @DataField("search-bar")
    private KieSearchBar<Contract> searchBar;

    @Inject
    @DataField("table")
    @ListContainer("table")
    private ListComponent<Contract, ContractTableRow> table;

    @Inject
    private EventManager eventManager;

    @Inject
    private TenantStore tenantStore;

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
        if (dataInvalidated.equals(Contract.class)) {
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
            ContractRestServiceBuilder.getContractList(tenantStore.getCurrentTenantId(), FailureShownRestCallback
                    .onSuccess(newContractList -> {
                        searchBar.setListToFilter(newContractList);
                        res.onInvoke(promiseUtils.resolve());
                    }));
        });
    }

    @EventHandler("add-contract-button")
    private void addContract(@ForEvent("click") MouseEvent e) {
        contractFormFactory.get().createNewContract();
    }
}
