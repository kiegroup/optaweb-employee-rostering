package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.TextBox;
import org.gwtbootstrap3.extras.tagsinput.client.ui.base.SingleValueTagsInput;
import org.gwtbootstrap3.extras.typeahead.client.base.CollectionDataset;
import org.gwtbootstrap3.extras.typeahead.client.base.Dataset;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.SkillConvertor;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;

@Dependent
@Templated
public class EmployeeSubform implements TakesValue<Employee> {

    @Inject
    Validator validator;

    @Inject
    @AutoBound
    private DataBinder<Employee> employeeModelDataBinder;

    @Inject
    @Bound(property = "name")
    @DataField
    private TextBox employeeNameTextBox;

    @Inject
    @Bound(property = "skillProficiencySet", converter = SkillConvertor.class)
    @DataField
    private SingleValueTagsInputExtension skillProficiencyList;

    @PostConstruct
    protected void initWidget() {
        skillProficiencyList.init(employeeModelDataBinder);
        skillProficiencyList.setItemValue(Skill::getName);
        skillProficiencyList.setItemText(Skill::getName);
        skillProficiencyList.reconfigure();
    }

    public Set<ConstraintViolation<Employee>> validate() {
        return validator.validate(employeeModelDataBinder.getWorkingModel());
    }

    public void reset() {
        employeeNameTextBox.setValue("");
    }

    public void submit(Callback<Employee, Set<ConstraintViolation<Employee>>> callback) {
        Set<ConstraintViolation<Employee>> validationErrorSet = validate();
        if (validationErrorSet.isEmpty()) {
            callback.onSuccess(employeeModelDataBinder.getModel());
        } else {
            callback.onFailure(validationErrorSet);
        }
    }

    public void setTenantId(Integer tenantId) {
        employeeModelDataBinder.getModel().setTenantId(tenantId);
    }

    public void setSkillList(List<Skill> skillList) {
        skillProficiencyList.setSkillSet(skillList);
        skillProficiencyList.removeAll();
        skillProficiencyList.setDatasets((Dataset<Skill>) new CollectionDataset<Skill>(skillList) {

            @Override
            public String getValue(Skill skill) {
                return (skill == null) ? "" : skill.getName();
            }
        });
        skillProficiencyList.reconfigure();
    }

    @Inject
    public void setEmployeeModel(Employee employeeModel) {
        employeeModelDataBinder.setModel(employeeModel);
    }

    @Override
    public void setValue(Employee value) {
        setEmployeeModel(value);
    }

    @Override
    public Employee getValue() {
        return employeeModelDataBinder.getModel();
    }

    public static class SingleValueTagsInputExtension extends SingleValueTagsInput<Skill> implements HasText {

        SkillConvertor skillConvertor = new SkillConvertor();
        DataBinder<Employee> dataBinder;
        boolean shouldFire = true;

        public void init(DataBinder<Employee> dataBinder) {
            this.dataBinder = dataBinder;
            this.addValueChangeHandler((e) -> {
                if (shouldFire) {
                    shouldFire = false;
                    dataBinder.getModel().setSkillProficiencySet(skillConvertor.toModelValue(e.getValue()));
                }
            });
        }

        public void setSkillSet(Collection<Skill> skillSet) {
            skillConvertor.setSkillSet(skillSet);
        }

        @Override
        public String getText() {
            return this.getValue();
        }

        @Override
        public void setText(String text) {
            this.removeAll();
            this.add(skillConvertor.toModelValue(text).stream().collect(Collectors.toList()));
            shouldFire = true;
        }

    }

}
