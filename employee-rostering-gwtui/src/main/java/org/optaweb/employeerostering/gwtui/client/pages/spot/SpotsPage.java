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

package org.optaweb.employeerostering.gwtui.client.pages.spot;

import java.util.Collections;
import java.util.HashSet;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.MouseEvent;
import elemental2.promise.Promise;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.EventManager.Event;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.common.KiePager;
import org.optaweb.employeerostering.gwtui.client.common.KieSearchBar;
import org.optaweb.employeerostering.gwtui.client.pages.Page;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.spot.SpotRestServiceBuilder;

@Templated
public class SpotsPage implements IsElement,
                                      Page {

    @Inject
    @DataField("refresh-button")
    private HTMLButtonElement refreshButton;
    @Inject
    @DataField("add-button")
    private HTMLButtonElement addButton;

    @Inject
    @DataField("pager")
    private KiePager<Spot> pager;

    @Inject
    @DataField("search-bar")
    private KieSearchBar<Spot> searchBar;

    @Inject
    private TenantStore tenantStore;

    // TODO use DataGrid instead
    @Inject
    @DataField("table")
    @ListContainer("table")
    private ListComponent<Spot, SpotTableRow> table;

    @Inject
    @DataField("name-header")
    @Named("th")
    private HTMLTableCellElement spotNameHeader;

    @Inject
    @DataField("skill-set-header")
    @Named("th")
    private HTMLTableCellElement skillSetHeader;

    @Inject
    private PromiseUtils promiseUtils;

    @Inject
    private EventManager eventManager;

    @PostConstruct
    protected void initWidget() {
        initTable();
        eventManager.subscribeToEventForever(Event.DATA_INVALIDATION, this::onAnyInvalidationEvent);
    }

    @Override
    public Promise<Void> beforeOpen() {
        return refresh();
    }

    public void onAnyTenantEvent(@Observes TenantStore.TenantChange tenant) {
        refresh();
    }

    public void onAnyInvalidationEvent(Class<?> dataInvalidated) {
        if (dataInvalidated.equals(Spot.class)) {
            refresh();
        }
    }

    @EventHandler("refresh-button")
    public void refresh(final @ForEvent("click") MouseEvent e) {
        refresh();
    }

    public Promise<Void> refresh() {
        if (tenantStore.getCurrentTenantId() == null) {
            return promiseUtils.resolve();
        }
        return promiseUtils.promise((res, rej) -> {
            SpotRestServiceBuilder.getSpotList(tenantStore.getCurrentTenantId(), FailureShownRestCallback
                    .onSuccess(newSpotList -> {
                        searchBar.setListToFilter(newSpotList);
                        res.onInvoke(promiseUtils.resolve());
                    }));
        });
    }

    private void initTable() {
        searchBar.setListToFilter(Collections.emptyList());
        pager.setPresenter(table);
        searchBar.setElementToStringMapping((spot) -> spot.getName());
        searchBar.addFilterListener(pager);
    }

    @EventHandler("add-button")
    public void add(final @ForEvent("click") MouseEvent e) {
        SpotTableRow.createNewRow(new Spot(tenantStore.getCurrentTenantId(), "", new HashSet<>()), table, pager);
    }

    @EventHandler("name-header")
    public void spotNameHeaderClick(final @ForEvent("click") MouseEvent e) {
        pager.sortBy((a, b) -> a.getName().toLowerCase().compareTo(b.getName().toLowerCase()));
    }

    @EventHandler("skill-set-header")
    public void skillSetHeaderClick(final @ForEvent("click") MouseEvent e) {
        pager.sortBy((a, b) -> b.getRequiredSkillSet().size() - a.getRequiredSkillSet().size());
    }
}
