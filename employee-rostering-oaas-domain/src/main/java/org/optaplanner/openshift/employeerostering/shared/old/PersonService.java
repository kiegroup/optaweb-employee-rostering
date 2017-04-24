package org.optaplanner.openshift.employeerostering.shared.old;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.nmorel.gwtjackson.rest.processor.GenRestBuilder;

@Path("/person")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface PersonService {

    @GET
    @Path("/one")
    Person getPersonOne();

    @GET
    @Path("/")
    List<Person> getPersonList();

}
