package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.rotation;

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
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.CSSGlobalStyle;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.CSSGlobalStyle.GridVariables;

@Templated
public class RotationPage implements IsElement,
                          Page {

    @Inject
    @DataField("viewport")
    private RotationPageViewport viewport;

    @Inject
    private RotationPageViewportBuilder viewportBuilder;

    @Inject
    private HeaderView headerView;

    @Inject
    private RotationToolbar toolbar;

    @Inject
    private CSSGlobalStyle cssGlobalStyle;

    @PostConstruct
    public void init() {
        cssGlobalStyle.setGridVariable(GridVariables.GRID_UNIT_SIZE, 10);
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
        return viewportBuilder.buildRotationViewport(viewport);
    }
}
