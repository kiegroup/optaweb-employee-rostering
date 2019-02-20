/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.shared.roster;

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
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.roster.view.AvailabilityRosterView;
import org.optaweb.employeerostering.shared.roster.view.ShiftRosterView;
import org.optaweb.employeerostering.shared.spot.Spot;

@Api(tags = {"Roster"})
@Path("/tenant/{tenantId}/roster")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface RosterRestService {

    // ************************************************************************
    // RosterState
    // ************************************************************************
    @ApiOperation("Fetches the current Roster State")
    @GET
    @Path("/state")
    RosterState getRosterState(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId);

    // ************************************************************************
    // ShiftRosterView
    // ************************************************************************

    @ApiOperation("Get the current shift roster view")
    @GET
    @Path("/shiftRosterView/current")
    ShiftRosterView getCurrentShiftRosterView(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
                                              @ApiParam @QueryParam("p") Integer pageNumber,
                                              @ApiParam @QueryParam("n") Integer numberOfItemsPerPage);

    @ApiOperation("Get a shift roster view between 2 dates")
    @GET
    @Path("/shiftRosterView")
    ShiftRosterView getShiftRosterView(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
                                       @ApiParam @QueryParam("p") Integer pageNumber,
                                       @ApiParam @QueryParam("n") Integer numberOfItemsPerPage,
                                       @ApiParam(value = "inclusive", required = true) @QueryParam("startDate") String startDateString,
                                       @ApiParam(value = "exclusive", required = true) @QueryParam("endDate") String endDateString);

    //TODO: find out if there a way to pass lists in GET requests
    @ApiOperation("Get a shift roster view between 2 dates for a subset of the spots")
    @POST
    @Path("/shiftRosterView/for")
    // TODO naming "for" is too abstract: we might add a sibling rest method that filters on another type than spots too
    ShiftRosterView getShiftRosterViewFor(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
                                          @ApiParam(value = "inclusive", required = true) @QueryParam("startDate") String startDateString,
                                          @ApiParam(value = "exclusive", required = true) @QueryParam("endDate") String endDateString,
                                          @ApiParam(required = true) List<Spot> spots);

    // ************************************************************************
    // AvailabilityRosterView
    // ************************************************************************

    @ApiOperation("Get the current availability roster view")
    @GET
    @Path("/availabilityRosterView/current")
    AvailabilityRosterView getCurrentAvailabilityRosterView(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
                                                            @ApiParam @QueryParam("p") Integer pageNumber,
                                                            @ApiParam @QueryParam("n") Integer numberOfItemsPerPage);

    @ApiOperation("Get an availability roster view between 2 dates")
    @GET
    @Path("/availabilityRosterView")
    AvailabilityRosterView getAvailabilityRosterView(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
                                                     @ApiParam @QueryParam("p") Integer pageNumber,
                                                     @ApiParam @QueryParam("n") Integer numberOfItemsPerPage,
                                                     @ApiParam(value = "inclusive", required = true) @QueryParam("startDate") String startDateString,
                                                     @ApiParam(value = "exclusive", required = true) @QueryParam("endDate") String endDateString);

    @ApiOperation("Get an availability roster view between 2 dates for a subset of the employees")
    @POST
    @Path("/availabilityRosterView/for")
        // TODO naming "for" is too abstract: we might add a sibling rest method that filters on another type than spots too
    AvailabilityRosterView getAvailabilityRosterViewFor(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
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

    // ************************************************************************
    // Publishing/Provisioning methods
    // ************************************************************************

    @ApiOperation("Publishes the next set of draft shifts and creates new draft shift from the rotation template.")
    @POST
    @Path("/publishAndProvision")
    PublishResult publishAndProvision(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId);
}
