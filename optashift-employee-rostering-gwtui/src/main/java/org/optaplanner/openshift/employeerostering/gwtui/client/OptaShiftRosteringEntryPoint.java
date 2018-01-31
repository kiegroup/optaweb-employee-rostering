package org.optaplanner.openshift.employeerostering.gwtui.client;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.github.nmorel.gwtjackson.rest.api.RestRequestBuilder;
import elemental2.dom.DomGlobal;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.shared.api.annotations.Bundle;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.NavigationController;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.NavigationController.PageChange;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;

import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.SKILLS;

@EntryPoint
@Bundle("resources/i18n/OptaShiftUIConstants.properties")
public class OptaShiftRosteringEntryPoint {

    static {
        // Keep in sync with web.xml
        RestRequestBuilder.setDefaultApplicationPath("/rest");
    }

    @Inject
    private NavigationController navigationController;

    @Inject
    private Event<PageChange> pageChangeEvent;

    @Inject
    private TenantStore tenantStore;

    @PostConstruct
    public void onModuleLoad() {
        tenantStore.init(); //FIXME: Shouldn't this call be made by the Container since it's annotated with @PostConstruct?
    }

    public void onTenantsReady(final @Observes TenantStore.TenantsReady tenantsReady) {
        pageChangeEvent.fire(new PageChange(SKILLS, () -> {
            DomGlobal.document.getElementById("initial-loading-message").remove();
            DomGlobal.document.body.appendChild(navigationController.getAppElement());
        }));
    }
}