package org.optaplanner.openshift.employeerostering.gwtui.client.admin;

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
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.DataInvalidation;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.KiePager;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.KieSearchBar;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.FormPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantRestServiceBuilder;

@Templated
public class TenantListPanel implements IsElement {

    @Inject
    @DataField("refresh-button")
    private HTMLButtonElement refreshButton;
    @Inject
    @DataField("add-button")
    private HTMLButtonElement addButton;

    @Inject
    @DataField("pager")
    private KiePager<Tenant> pager;

    @Inject
    @DataField("search-bar")
    private KieSearchBar<Tenant> searchBar;

    @Inject
    private TenantStore tenantStore;

    @Inject
    @DataField("table")
    @ListContainer("table")
    private ListComponent<Tenant, TenantTableRow> table;

    @Inject
    @DataField("name-header")
    @Named("th")
    private HTMLTableCellElement skillNameHeader;

    @Inject
    private ManagedInstance<TenantPopupForm> popupForm;

    @Inject
    private PromiseUtils promiseUtils;

    @Inject
    private CommonUtils commonUtils;

    public TenantListPanel() {}

    @PostConstruct
    protected void initWidget() {
        initTable();
    }

    public void onAnyTenantEvent(@Observes TenantStore.TenantChange tenant) {
        refresh();
    }

    public void onAnyInvalidationEvent(@Observes DataInvalidation<Tenant> skill) {
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
            TenantRestServiceBuilder.getTenantList(FailureShownRestCallback
                    .onSuccess(newTenantList -> {
                        searchBar.setListToFilter(newTenantList);
                        res.onInvoke(promiseUtils.resolve());
                    }));
        });
    }

    private void initTable() {
        searchBar.setListToFilter(Collections.emptyList());
        pager.setPresenter(table);
        searchBar.setElementToStringMapping((tenant) -> tenant.getName());
        searchBar.addFilterListener(pager);
    }

    @EventHandler("add-button")
    public void add(final @ForEvent("click") MouseEvent e) {
        FormPopup.getFormPopup(popupForm.get()).center();
    }

    @EventHandler("name-header")
    public void tenantNameHeaderClick(final @ForEvent("click") MouseEvent e) {
        pager.sortBy((a, b) -> commonUtils.stringWithIntCompareTo(a.getName(), b.getName()));
    }
}
