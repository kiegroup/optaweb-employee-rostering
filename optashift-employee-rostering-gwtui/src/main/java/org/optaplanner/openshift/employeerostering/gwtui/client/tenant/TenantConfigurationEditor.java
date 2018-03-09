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
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;
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
    private IntegerBox rotationEmployeeMatchWeightInput;

    @Inject
    @DataField
    private Button updateConfig;

    @Inject
    private TenantStore tenantStore;

    @PostConstruct
    protected void initWidget() {
        desiredWeightInput.setValidators(new DecimalMinValidator<Integer>(0));
        undesiredWeightInput.setValidators(new DecimalMinValidator<Integer>(0));
        rotationEmployeeMatchWeightInput.setValidators(new DecimalMinValidator<Integer>(0));
    }

    public void onAnyTenantEvent(@Observes TenantStore.TenantChange tenant) {
        desiredWeightInput.setValue(tenantStore.getCurrentTenant().getConfiguration().getDesiredTimeSlotWeight());
        undesiredWeightInput.setValue(tenantStore.getCurrentTenant().getConfiguration().getUndesiredTimeSlotWeight());
        refresh();
    }

    public void refresh() {}

    @EventHandler("updateConfig")
    private void onUpdateConfigClick(ClickEvent e) {
        tenantStore.getCurrentTenant().getConfiguration().setDesiredTimeSlotWeight(desiredWeightInput.getValue());
        tenantStore.getCurrentTenant().getConfiguration().setUndesiredTimeSlotWeight(undesiredWeightInput.getValue());
        tenantStore.getCurrentTenant().getConfiguration().setRotationEmployeeMatchWeight(rotationEmployeeMatchWeightInput.getValue());
        TenantRestServiceBuilder.updateTenantConfiguration(tenantStore.getCurrentTenant().getConfiguration(),
                                                           FailureShownRestCallback.onSuccess(i -> {
                                                               tenantStore.updateTenant(i);
                                                               tenantStore.setCurrentTenant(i);
                                                           }));
    }
}
