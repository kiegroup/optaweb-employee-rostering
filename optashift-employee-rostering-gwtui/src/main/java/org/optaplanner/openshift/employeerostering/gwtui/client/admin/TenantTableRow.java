package org.optaplanner.openshift.employeerostering.gwtui.client.admin;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import elemental2.dom.HTMLAnchorElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;

@Templated("#row")
public class TenantTableRow extends Composite implements TakesValue<Tenant> {

    @Inject
    @DataField("delete")
    private HTMLAnchorElement deleteCell;
    @Inject
    @DataField("edit")
    private HTMLAnchorElement editCell;

    @Inject
    @DataField("tenant-name-display")
    @Named("td")
    private HTMLTableCellElement tenantName;

    private Tenant tenant;

    @PostConstruct
    private void init() {}

    @EventHandler("edit")
    public void onEditClick(final @ForEvent("click") MouseEvent e) {
        // TODO: Create modal popup
    }

    @EventHandler("delete")
    public void onDeleteClick(final @ForEvent("click") MouseEvent e) {
        // TODO: Delete tenant
    }

    @Override
    public void setValue(Tenant tenant) {
        this.tenant = tenant;
        tenantName.innerHTML = new SafeHtmlBuilder().appendEscaped(tenant.getName()).toSafeHtml().asString();
    }

    @Override
    public Tenant getValue() {
        return tenant;
    }

}
