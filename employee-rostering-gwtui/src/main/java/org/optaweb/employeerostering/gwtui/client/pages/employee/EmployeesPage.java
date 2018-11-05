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

package org.optaweb.employeerostering.gwtui.client.pages.employee;

import java.util.Collections;

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
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.common.KiePager;
import org.optaweb.employeerostering.gwtui.client.common.KieSearchBar;
import org.optaweb.employeerostering.gwtui.client.pages.Page;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeRestServiceBuilder;

@Templated
public class EmployeesPage implements IsElement,
                                      Page {

    @Inject
    @DataField("refresh-button")
    private HTMLButtonElement refreshButton;
    @Inject
    @DataField("add-button")
    private HTMLButtonElement addButton;

    @Inject
    @DataField("pager")
    private KiePager<Employee> pager;

    @Inject
    @DataField("search-bar")
    private KieSearchBar<Employee> searchBar;

    @Inject
    private TenantStore tenantStore;

    // TODO use DataGrid instead
    @Inject
    @DataField("table")
    @ListContainer("table")
    private ListComponent<Employee, EmployeeTableRow> table;

    @Inject
    @DataField("name-header")
    @Named("th")
    private HTMLTableCellElement employeeNameHeader;

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
        eventManager.subscribeToEventForever(EventManager.Event.DATA_INVALIDATION, this::onAnyInvalidationEvent);
    }

    @Override
    public Promise<Void> beforeOpen() {
        return refresh();
    }

    public void onAnyTenantEvent(@Observes TenantStore.TenantChange tenant) {
        refresh();
    }

    public void onAnyInvalidationEvent(Class<?> dataInvalidated) {
        if (dataInvalidated.equals(Employee.class)) {
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
            EmployeeRestServiceBuilder.getEmployeeList(tenantStore.getCurrentTenantId(), FailureShownRestCallback
                    .onSuccess(newEmployeeList -> {
                        searchBar.setListToFilter(newEmployeeList);
                        res.onInvoke(promiseUtils.resolve());
                    }));
        });
    }

    private void initTable() {
        searchBar.setListToFilter(Collections.emptyList());
        pager.setPresenter(table);
        searchBar.setElementToStringMapping((employee) -> employee.getName());
        searchBar.addFilterListener(pager);
    }

    @EventHandler("add-button")
    public void add(final @ForEvent("click") MouseEvent e) {
        EmployeeTableRow.createNewRow(new Employee(tenantStore.getCurrentTenantId(), "", null, Collections.emptySet()), table, pager);
    }

    @EventHandler("name-header")
    public void spotNameHeaderClick(final @ForEvent("click") MouseEvent e) {
        pager.sortBy((a, b) -> a.getName().toLowerCase().compareTo(b.getName().toLowerCase()));
    }

    @EventHandler("skill-set-header")
    public void skillSetHeaderClick(final @ForEvent("click") MouseEvent e) {
        pager.sortBy((a, b) -> b.getSkillProficiencySet().size() - a.getSkillProficiencySet().size());
    }

    @EventHandler("contract-header")
    public void contractHeaderClick(final @ForEvent("click") MouseEvent e) {
        pager.sortBy((a, b) -> a.getContract().getName().compareTo(b.getContract().getName()));
    }
}
