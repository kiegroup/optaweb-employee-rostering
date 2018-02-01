package org.optaplanner.openshift.employeerostering.gwtui.client.tenant;

import java.time.DayOfWeek;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import org.gwtbootstrap3.client.ui.IntegerBox;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.form.validator.DecimalMinValidator;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.ConfigurationEditor.Views;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantRestServiceBuilder;

@Templated
public class TenantConfigurationEditor implements IsElement {

    @Inject
    @DataField
    private IntegerBox undesiredWeightInput;

    @Inject
    @DataField
    private IntegerBox desiredWeightInput;

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

    private ConfigurationEditor configurationEditor;

    BiMap<Integer, Integer> templateDurationIndexBiMap;

    @Inject
    private TenantStore tenantStore;

    @PostConstruct
    protected void initWidget() {
        for (DayOfWeek day : DayOfWeek.values()) {
            weekStart.addItem(day.toString());
        }

        // TODO: Make this more maintainable
        templateDurationIndexBiMap = HashBiMap.create();
        templateDuration.addItem("1 Week");
        templateDurationIndexBiMap.put(1, 0);
        templateDuration.addItem("2 Weeks");
        templateDurationIndexBiMap.put(2, 1);
        templateDuration.addItem("4 Weeks");
        templateDurationIndexBiMap.put(4, 2);

        desiredWeightInput.setValidators(new DecimalMinValidator<Integer>(0));
        undesiredWeightInput.setValidators(new DecimalMinValidator<Integer>(0));
    }

    public void onAnyTenantEvent(@Observes TenantStore.TenantChange tenant) {
        weekStart.setSelectedIndex(tenantStore.getCurrentTenant().getConfiguration().getWeekStart().getValue() - 1);
        templateDuration.setSelectedIndex(templateDurationIndexBiMap.get(tenantStore.getCurrentTenant().getConfiguration()
                                                                             .getTemplateDuration()));
        desiredWeightInput.setValue(tenantStore.getCurrentTenant().getConfiguration().getDesiredTimeSlotWeight());
        undesiredWeightInput.setValue(tenantStore.getCurrentTenant().getConfiguration().getUndesiredTimeSlotWeight());
        refresh();
    }

    public void refresh() {}

    public void setConfigurationEditor(ConfigurationEditor configurationEditor) {
        this.configurationEditor = configurationEditor;
    }

    @EventHandler("updateConfig")
    private void onUpdateConfigClick(ClickEvent e) {
        tenantStore.getCurrentTenant().getConfiguration().setTemplateDuration(templateDurationIndexBiMap.inverse().get(templateDuration
                                                                                                             .getSelectedIndex()));
        tenantStore.getCurrentTenant().getConfiguration().setWeekStart(DayOfWeek.valueOf(weekStart.getSelectedItemText()));
        tenantStore.getCurrentTenant().getConfiguration().setDesiredTimeSlotWeight(desiredWeightInput.getValue());
        tenantStore.getCurrentTenant().getConfiguration().setUndesiredTimeSlotWeight(undesiredWeightInput.getValue());
        TenantRestServiceBuilder.updateTenantConfiguration(tenantStore.getCurrentTenant().getConfiguration(),
                                                           FailureShownRestCallback.onSuccess(i -> {
                                                               tenantStore.setCurrentTenant(tenantStore.getCurrentTenant());
                                                           }));
    }

    @EventHandler("templateEditorButton")
    private void onTemplateEditorButtonClick(ClickEvent e) {
        configurationEditor.switchView(Views.TEMPLATE_EDITOR);
    }
}
