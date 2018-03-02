package org.optaplanner.openshift.employeerostering.gwtui.client;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.github.nmorel.gwtjackson.rest.api.RestRequestBuilder;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.logging.impl.StackTracePrintStream;
import com.google.gwt.user.client.Window;
import elemental2.dom.DomGlobal;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.shared.api.annotations.Bundle;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.NavigationController;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.NavigationController.PageChange;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantRestServiceBuilder;

import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.SKILLS;
import static org.optaplanner.openshift.employeerostering.gwtui.client.pages.Pages.Id.SPOT_ROSTER;

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
        final GWT.UncaughtExceptionHandler javascriptLoggerExceptionHandler = GWT.getUncaughtExceptionHandler();
        GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {

            public void onUncaughtException(Throwable e) {
                javascriptLoggerExceptionHandler.onUncaughtException(e);
                Throwable unwrapped = unwrap(e);
                StringBuilder message = new StringBuilder();
                StackTracePrintStream stackTracePrintStream = new StackTracePrintStream(message);
                unwrapped.printStackTrace(stackTracePrintStream);
                ErrorPopup.show(message.toString());
            }

            public Throwable unwrap(Throwable e) {
                if (e instanceof UmbrellaException) {
                    UmbrellaException ue = (UmbrellaException) e;
                    if (ue.getCauses().size() == 1) {
                        return unwrap(ue.getCauses().iterator().next());
                    }
                }
                return e;
            }
        });
        healthCheck();
    }

    public void onTenantsReady(final @Observes TenantStore.TenantsReady tenantsReady) {
        //FIXME: We should probably have a better 'home page' than the skills table, but since it's the lightest one to load, that was the chosen one
        pageChangeEvent.fire(new PageChange(SPOT_ROSTER, () -> {
            DomGlobal.document.getElementById("initial-loading-message").remove();
            DomGlobal.document.body.appendChild(navigationController.getAppElement());
        }));
    }

    private void healthCheck() {
        TenantRestServiceBuilder.getTenantList(FailureShownRestCallback.onSuccess(tenantList -> {
            if (null == tenantList) {
                throw new IllegalStateException("The server cannot be contacted on url (" + Window.Location.getHref() + ").");
            }
            tenantStore.init(); //FIXME: Shouldn't this call be made by the Container once it's annotated with @PostConstruct?
        }));
    }
}
