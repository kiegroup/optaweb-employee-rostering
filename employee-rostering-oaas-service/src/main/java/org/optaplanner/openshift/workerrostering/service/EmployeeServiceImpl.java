package org.optaplanner.openshift.workerrostering.service;

import java.util.Arrays;
import java.util.List;

import org.optaplanner.openshift.workerrostering.domain.Employee;
import org.optaplanner.openshift.workerrostering.domain.EmployeeService;

public class EmployeeServiceImpl implements EmployeeService {

    public Employee getEmployeeOne() {
        return new Employee("Ann");
    }

    public List<Employee> getEmployeeList() {
        return Arrays.asList(
                new Employee("Ann"),
                new Employee("Beth"),
                new Employee("Carl"),
                new Employee("Dan"),
                new Employee("Ed"),
                new Employee("Flo"));
    }

}
