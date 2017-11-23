package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.HasTitle;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;

public class EmployeeId implements HasTitle {

    Employee employee;

    public EmployeeId(Employee employee) {
        this.employee = employee;
    }

    @Override
    public String getTitle() {
        return employee.getName();
    }

    public Employee getEmployee() {
        return employee;
    }

    @Override
    public int hashCode() {
        return employee.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof EmployeeId) {
            EmployeeId other = (EmployeeId) o;
            return employee.equals(other.getEmployee());
        }
        return false;
    }

}
