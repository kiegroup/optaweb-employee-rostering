package org.optaplanner.openshift.employeerostering.gwtui.client.app;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.css.CssParser;
import org.optaplanner.openshift.employeerostering.gwtui.client.employee.EmployeeListPanel;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.css.CssResources;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants;
import org.optaplanner.openshift.employeerostering.gwtui.client.roster.EmployeeRosterViewPanel;
import org.optaplanner.openshift.employeerostering.gwtui.client.roster.SpotRosterViewPanel;
import org.optaplanner.openshift.employeerostering.gwtui.client.skill.SkillListPanel;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotListPanel;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.ConfigurationEditor;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TemplateEditor;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantRestServiceBuilder;

@Templated
public class MenuPanel implements IsElement {

    private Integer tenantId = null;

    @Inject
    @DataField
    private Anchor skillsAnchor;
    @Inject
    private SkillListPanel skillListPanel;
    @Inject
    @DataField
    private Anchor spotsAnchor;
    @Inject
    private SpotListPanel spotListPanel;
    @Inject
    @DataField
    private Anchor employeesAnchor;
    @Inject
    private EmployeeListPanel employeeListPanel;
    @Inject
    @DataField
    private Anchor spotRosterAnchor;
    @Inject
    private SpotRosterViewPanel spotRosterViewPanel;
    @Inject
    @DataField
    private Anchor employeeRosterAnchor;
    @Inject
    private EmployeeRosterViewPanel employeeRosterViewPanel;
    @Inject
    @DataField
    private Anchor configAnchor;
    @Inject
    private ConfigurationEditor configEditor;

    @Inject
    @DataField
    private ListBox tenantListBox;
    private List<Tenant> tenantListBoxValueList;
    @Inject
    @Any
    private Event<Tenant> tenantEvent;

    @Inject
    @DataField
    private Div content;

    public MenuPanel() {
    }

    @PostConstruct
    protected void initWidget() {
        showSpotRoster(null);
        refreshTenantListBox();
    }

    private void refreshTenantListBox() {
        TenantRestServiceBuilder.getTenantList(new FailureShownRestCallback<List<Tenant>>() {

            @Override
            public void onSuccess(List<Tenant> tenantList) {
                tenantListBoxValueList = tenantList;
                tenantListBox.clear();
                tenantList.forEach(tenant -> tenantListBox.addItem(tenant.getName()));
                if (tenantId == null) {
                    if (!tenantList.isEmpty()) {
                        Tenant tenant = tenantList.get(0);
                        tenantId = tenant.getId();
                        tenantEvent.fire(tenant);
                    }
                }
            }
        });
    }

    @EventHandler("skillsAnchor")
    public void showSkills(ClickEvent e) {
        content.removeChild(content.getLastChild());
        switchActive(skillsAnchor);
        content.appendChild(skillListPanel.getElement());
        skillListPanel.refresh();
    }

    @EventHandler("spotsAnchor")
    public void showSpots(ClickEvent e) {
        content.removeChild(content.getLastChild());
        switchActive(spotsAnchor);
        content.appendChild(spotListPanel.getElement());
        spotListPanel.refresh();
    }

    @EventHandler("employeesAnchor")
    public void showEmployees(ClickEvent e) {
        content.removeChild(content.getLastChild());
        switchActive(employeesAnchor);
        content.appendChild(employeeListPanel.getElement());
        employeeListPanel.refresh();
    }

    @EventHandler("spotRosterAnchor")
    public void showSpotRoster(ClickEvent e) {
        content.removeChild(content.getLastChild());
        switchActive(spotRosterAnchor);
        content.appendChild(spotRosterViewPanel.getElement());
        spotRosterViewPanel.refresh();
    }

    @EventHandler("employeeRosterAnchor")
    public void showEmployeeRoster(ClickEvent e) {
        content.removeChild(content.getLastChild());
        switchActive(employeeRosterAnchor);
        content.appendChild(employeeRosterViewPanel.getElement());
        employeeRosterViewPanel.refresh();
    }

    @EventHandler("configAnchor")
    public void showConfigurationEditor(ClickEvent e) {
        content.removeChild(content.getLastChild());
        switchActive(configAnchor);
        content.appendChild(configEditor.getElement());
        configEditor.refresh();
    }

    private void switchActive(Anchor anchor) {
        skillsAnchor.getElement().getParentElement().removeClassName("active");
        spotsAnchor.getElement().getParentElement().removeClassName("active");
        employeesAnchor.getElement().getParentElement().removeClassName("active");
        spotRosterAnchor.getElement().getParentElement().removeClassName("active");
        employeeRosterAnchor.getElement().getParentElement().removeClassName("active");
        configAnchor.getElement().getParentElement().removeClassName("active");
        anchor.getElement().getParentElement().addClassName("active");
    }

    @EventHandler("tenantListBox")
    public void selectTenant(ClickEvent e) {
        int tenantIndex = tenantListBox.getSelectedIndex();
        Tenant tenant = tenantIndex < 0 ? null : tenantListBoxValueList.get(tenantIndex);
        tenantId = tenant.getId();
        tenantEvent.fire(tenant);
    }

}
