package org.optaplanner.openshift.employeerostering.gwtui.client.tenant;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import elemental2.dom.HTMLDivElement;
import elemental2.promise.Promise;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;

@Templated
public class ConfigurationEditor implements IsElement,
                                            Page {

    @Inject
    TenantConfigurationEditor tenantConfigurationEditor;

    @Inject
    TemplateEditor templateEditor;

    IsElement current;

    @Inject
    @DataField
    private HTMLDivElement content;

    @Inject
    private TenantStore tenantStore;

    public ConfigurationEditor() {
    }

    @PostConstruct
    public void initWidget() {
        templateEditor.setConfigurationEditor(this);
        tenantConfigurationEditor.setConfigurationEditor(this);
        content.removeChild(content.lastChild);
        content.appendChild(templateEditor.getElement());
    }

    @Override
    public Promise<Void> onOpen() {
        //FIXME: For some reason it's not enough to simply call `refresh()`, but it works fine when firing a TenantChange event.
        tenantStore.setCurrentTenant(tenantStore.getCurrentTenant());
        return PromiseUtils.resolve(); //FIXME: Make it resolve only after the page is assembled
    }

    public void onAnyTenantEvent(@Observes TenantStore.TenantChange tenant) {
        refresh();
    }

    public void refresh() {
        templateEditor.refresh();
    }

    public void switchView(Views view) {
        switch (view) {
            case TEMPLATE_EDITOR:
                content.removeChild(content.lastChild);
                content.appendChild(templateEditor.getElement());
                break;
            case TENANT_CONFIGURATION_EDITOR:
                content.removeChild(content.lastChild);
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
