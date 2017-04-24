package org.optaplanner.openshift.employeerostering.server.old;

import java.util.Arrays;
import java.util.List;

import org.optaplanner.openshift.employeerostering.shared.old.Person;
import org.optaplanner.openshift.employeerostering.shared.old.PersonService;

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
