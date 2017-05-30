package org.optaplanner.openshift.employeerostering.gwtui.client.app;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.github.nmorel.gwtjackson.rest.api.RestCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.employee.EmployeeListPanel;
import org.optaplanner.openshift.employeerostering.gwtui.client.roster.RosterListPanel;
import org.optaplanner.openshift.employeerostering.gwtui.client.skill.SkillListPanel;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotListPanel;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;

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
    private Anchor rosterAnchor;
    @Inject
    private RosterListPanel rosterListPanel;

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

    @EventHandler("rosterAnchor")
    public void showRoster(ClickEvent e) {
        content.removeChild(content.getLastChild());
        content.appendChild(rosterListPanel.getElement());
    }

}
