package org.optaplanner.openshift.employeerostering.gwtui.client.app;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Anchor;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.employee.EmployeeListPanel;
import org.optaplanner.openshift.employeerostering.gwtui.client.roster.SpotRosterViewPanel;
import org.optaplanner.openshift.employeerostering.gwtui.client.skill.SkillListPanel;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotListPanel;

@Templated
public class MenuPanel implements IsElement {

    @Inject @DataField
    private Anchor skillsAnchor;
    @Inject
    private SkillListPanel skillListPanel;

    @Inject @DataField
    private Anchor spotsAnchor;
    @Inject
    private SpotListPanel spotListPanel;

    @Inject @DataField
    private Anchor employeesAnchor;
    @Inject
    private EmployeeListPanel employeeListPanel;

    @Inject @DataField
    private Anchor spotRosterAnchor;
    @Inject
    private SpotRosterViewPanel spotRosterViewPanel;

    @Inject @DataField
    private Div content;

    public MenuPanel() {
    }

    @PostConstruct
    protected void initWidget() {
    }

    @EventHandler("skillsAnchor")
    public void showSkills(ClickEvent e) {
        content.removeChild(content.getLastChild());
        content.appendChild(skillListPanel.getElement());
    }

    @EventHandler("spotsAnchor")
    public void showSpots(ClickEvent e) {
        content.removeChild(content.getLastChild());
        content.appendChild(spotListPanel.getElement());
    }

    @EventHandler("employeesAnchor")
    public void showEmployees(ClickEvent e) {
        content.removeChild(content.getLastChild());
        content.appendChild(employeeListPanel.getElement());
    }

    @EventHandler("spotRosterAnchor")
    public void showSpotRoster(ClickEvent e) {
        content.removeChild(content.getLastChild());
        content.appendChild(spotRosterViewPanel.getElement());
    }

}
