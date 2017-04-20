package org.optaplanner.openshift.workerrostering.service;

import java.util.Arrays;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.optaplanner.openshift.workerrostering.domain.Employee;

@Path("/employee")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeService {

    @GET
    @Path("/")
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
