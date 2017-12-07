package org.optaplanner.openshift.employeerostering.gwtui.client.tenant;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.dom.client.Node;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;

@Templated
public class ConfigurationEditor implements IsElement {

    private Integer tenantId = null;

    @Inject
    TenantConfigurationEditor tenantConfigurationEditor;

    @Inject
    TemplateEditor templateEditor;

    IsElement current;

    @Inject
    @DataField
    private Div content;

    public ConfigurationEditor() {
    }

    @PostConstruct
    protected void initWidget() {
        templateEditor.setConfigurationEditor(this);
        tenantConfigurationEditor.setConfigurationEditor(this);
        content.removeChild(content.getLastChild());
        content.appendChild(templateEditor.getElement());
    }

    public void onAnyTenantEvent(@Observes Tenant tenant) {
        tenantId = tenant.getId();
        refresh();
    }

    public void refresh() {
        templateEditor.refresh();
    }

    public void switchView(Views view) {
        switch (view) {
            case TEMPLATE_EDITOR:
                content.removeChild(content.getLastChild());
                content.appendChild(templateEditor.getElement());
                break;
            case TENANT_CONFIGURATION_EDITOR:
                content.removeChild(content.getLastChild());
                content.appendChild(tenantConfigurationEditor.getElement());
                break;
            default:
                break;

        }
    }

    protected static enum Views {
        TEMPLATE_EDITOR,
        TENANT_CONFIGURATION_EDITOR;
    }

}
