package org.optaplanner.openshift.employeerostering.server;

import java.util.Arrays;
import java.util.List;

import org.optaplanner.openshift.employeerostering.domain.Person;
import org.optaplanner.openshift.employeerostering.domain.PersonService;

public class PersonServiceImpl implements PersonService {

    public Person getPersonOne() {
        return new Person("Ann");
    }

    public List<Person> getPersonList() {
        return Arrays.asList(
                new Person("Ann"),
                new Person("Beth"),
                new Person("Carl"),
                new Person("Dan"),
                new Person("Ed"),
                new Person("Flo"));
    }

}
