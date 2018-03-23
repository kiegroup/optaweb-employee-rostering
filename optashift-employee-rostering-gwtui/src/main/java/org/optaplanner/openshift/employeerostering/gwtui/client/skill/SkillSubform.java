package org.optaplanner.openshift.employeerostering.gwtui.client.skill;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.TextBox;
import elemental2.dom.HTMLTableCellElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.DataInvalidation;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.TableRow;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;

@Templated("#row")
public class SkillSubform extends TableRow<Skill> implements TakesValue<Skill> {

    @Inject
    private TenantStore tenantStore;

    @Inject
    @DataField("skill-name-text-box")
    private TextBox skillName;

    @Inject
    @DataField("skill-name-display")
    @Named("td")
    private HTMLTableCellElement skillNameDisplay;

    @Inject
    private Event<DataInvalidation<Skill>> dataInvalidationEvent;

    @Inject
    private TranslationService translationService;

    @PostConstruct
    protected void initWidget() {
        skillName.getElement().setAttribute("placeholder", translationService.format(
                OptaShiftUIConstants.SkillListPanel_skillName));
        dataBinder.getModel().setTenantId(tenantStore.getCurrentTenantId());
        dataBinder.bind(skillName, "name");

        dataBinder.<String> addPropertyChangeHandler("name", (e) -> {
            skillNameDisplay.innerHTML = new SafeHtmlBuilder().appendEscaped(e.getNewValue()).toSafeHtml().asString();
        });
    }

    public void reset() {
        skillName.setValue("");
    }

    @Override
    protected void deleteRow(Skill skill) {
        SkillRestServiceBuilder.removeSkill(tenantStore.getCurrentTenantId(), skill.getId(),
                FailureShownRestCallback.onSuccess(success -> {
                    dataInvalidationEvent.fire(new DataInvalidation<>());
                }));
    }

    @Override
    protected void updateRow(Skill oldValue, Skill newValue) {
        SkillRestServiceBuilder.updateSkill(tenantStore.getCurrentTenantId(), newValue,
                FailureShownRestCallback.onSuccess(v -> {
                    dataInvalidationEvent.fire(new DataInvalidation<>());
                }));
    }

    @Override
    protected void createRow(Skill skill) {
        SkillRestServiceBuilder.addSkill(tenantStore.getCurrentTenantId(), skill,
                FailureShownRestCallback.onSuccess(v -> {
                    dataInvalidationEvent.fire(new DataInvalidation<>());
                }));
    }

    @Override
    protected void focusOnFirstInput() {
        skillName.setFocus(true);
    }
}
