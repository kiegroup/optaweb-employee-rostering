package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.util.Collections;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import elemental2.dom.HTMLButtonElement;
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
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;

@Templated
public class EmployeeListPanel implements IsElement,
                               Page {

    @Inject
    @DataField
    private HTMLButtonElement refreshButton;
    @Inject
    @DataField
    private HTMLButtonElement addButton;

    @Inject
    @DataField
    private KiePager<Employee> pager;

    @Inject
    @DataField
    private KieSearchBar searchBar;

    @Inject
    private TenantStore tenantStore;

    // TODO use DataGrid instead
    @Inject
    @DataField
    @ListContainer("table")
    private ListComponent<Employee, EmployeeSubform> table;

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

    @EventHandler("refreshButton")
    public void refresh(final @ForEvent("click") MouseEvent e) {
        refresh();
    }

    public Promise<Void> refresh() {
        if (tenantStore.getCurrentTenantId() == null) {
            return PromiseUtils.resolve();
        }
        return new Promise<>((res, rej) -> {
            EmployeeRestServiceBuilder.getEmployeeList(tenantStore.getCurrentTenantId(), FailureShownRestCallback
                                                                                                                 .onSuccess(newEmployeeList -> {
                                                                                                                     pager.setData(newEmployeeList);
                                                                                                                     res.onInvoke(PromiseUtils.resolve());
                                                                                                                 }));
        });
    }

    private void initTable() {
        pager.setData(Collections.emptyList());
        pager.setPresenter(table);
    }

    @EventHandler("addButton")
    public void add(final @ForEvent("click") MouseEvent e) {
        EmployeeSubform.createNewRow(new Employee(tenantStore.getCurrentTenantId(), ""), table, pager);
    }
}
