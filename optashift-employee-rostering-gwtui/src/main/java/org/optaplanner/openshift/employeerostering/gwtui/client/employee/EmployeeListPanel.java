package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.util.Collections;
import java.util.List;

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
import org.optaplanner.openshift.employeerostering.gwtui.client.common.DataInvalidation;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.KiePager;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.KieSearchBar;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Updatable;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;

@Templated
public class EmployeeListPanel implements IsElement,
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
    private ListComponent<Employee, EmployeeSubform> table;

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
    private CommonUtils commonUtils;

    public EmployeeListPanel() {}

    @PostConstruct
    protected void initWidget() {
        initTable();
    }

    @Override
    public Promise<Void> beforeOpen() {
        return refresh();
    }

    public void onAnyTenantEvent(@Observes TenantStore.TenantChange tenant) {
        refresh();
    }

    public void onAnyInvalidationEvent(@Observes DataInvalidation<Employee> employee) {
        refresh();
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
        EmployeeSubform.createNewRow(new Employee(tenantStore.getCurrentTenantId(), ""), table, pager);
    }

    @EventHandler("name-header")
    public void spotNameHeaderClick(final @ForEvent("click") MouseEvent e) {
        pager.sortBy((a, b) -> commonUtils.stringWithIntCompareTo(a.getName(), b.getName()));
    }

    @EventHandler("skill-set-header")
    public void skillSetHeaderClick(final @ForEvent("click") MouseEvent e) {
        pager.sortBy((a, b) -> b.getSkillProficiencySet().size() - a.getSkillProficiencySet().size());
    }
}
