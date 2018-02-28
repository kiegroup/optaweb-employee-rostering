package org.optaplanner.openshift.employeerostering.shared.roster;

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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.roster.view.EmployeeRosterView;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

@Api(tags = {"Roster"})
@Path("/tenant/{tenantId}/roster")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface RosterRestService {

    // ************************************************************************
    // SpotRosterView
    // ************************************************************************

    @ApiOperation("Get the current spot roster view")
    @GET
    @Path("/spotRosterView/current")
    SpotRosterView getCurrentSpotRosterView(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
                                            @ApiParam(required = true) @QueryParam("p") Integer pageNumber,
                                            @ApiParam(required = true) @QueryParam("n") Integer numberOfItemsPerPage);

    @ApiOperation("Get a spot roster view between 2 dates")
    @GET
    @Path("/spotRosterView")
    SpotRosterView getSpotRosterView(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
                                     @ApiParam(value = "inclusive", required = true) @QueryParam("startDate") String startDateString,
                                     @ApiParam(value = "exclusive", required = true) @QueryParam("endDate") String endDateString);

    //TODO: find out if there a way to pass lists in GET requests
    @ApiOperation("Get a spot roster view between 2 dates for a subset of the spots")
    @POST
    @Path("/spotRosterView/for")
    // TODO naming "for" is too abstract: we might add a sibling rest method that filters on another type than spots too
    SpotRosterView getSpotRosterViewFor(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
                                        @ApiParam(value = "inclusive", required = true) @QueryParam("startDate") String startDateString,
                                        @ApiParam(value = "exclusive", required = true) @QueryParam("endDate") String endDateString,
                                        @ApiParam(required = true) List<Spot> spots);

    // ************************************************************************
    // EmployeeRosterView
    // ************************************************************************

    @ApiOperation("Get the current employee roster view")
    @GET
    @Path("/employeeRosterView/current")
    EmployeeRosterView getCurrentEmployeeRosterView(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId);

    @ApiOperation("Get an employee roster view between 2 dates")
    @GET
    @Path("/employeeRosterView")
    EmployeeRosterView getEmployeeRosterView(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
                                             @ApiParam(value = "inclusive", required = true) @QueryParam("startDate") String startDateString,
                                             @ApiParam(value = "exclusive", required = true) @QueryParam("endDate") String endDateString);

    @ApiOperation("Get an employee roster view between 2 dates for a subset of the employees")
    @POST
    @Path("/employeeRosterView/for")
        // TODO naming "for" is too abstract: we might add a sibling rest method that filters on another type than spots too
    EmployeeRosterView getEmployeeRosterViewFor(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
                                                @ApiParam(value = "inclusive", required = true) @QueryParam("startDate") String startDateString,
                                                @ApiParam(value = "exclusive", required = true) @QueryParam("endDate") String endDateString,
                                                @ApiParam(required = true) List<Employee> employees);

    // ************************************************************************
    // Solver methods
    // ************************************************************************

    @ApiOperation("Start solving the roster. This will assign each shift to an employee.")
    @POST
    @Path("/solve")
    void solveRoster(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId);

    @ApiOperation("Stop solving the roster, if it hasn't terminated automatically already.")
    @POST
    @Path("/terminate")
    void terminateRosterEarly(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId);

    // Not a REST method
    Roster buildRoster(Integer tenantId);

    // Not a REST method
    void updateShiftsOfRoster(Roster newRoster);
}
