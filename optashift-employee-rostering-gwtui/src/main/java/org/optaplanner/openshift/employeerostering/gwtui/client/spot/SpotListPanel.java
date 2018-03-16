package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import java.util.Collections;
import java.util.HashSet;
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
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

@Templated
public class SpotListPanel implements IsElement,
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
    private ListComponent<Spot, SpotSubform> table;

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
    private CommonUtils commonUtils;

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
        SpotSubform.createNewRow(new Spot(tenantStore.getCurrentTenantId(), "", new HashSet<>()), table, pager);
    }

    @EventHandler("name-header")
    public void spotNameHeaderClick(final @ForEvent("click") MouseEvent e) {
        pager.sortBy((a, b) -> commonUtils.stringWithIntCompareTo(a.getName(), b.getName()));
    }

    @EventHandler("skill-set-header")
    public void skillSetHeaderClick(final @ForEvent("click") MouseEvent e) {
        pager.sortBy((a, b) -> b.getRequiredSkillSet().size() - a.getRequiredSkillSet().size());
    }
}
