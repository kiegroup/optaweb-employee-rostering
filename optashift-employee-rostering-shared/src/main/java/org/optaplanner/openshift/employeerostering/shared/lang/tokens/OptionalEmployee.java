package org.optaplanner.openshift.employeerostering.shared.lang.tokens;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;

@Entity
public class OptionalEmployee extends AbstractPersistable {

    @ManyToOne
    private Employee employee;

    @SuppressWarnings("unused")
    public OptionalEmployee() {}

    public OptionalEmployee(Integer tenantId, Employee employee) {
        super(tenantId);
        this.employee = employee;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
