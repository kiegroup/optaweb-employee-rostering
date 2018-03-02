package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import java.util.Collections;
import java.util.HashSet;

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
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

@Templated
public class SpotListPanel implements IsElement,
                           Page {

    @Inject
    @DataField
    private HTMLButtonElement refreshButton;
    @Inject
    @DataField
    private HTMLButtonElement addButton;

    @Inject
    @DataField
    private KiePager<Spot> pager;

    @Inject
    @DataField
    private KieSearchBar searchBar;

    @Inject
    private TenantStore tenantStore;

    // TODO use DataGrid instead
    @Inject
    @DataField
    @ListContainer("table")
    private ListComponent<Spot, SpotSubform> table;

    public SpotListPanel() {}

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

    public void onAnyInvalidationEvent(@Observes DataInvalidation<Spot> spot) {
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
            SpotRestServiceBuilder.getSpotList(tenantStore.getCurrentTenantId(), FailureShownRestCallback
                                                                                                         .onSuccess(newSpotList -> {
                                                                                                             pager.setData(newSpotList);
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
        SpotSubform.createNewRow(new Spot(tenantStore.getCurrentTenantId(), "", new HashSet<>()), table, pager);
    }
}
