package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.util.HashSet;

import javax.inject.Inject;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;

@Bindable
@Portable
public class EmployeeModel extends Employee {

    @Inject
    public EmployeeModel() {
        super(-1, "");
    }

    public EmployeeModel(Employee employee) {
        super(employee.getTenantId(), employee.getName());
        setSkillProficiencySet(employee.getSkillProficiencySet());
        setId(employee.getId());
        setVersion(employee.getVersion());
    }
}
