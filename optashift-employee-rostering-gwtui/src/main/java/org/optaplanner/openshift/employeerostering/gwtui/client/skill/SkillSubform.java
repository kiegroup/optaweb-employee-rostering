package org.optaplanner.openshift.employeerostering.gwtui.client.skill;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.TextBox;
import elemental2.dom.HTMLTableCellElement;
import elemental2.promise.Promise;
import org.gwtbootstrap3.extras.select.client.ui.MultipleSelect;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.jboss.errai.databinding.client.api.Converter;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.DataInvalidation;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.StringListToSkillSetConverter;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.TableRow;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Updatable;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;

@Dependent
@Templated("#row")
public class SkillSubform extends TableRow<Skill> implements TakesValue<Skill> {

    @Inject
    Validator validator;

    @Inject
    private TenantStore tenantStore;

    @Inject
    @DataField
    private TextBox skillNameTextBox;

    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement skillName;

    @Inject
    private Event<DataInvalidation<Skill>> dataInvalidationEvent;

    @Inject
    private TranslationService translationService;

    @PostConstruct
    protected void initWidget() {
        skillNameTextBox.getElement().setAttribute("placeholder", translationService.format(
                OptaShiftUIConstants.SkillListPanel_skillName));
        dataBinder.getModel().setTenantId(tenantStore.getCurrentTenantId());
        dataBinder.bind(skillNameTextBox, "name");

        dataBinder.<String> addPropertyChangeHandler("name", (e) -> {
            skillName.innerHTML = new SafeHtmlBuilder().appendEscaped(e.getNewValue()).toSafeHtml().asString();
        });
    }

    public Set<ConstraintViolation<Skill>> validate() {
        return validator.validate(getNewValue());
    }

    public void reset() {
        skillNameTextBox.setValue("");
    }

    public Promise<Skill> getIfValid() {
        return new Promise<Skill>((res, rej) -> {
            Set<ConstraintViolation<Skill>> validationErrorSet = validate();
            if (validationErrorSet.isEmpty()) {
                res.onInvoke(getNewValue());
            } else {
                rej.onInvoke(validationErrorSet);
            }
        });

    }

    @Override
    protected void deleteRow() {
        SkillRestServiceBuilder.removeSkill(tenantStore.getCurrentTenantId(), getValue().getId(),
                FailureShownRestCallback.onSuccess(success -> {
                    dataInvalidationEvent.fire(new DataInvalidation<Skill>());
                }));
    }

    @Override
    protected void updateRow() {
        getIfValid()
                .then((e) -> {
                    commitChanges();
                    setEditing(false);
                    SkillRestServiceBuilder.updateSkill(tenantStore.getCurrentTenantId(), getNewValue(),
                            FailureShownRestCallback.onSuccess(skill -> {
                                dataInvalidationEvent.fire(new DataInvalidation<Skill>());
                            }));
                    return PromiseUtils.resolve();
                })
                .catch_((s) -> {
                    @SuppressWarnings("unchecked")
                    Set<ConstraintViolation<Skill>> errors = (Set<ConstraintViolation<Skill>>) s;
                    ErrorPopup.show(CommonUtils.delimitCollection(errors, (e) -> e.getMessage(), "\n"));
                    return PromiseUtils.resolve();
                });
    }

    @Override
    protected void createRow() {
        getIfValid()
                .then((e) -> {
                    commitChanges();
                    setEditing(false);
                    SkillRestServiceBuilder.addSkill(tenantStore.getCurrentTenantId(), getNewValue(),
                            FailureShownRestCallback.onSuccess(skill -> {
                                dataInvalidationEvent.fire(new DataInvalidation<Skill>());
                            }));
                    return PromiseUtils.resolve();
                })
                .catch_((s) -> {
                    @SuppressWarnings("unchecked")
                    Set<ConstraintViolation<Skill>> errors = (Set<ConstraintViolation<Skill>>) s;
                    ErrorPopup.show(CommonUtils.delimitCollection(errors, (e) -> e.getMessage(), "\n"));
                    return PromiseUtils.resolve();
                });
    }
}