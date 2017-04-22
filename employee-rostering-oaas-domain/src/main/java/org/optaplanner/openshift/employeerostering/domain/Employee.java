package org.optaplanner.openshift.employeerostering.domain;

import java.io.Serializable;

public class Employee implements Serializable {

    private String name;

    private Employee() {
    }

    public Employee(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
