package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

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
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

@Dependent
@Templated("#row")
public class SpotSubform extends TableRow<Spot> implements TakesValue<Spot>, Updatable<Map<String, Skill>> {

    @Inject
    Validator validator;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private StringListToSkillSetConverter skillConvertor;

    @Inject
    @DataField
    private TextBox spotNameTextBox;

    @Inject
    @DataField
    private MultipleSelect requiredSkillSet;

    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement spotName;

    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement requiredSkillSetDisplay;

    @Inject
    private Event<DataInvalidation<Spot>> dataInvalidationEvent;

    @Inject
    private TranslationService translationService;

    @PostConstruct
    protected void initWidget() {
        spotNameTextBox.getElement().setAttribute("placeholder", translationService.format(
                OptaShiftUIConstants.SpotListPanel_spotName));
        dataBinder.getModel().setTenantId(tenantStore.getCurrentTenantId());
        skillConvertor.registerSkillMapListener(this);
        dataBinder.bind(spotNameTextBox, "name");
        dataBinder.bind(requiredSkillSet, "requiredSkillSet", skillConvertor);

        dataBinder.<String> addPropertyChangeHandler("name", (e) -> {
            spotName.innerHTML = new SafeHtmlBuilder().appendEscaped(e.getNewValue()).toSafeHtml().asString();
        });
        dataBinder.<Set<Skill>> addPropertyChangeHandler("requiredSkillSet", (e) -> {
            requiredSkillSetDisplay.innerHTML = new SafeHtmlBuilder().appendEscaped(CommonUtils.delimitCollection(e
                    .getNewValue(),
                    (s) -> s.getName(), ",")).toSafeHtml().asString();
        });
    }

    public Set<ConstraintViolation<Spot>> validate() {
        return validator.validate(getNewValue());
    }

    public void reset() {
        spotNameTextBox.setValue("");
    }

    public Promise<Spot> getIfValid() {
        return new Promise<Spot>((res, rej) -> {
            Set<ConstraintViolation<Spot>> validationErrorSet = validate();
            if (validationErrorSet.isEmpty()) {
                res.onInvoke(getNewValue());
            } else {
                rej.onInvoke(validationErrorSet);
            }
        });

    }

    @Override
    public void onUpdate(Map<String, Skill> data) {
        requiredSkillSet.clear();
        data.forEach((name, skill) -> {
            Option option = new Option();
            option.setName(name);
            option.setValue(name);
            option.setText(name);
            requiredSkillSet.add(option);
        });
        requiredSkillSet.refresh();
    }

    @Override
    protected void deleteRow() {
        SpotRestServiceBuilder.removeSpot(tenantStore.getCurrentTenantId(), getValue().getId(),
                FailureShownRestCallback.onSuccess(success -> {
                    dataInvalidationEvent.fire(new DataInvalidation<Spot>());
                }));
    }

    @Override
    protected void updateRow() {
        getIfValid()
                .then((e) -> {
                    commitChanges();
                    setEditing(false);
                    SpotRestServiceBuilder.updateSpot(tenantStore.getCurrentTenantId(), getNewValue(),
                            FailureShownRestCallback.onSuccess(spot -> {
                                dataInvalidationEvent.fire(new DataInvalidation<Spot>());
                            }));
                    return PromiseUtils.resolve();
                })
                .catch_((s) -> {
                    @SuppressWarnings("unchecked")
                    Set<ConstraintViolation<Spot>> errors = (Set<ConstraintViolation<Spot>>) s;
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
                    SpotRestServiceBuilder.addSpot(tenantStore.getCurrentTenantId(), getNewValue(),
                            FailureShownRestCallback.onSuccess(spot -> {
                                dataInvalidationEvent.fire(new DataInvalidation<Spot>());
                            }));
                    return PromiseUtils.resolve();
                })
                .catch_((s) -> {
                    @SuppressWarnings("unchecked")
                    Set<ConstraintViolation<Spot>> errors = (Set<ConstraintViolation<Spot>>) s;
                    ErrorPopup.show(CommonUtils.delimitCollection(errors, (e) -> e.getMessage(), "\n"));
                    return PromiseUtils.resolve();
                });
    }
}