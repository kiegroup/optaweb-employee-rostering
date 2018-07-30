package org.optaweb.employeerostering.gwtui.client.tenant;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.TakesValue;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.shared.tenant.Tenant;
import org.optaweb.employeerostering.shared.tenant.TenantRestServiceBuilder;

@Templated("#row")
public class TenantTableRow implements TakesValue<Tenant>, IsElement {

    private Tenant tenant;

    @Inject
    @DataField("tenant-name")
    @Named("td")
    private HTMLTableCellElement tenantName;

    @Inject
    @DataField("delete-tenant-button")
    private HTMLButtonElement deleteTenantButton;

    @Inject
    private EventManager eventManager;

    @Override
    public void setValue(Tenant tenant) {
        this.tenant = tenant;
        tenantName.innerHTML = new SafeHtmlBuilder().appendEscaped(tenant.getName()).toSafeHtml().asString();
    }

    @Override
    public Tenant getValue() {
        return tenant;
    }

    @EventHandler("delete-tenant-button")
    public void onDeleteTenantButtonClick(@ForEvent("click") MouseEvent e) {
        TenantRestServiceBuilder.removeTenant(tenant.getId(), FailureShownRestCallback.onSuccess(v -> {
            eventManager.fireEvent(EventManager.Event.DATA_INVALIDATION, Tenant.class);
        }));
    }

}
