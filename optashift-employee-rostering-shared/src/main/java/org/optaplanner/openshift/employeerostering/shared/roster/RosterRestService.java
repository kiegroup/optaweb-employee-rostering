package org.optaplanner.openshift.employeerostering.shared.roster;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.nmorel.gwtjackson.rest.processor.GenRestBuilder;

@Path("/tenant/{tenantId}/roster")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface RosterRestService {
    /**
     * @return never null, the id
     */
    @GET
    @Path("/")
    Roster getRoster(@PathParam("tenantId") Integer tenantId);

    @POST
    @Path("/solve")
    void solveRoster(@PathParam("tenantId") Integer tenantId);

}
