package org.optaplanner.openshift.employeerostering.domain;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.nmorel.gwtjackson.rest.processor.GenRestBuilder;

@Path("/employee")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface EmployeeService {

    @GET
    @Path("/one")
    Employee getEmployeeOne();

    @GET
    @Path("/")
    List<Employee> getEmployeeList();

}
