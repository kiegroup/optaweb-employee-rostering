package org.optaplanner.openshift.employeerostering.shared.roster;

import java.time.LocalDate;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.github.nmorel.gwtjackson.rest.processor.GenRestBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

@Path("/tenant/{tenantId}/roster")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface RosterRestService {

    @GET
    @Path("/spotRosterView")
    SpotRosterView getCurrentSpotRosterView(@PathParam("tenantId") Integer tenantId);

    @GET
    @Path("/spotRosterView")
    SpotRosterView getCurrentSpotRosterView(@PathParam("tenantId") Integer tenantId,
            @QueryParam("startDate") String startDateString, @QueryParam("endDate") String endDateString);

    @GET
    @Path("/")
    // TODO REMOVE ME
    Roster getRoster(@PathParam("tenantId") Integer tenantId);

    @POST
    @Path("/solve")
    void solveRoster(@PathParam("tenantId") Integer tenantId);

}
