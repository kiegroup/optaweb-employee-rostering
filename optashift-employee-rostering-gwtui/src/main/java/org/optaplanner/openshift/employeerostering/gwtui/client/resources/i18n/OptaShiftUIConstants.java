package org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n;

import org.jboss.errai.ui.shared.api.annotations.TranslationKey;

public interface OptaShiftUIConstants {

    @TranslationKey(
            defaultValue = "The server returned an empty result which is impossible.\nMaybe the server is using the wrong WildFly version. Try using WildFly 10.1.0.Final.")
    final String AbstractRosterViewPanel_emptyResult = "AbstractRosterViewPanel.emptyResult";

    @TranslationKey(defaultValue = "Finished solving")
    final String AbstractRosterViewPanel_finishedSolving = "AbstractRosterViewPanel.finishedSolving";

    @TranslationKey(defaultValue = "Solve")
    final String AbstractRosterViewPanel_solve = "AbstractRosterViewPanel.solve";

    @TranslationKey(defaultValue = "Solving for another")
    final String AbstractRosterViewPanel_solvingFor = "AbstractRosterViewPanel.solvingFor";

    @TranslationKey(defaultValue = "AbstractRosterViewPanel.startSolving")
    final String AbstractRosterViewPanel_startSolving = "Click the <i>Solve</i> button to start solving.";

    @TranslationKey(defaultValue = "AbstractRosterViewPanel.terminateEarly")
    final String AbstractRosterViewPanel_terminateEarly = "Terminate";

    @TranslationKey(defaultValue = "Week {0}")
    final String DateDisplay_WEEKS_FROM_EPOCH = "DateDisplay.WEEKS_FROM_EPOCH";

    @TranslationKey(defaultValue = "Week starting {0}/{1}/{2}")
    final String DateDisplay_WEEK_STARTING = "DateDisplay.WEEK_STARTING";

    @TranslationKey(defaultValue = "Week ending {0}/{1}/{2}")
    final String DateDisplay_WEEK_ENDING = "DateDisplay.WEEK_ENDING";

    @TranslationKey(defaultValue = "Employee name")
    final String EmployeeListPanel_employeeName = "EmployeeListPanel.employeeName";

    @TranslationKey(defaultValue = "Actions")
    final String General_actions = "General.actions";

    @TranslationKey(defaultValue = "Add")
    final String General_add = "General.add";

    @TranslationKey(defaultValue = "Cancel")
    final String General_cancel = "General.cancel";

    @TranslationKey(defaultValue = "Confirm")
    final String General_confirm = "General.confirm";

    @TranslationKey(defaultValue = "Delete")
    final String General_delete = "General.delete";

    @TranslationKey(defaultValue = "Edit")
    final String General_edit = "General.edit";

    @TranslationKey(defaultValue = "Update")
    final String General_update = "General.update";

    @TranslationKey(defaultValue = "Employee")
    final String General_employee = "General.employee";

    @TranslationKey(defaultValue = "Name")
    final String General_name = "General.name";

    @TranslationKey(defaultValue = "Refresh")
    final String General_refresh = "General.refresh";

    @TranslationKey(defaultValue = "Skills")
    final String General_skills = "General.skills";

    @TranslationKey(defaultValue = "Employee Roster")
    final String MenuPanel_employeeRoster = "MenuPanel.employeeRoster";

    @TranslationKey(defaultValue = "Employees")
    final String MenuPanel_employees = "MenuPanel.employees";

    @TranslationKey(defaultValue = "Rest API")
    final String MenuPanel_restAPI = "MenuPanel.restAPI";

    @TranslationKey(defaultValue = "Spot Roster")
    final String MenuPanel_spotRoster = "MenuPanel.spotRoster";

    @TranslationKey(defaultValue = "Spots")
    final String MenuPanel_spots = "MenuPanel.spots";

    @TranslationKey(defaultValue = "Tenant")
    final String MenuPanel_tenant = "MenuPanel.tenant";

    @TranslationKey(defaultValue = "Toggle navigation")
    final String MenuPanel_toggleNavigation = "MenuPanel.toggleNavigation";

    @TranslationKey(defaultValue = "Skill name")
    final String SkillListPanel_skillName = "SkillListPanel.skillName";

    @TranslationKey(defaultValue = "Required skill")
    final String SpotListPanel_requiredSkill = "SpotListPanel.requiredSkill";

    @TranslationKey(defaultValue = "Spot name")
    final String SpotListPanel_spotName = "SpotListPanel.spotName";

    @TranslationKey(defaultValue = "Spot")
    final String SpotRosterView_spot = "SpotRosterView.spot";
}
