package org.optaplanner.openshift.employeerostering.shared.old;

import java.io.Serializable;

public class Person implements Serializable {

    private String name;

    private Person() {
    }

    public Person(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
