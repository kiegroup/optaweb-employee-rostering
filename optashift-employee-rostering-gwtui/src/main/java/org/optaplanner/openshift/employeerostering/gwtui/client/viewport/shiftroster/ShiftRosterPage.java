package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.shiftroster;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import elemental2.promise.Promise;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.header.HeaderView;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.CSSGlobalStyle;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.CSSGlobalStyle.GridVariables;

@Templated
public class ShiftRosterPage implements IsElement,
                             Page {

    @Inject
    TenantStore tenantStore;

    @Inject
    PromiseUtils promiseUtils;

    @Inject
    @DataField("viewport")
    ShiftRosterPageViewport viewport;

    @Inject
    ShiftRosterPageViewportBuilder viewportBuilder;

    @Inject
    HeaderView headerView;

    @Inject
    ShiftRosterToolbar toolbar;

    @Inject
    CSSGlobalStyle cssGlobalStyle;

    @PostConstruct
    public void init() {
        cssGlobalStyle.setGridVariable(GridVariables.GRID_UNIT_SIZE, 20);
        cssGlobalStyle.setGridVariable(GridVariables.GRID_SOFT_LINE_INTERVAL, 4);
        cssGlobalStyle.setGridVariable(GridVariables.GRID_HARD_LINE_INTERVAL, 24);
        cssGlobalStyle.setGridVariable(GridVariables.GRID_HEADER_COLUMN_WIDTH, 120);
    }

    @Override
    public Promise<Void> onOpen() {
        headerView.addStickyElement(toolbar);
        return refresh();
    }

    public void onTenantChanged(@Observes final TenantStore.TenantChange tenant) {
        refresh();
    }

    private Promise<Void> refresh() {
        return viewportBuilder.buildShiftRosterViewport(viewport);
    }

    @Override
    public void restoreFromHistory(Map<String, String> params) {
        tenantStore.setCurrentTenantId(Integer.parseInt(params.get("tenantId")));
    }
}
