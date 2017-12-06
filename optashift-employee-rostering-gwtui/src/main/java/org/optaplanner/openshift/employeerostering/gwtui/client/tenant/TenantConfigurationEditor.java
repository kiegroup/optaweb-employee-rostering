package org.optaplanner.openshift.employeerostering.gwtui.client.tenant;

import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.ConfigurationEditor.Views;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeGroup;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantConfiguration;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantConfigurationView;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantRestServiceBuilder;

import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import org.gwtbootstrap3.client.ui.ListBox;
import org.jboss.errai.ui.client.local.api.IsElement;

@Templated
public class TenantConfigurationEditor implements IsElement {

    @Inject
    @DataField
    private ListBox weekStart;

    @Inject
    @DataField
    private ListBox templateDuration;

    @Inject
    @DataField
    private Button updateConfig;

    @Inject
    @DataField
    private Button templateEditorButton;

    private Tenant tenant;
    private ConfigurationEditor configurationEditor;

    @Inject
    @Any
    private Event<Tenant> tenantEvent;

    BiMap<Integer, Integer> templateDurationIndexMap;

    @PostConstruct
    protected void initWidget() {
        for (DayOfWeek day : DayOfWeek.values()) {
            weekStart.addItem(day.toString());
        }

        // TODO: Make this more maintainable
        templateDurationIndexMap = HashBiMap.create();
        templateDuration.addItem("1 Week");
        templateDurationIndexMap.put(1, 0);
        templateDuration.addItem("2 Weeks");
        templateDurationIndexMap.put(2, 1);
        templateDuration.addItem("4 Weeks");
        templateDurationIndexMap.put(4, 2);
    }

    public void onAnyTenantEvent(@Observes Tenant tenant) {
        this.tenant = tenant;
        weekStart.setSelectedIndex(tenant.getConfiguration().getView().getWeekStart().getValue() - 1);
        templateDuration.setSelectedIndex(templateDurationIndexMap.get(tenant.getConfiguration().getView()
                .getTemplateDuration()));
        refresh();
    }

    public void refresh() {
    }

    public void setConfigurationEditor(ConfigurationEditor configurationEditor) {
        this.configurationEditor = configurationEditor;
    }

    @EventHandler("updateConfig")
    private void onUpdateConfigClick(ClickEvent e) {
        tenant.getConfiguration().getView().setTemplateDuration(templateDurationIndexMap.inverse().get(templateDuration
                .getSelectedIndex()));
        tenant.getConfiguration().getView().setWeekStart(DayOfWeek.valueOf(weekStart.getSelectedItemText()));
        TenantRestServiceBuilder.updateTenantConfiguration(tenant.getConfiguration().getView(),
                new FailureShownRestCallback<
                        Tenant>() {

                    @Override
                    public void onSuccess(Tenant newConfig) {
                        tenant.setConfiguration(newConfig.getConfiguration());
                        tenantEvent.fire(newConfig);
                    }
                });
    }

    @EventHandler("templateEditorButton")
    private void onTemplateEditorButtonClick(ClickEvent e) {
        configurationEditor.switchView(Views.TEMPLATE_EDITOR);
    }
}
