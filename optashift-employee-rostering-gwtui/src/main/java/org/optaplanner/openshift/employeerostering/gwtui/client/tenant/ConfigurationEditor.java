package org.optaplanner.openshift.employeerostering.gwtui.client.tenant;

import java.util.Collection;
import java.util.Collections;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import elemental2.dom.HTMLCanvasElement;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Calendar;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.ShiftData;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.ConstantFetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotNameFetchable;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;

import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.*;

@Templated
public class ConfigurationEditor implements IsElement {

    private Integer tenantId = null;
    
    @Inject @DataField
    private HTMLCanvasElement canvasElement;
    @Inject @DataField
    private Div topPanel;
    @Inject @DataField
    private Div bottomPanel;
    @Inject @DataField
    private Span sidePanel;
    
    @Inject
    private TranslationService CONSTANTS;
    
    private Calendar<ShiftData> calendar;
    

    public ConfigurationEditor() {     
    }

    @PostConstruct
    protected void initWidget() {
        calendar = new Calendar<ShiftData>(canvasElement, tenantId, topPanel, bottomPanel, sidePanel,
                new ConstantFetchable<Collection<ShiftData>>(Collections.emptyList()), new SpotNameFetchable(() -> getTenantId()),
                (name,start,end) -> new ShiftData(start,end,name));
    }

    public void onAnyTenantEvent(@Observes Tenant tenant) {
        tenantId = tenant.getId();
        calendar.setTenantId(tenantId);
        refresh();
    }
    
    private Integer getTenantId() {
        return tenantId;
    }
   
   public void refresh() {
       calendar.refresh();
   }

}
