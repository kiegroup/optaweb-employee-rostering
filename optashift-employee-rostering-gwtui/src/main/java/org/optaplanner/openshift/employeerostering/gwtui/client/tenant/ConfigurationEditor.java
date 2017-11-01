package org.optaplanner.openshift.employeerostering.gwtui.client.tenant;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import elemental2.dom.MouseEvent;
import elemental2.dom.HTMLCanvasElement;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.jboss.errai.common.client.api.annotations.BrowserEvent;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Calendar;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;
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
    
    private Calendar calendar;
    

    public ConfigurationEditor() {     
    }

    @PostConstruct
    protected void initWidget() {
        calendar = new Calendar(canvasElement, tenantId, topPanel, bottomPanel, sidePanel);
    }

    public void onAnyTenantEvent(@Observes Tenant tenant) {
        tenantId = tenant.getId();
        calendar.setTenantId(tenantId);
        refresh();
    }
   
   public void refresh() {
       
   }

}
