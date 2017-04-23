package org.optaplanner.openshift.employeerostering.shared.rest;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.nmorel.gwtjackson.rest.processor.GenRestBuilder;
import org.optaplanner.openshift.employeerostering.domain.Person;
import org.optaplanner.openshift.employeerostering.shared.domain.Roster;

@Path("/roster")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface RosterService {

    @GET
    @Path("/{id}")
    Roster getRoster(@PathParam("id") String id);

    @GET
    @Path("/")
    List<Roster> getRosterList();

}
