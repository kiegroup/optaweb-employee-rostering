package org.optaplanner.openshift.employeerostering.gwtui.client.skill;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayView;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.FormPopup;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;

@Templated
public class SkillEditForm implements IsElement {

    @Inject
    @DataField
    private TextBox skillName;

    @Inject
    @DataField
    private Button saveButton;

    @Inject
    @DataField
    private Button cancelButton;

    @Inject
    @DataField
    private Button closeButton;

    @Inject
    @DataField
    private @Named(value = "h3") HeadingElement title;

    @Inject
    private TranslationService CONSTANTS;

    private static Skill skill;
    private static SkillListPanel panel;

    private FormPopup popup;

    public static SkillEditForm create(SyncBeanManager beanManager, SkillListPanel skillPanel, Skill skillData) {
        panel = skillPanel;
        skill = skillData;
        return beanManager.lookupBean(SkillEditForm.class).newInstance();
    }

    @PostConstruct
    protected void initWidget() {
        skillName.setValue(skill.getName());
        title.setInnerSafeHtml(new SafeHtmlBuilder().appendEscaped(skill.getName())
                .toSafeHtml());
        popup = FormPopup.getFormPopup(this);
        popup.center();
    }

    @EventHandler("cancelButton")
    public void cancel(ClickEvent e) {
        popup.hide();
    }

    @EventHandler("closeButton")
    public void close(ClickEvent e) {
        popup.hide();
    }

    @EventHandler("saveButton")
    public void save(ClickEvent click) {
        skill.setName(skillName.getValue());
        popup.hide();
        SkillRestServiceBuilder.updateSkill(skill.getTenantId(), skill, new FailureShownRestCallback<Skill>() {

            @Override
            public void onSuccess(Skill skill) {
                panel.refresh();
            }
        });

    }

}
