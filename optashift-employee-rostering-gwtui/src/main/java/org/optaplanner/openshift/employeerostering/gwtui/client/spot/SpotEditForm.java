package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

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
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

@Templated
public class SpotEditForm implements IsElement {

    @Inject
    @DataField
    private TextBox spotName;

    @Inject
    @DataField
    ListBox requiredSkill;

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

    private static Spot spot;
    private static SpotListPanel panel;
    private static List<Skill> skills;

    private FormPopup popup;

    public static SpotEditForm create(SyncBeanManager beanManager, SpotListPanel spotPanel, Spot spotData, List<
            Skill> skillData) {
        panel = spotPanel;
        spot = spotData;
        skills = skillData;
        return beanManager.lookupBean(SpotEditForm.class).newInstance();
    }

    @PostConstruct
    protected void initWidget() {
        spotName.setValue(spot.getName());
        skills.forEach((s) -> requiredSkill.addItem(s.getName()));
        requiredSkill.setItemSelected(skills.indexOf(spot.getRequiredSkill()), true);
        title.setInnerSafeHtml(new SafeHtmlBuilder().appendEscaped(spot.getName())
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
        spot.setName(spotName.getValue());
        int requiredSkillIndex = requiredSkill.getSelectedIndex();
        spot.setRequiredSkill(requiredSkillIndex < 0 ? null : skills.get(requiredSkillIndex));
        popup.hide();
        SpotRestServiceBuilder.updateSpot(spot.getTenantId(), spot, new FailureShownRestCallback<Spot>() {

            @Override
            public void onSuccess(Spot spot) {
                panel.refresh();
            }
        });

    }

}
