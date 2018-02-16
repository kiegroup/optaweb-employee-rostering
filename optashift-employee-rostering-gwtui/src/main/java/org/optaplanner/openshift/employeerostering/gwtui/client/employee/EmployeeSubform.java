package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

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
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.DataInvalidation;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.StringListToSkillSetConverter;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.TableRow;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Updatable;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;

@Dependent
@Templated("#row")
public class EmployeeSubform extends TableRow<Employee> implements TakesValue<Employee>, Updatable<Map<String, Skill>> {

    @Inject
    Validator validator;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private StringListToSkillSetConverter skillConvertor;

    @Inject
    @DataField
    private TextBox employeeNameTextBox;

    @Inject
    @DataField
    private MultipleSelect skillProficiencySet;

    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement employeeName;

    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement skillSet;

    @Inject
    private Event<DataInvalidation<Employee>> dataInvalidationEvent;

    @PostConstruct
    protected void initWidget() {
        dataBinder.getModel().setTenantId(tenantStore.getCurrentTenantId());
        skillConvertor.registerSkillMapListener(this);
        dataBinder.bind(employeeNameTextBox, "name");
        dataBinder.bind(skillProficiencySet, "skillProficiencySet", skillConvertor);

        dataBinder.<String> addPropertyChangeHandler("name", (e) -> {
            employeeName.innerHTML = new SafeHtmlBuilder().appendEscaped(e.getNewValue()).toSafeHtml().asString();
        });
        dataBinder.<Set<Skill>> addPropertyChangeHandler("skillProficiencySet", (e) -> {
            skillSet.innerHTML = new SafeHtmlBuilder().appendEscaped(CommonUtils.delimitCollection(e.getNewValue(),
                    (s) -> s.getName(), ",")).toSafeHtml().asString();
        });
    }

    public Set<ConstraintViolation<Employee>> validate() {
        return validator.validate(getNewValue());
    }

    public void reset() {
        employeeNameTextBox.setValue("");
    }

    public Promise<Employee> getIfValid() {
        return new Promise<Employee>((res, rej) -> {
            Set<ConstraintViolation<Employee>> validationErrorSet = validate();
            if (validationErrorSet.isEmpty()) {
                res.onInvoke(getNewValue());
            } else {
                rej.onInvoke(validationErrorSet);
            }
        });

    }

    @Override
    public void onUpdate(Map<String, Skill> data) {
        skillProficiencySet.clear();
        data.forEach((name, skill) -> {
            Option option = new Option();
            option.setName(name);
            option.setValue(name);
            option.setText(name);
            skillProficiencySet.add(option);
        });
        skillProficiencySet.refresh();
    }

    @Override
    protected void deleteRow() {
        EmployeeRestServiceBuilder.removeEmployee(tenantStore.getCurrentTenantId(), getValue().getId(),
                FailureShownRestCallback.onSuccess(success -> {
                    dataInvalidationEvent.fire(new DataInvalidation<Employee>());
                }));
    }

    @Override
    protected void updateRow() {
        getIfValid()
                .then((e) -> {
                    commitChanges();
                    setEditing(false);
                    EmployeeRestServiceBuilder.updateEmployee(tenantStore.getCurrentTenantId(), getNewValue(),
                            FailureShownRestCallback.onSuccess(employee -> {
                                dataInvalidationEvent.fire(new DataInvalidation<Employee>());
                            }));
                    return PromiseUtils.resolve();
                })
                .catch_((s) -> {
                    @SuppressWarnings("unchecked")
                    Set<ConstraintViolation<Employee>> errors = (Set<ConstraintViolation<Employee>>) s;
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
                    EmployeeRestServiceBuilder.addEmployee(tenantStore.getCurrentTenantId(), getNewValue(),
                            FailureShownRestCallback.onSuccess(employee -> {
                                dataInvalidationEvent.fire(new DataInvalidation<Employee>());
                            }));
                    return PromiseUtils.resolve();
                })
                .catch_((s) -> {
                    @SuppressWarnings("unchecked")
                    Set<ConstraintViolation<Employee>> errors = (Set<ConstraintViolation<Employee>>) s;
                    ErrorPopup.show(CommonUtils.delimitCollection(errors, (e) -> e.getMessage(), "\n"));
                    return PromiseUtils.resolve();
                });
    }
}