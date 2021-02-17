/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.service.roster;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.optaplanner.core.api.solver.SolverStatus;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.roster.PublishResult;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.roster.view.AvailabilityRosterView;
import org.optaweb.employeerostering.domain.roster.view.ShiftRosterView;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.service.spot.SpotRepository;
import org.optaweb.employeerostering.util.ShiftRosterXlsxFileIO;

@Path("/rest/tenant/{tenantId}/roster")
@ApplicationScoped
// @Api(tags = "Roster")
public class RosterController {

    private final RosterService rosterService;
    private final SpotRepository spotRepository;

    @Inject
    public RosterController(RosterService rosterService, SpotRepository spotRepository) {
        this.rosterService = rosterService;
        this.spotRepository = spotRepository;
    }

    // ************************************************************************
    // RosterState
    // ************************************************************************

    // @ApiOperation("Get the current roster state")
    @GET
    @Path("/{id}")
    public RosterState getRosterState(@PathParam("tenantId") @Min(0) Integer tenantId) {
        return rosterService.getRosterState(tenantId);
    }

    // ************************************************************************
    // ShiftRosterView
    // ************************************************************************

    // @ApiOperation("Get the current shift roster view")
    @GET
    @Path("/shiftRosterView/current")
    public ShiftRosterView getCurrentShiftRosterView(@PathParam("tenantId") @Min(0) Integer tenantId,
            @QueryParam("p") Integer pageNumber,
            @QueryParam("n") Integer numberOfItemsPerPage) {
        return rosterService.getCurrentShiftRosterView(tenantId, pageNumber,
                numberOfItemsPerPage);
    }

    // @ApiOperation("Get a shift roster view between two dates")
    @GET
    @Path("/shiftRosterView")
    public ShiftRosterView getShiftRosterView(@PathParam("tenantId") @Min(0) Integer tenantId,
            @QueryParam("p") Integer pageNumber,
            @QueryParam("n") Integer numberOfItemsPerPage,
            @QueryParam("startDate") String startDateString,
            @QueryParam("endDate") String endDateString) {
        if (startDateString == null) {
            throw new IllegalArgumentException("query parameter startDate is required");
        }
        if (endDateString == null) {
            throw new IllegalArgumentException("query parameter endDate is required");
        }
        return rosterService.getShiftRosterView(tenantId, pageNumber, numberOfItemsPerPage,
                startDateString, endDateString);
    }

    // TODO: find out if there a way to pass lists in GET requests
    // TODO naming "for" is too abstract: we might add a sibling rest method that filters on another type than spots too
    // @ApiOperation("Get a shift roster view between two dates for a subset of the spots")
    @POST
    @Path("/shiftRosterView/for")
    public ShiftRosterView getShiftRosterViewFor(@PathParam("tenantId") @Min(0) Integer tenantId,
            @QueryParam("startDate") String startDateString,
            @QueryParam("endDate") String endDateString,
            @Valid List<Spot> spots) {
        if (startDateString == null) {
            throw new IllegalArgumentException("query parameter startDate is required");
        }
        if (endDateString == null) {
            throw new IllegalArgumentException("query parameter endDate is required");
        }
        return rosterService.getShiftRosterViewFor(tenantId, startDateString, endDateString,
                spots);
    }

    // @ApiOperation("Get a shift roster view between two dates for a subset of the spots as an excel file")
    @GET
    @Path("/shiftRosterView/excel")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response getShiftRosterViewAsExcel(@PathParam("tenantId") @Min(0) Integer tenantId,
            @QueryParam("startDate") String startDateString,
            @QueryParam("endDate") String endDateString,
            @QueryParam("spotList") String spotListString) {
        if (startDateString == null) {
            throw new IllegalArgumentException("query parameter startDate is required");
        }
        if (endDateString == null) {
            throw new IllegalArgumentException("query parameter endDate is required");
        }
        if (spotListString == null) {
            throw new IllegalArgumentException("query parameter spotList is required");
        }
        Set<Long> spotIdSet = Arrays.stream(spotListString.split(",")).map(Long::parseLong)
                .collect(Collectors.toSet());
        List<Spot> spotList = spotRepository.findAllByTenantId(tenantId)
                .stream().filter(s -> spotIdSet.contains(s.getId()))
                .collect(Collectors.toList());

        if (spotList.size() != spotIdSet.size()) {
            return Response.noContent().status(Response.Status.BAD_REQUEST).build();
        }
        ShiftRosterView shiftRosterView = rosterService.getShiftRosterViewFor(tenantId, startDateString, endDateString,
                spotList);
        try {
            String fileName = "Roster-" + startDateString + "--" +
                    endDateString + ".xlsx";
            return Response.ok(ShiftRosterXlsxFileIO.getExcelBytesForShiftRoster(shiftRosterView))
                    .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .build();
        } catch (IOException e) {
            return Response.noContent().status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ************************************************************************
    // AvailabilityRosterView
    // ************************************************************************

    // @ApiOperation("Get the current availability roster view")
    @GET
    @Path("/availabilityRosterView/current")
    public AvailabilityRosterView getCurrentAvailabilityRosterView(
            @PathParam("tenantId") @Min(0) Integer tenantId,
            @QueryParam("p") Integer pageNumber,
            @QueryParam("n") Integer numberOfItemsPerPage) {
        return rosterService.getCurrentAvailabilityRosterView(tenantId, pageNumber,
                numberOfItemsPerPage);
    }

    // @ApiOperation("Get an availability roster view between two dates")
    @GET
    @Path("/availabilityRosterView")
    public AvailabilityRosterView getAvailabilityRosterView(
            @PathParam("tenantId") @Min(0) Integer tenantId,
            @QueryParam("p") Integer pageNumber,
            @QueryParam("n") Integer numberOfItemsPerPage,
            @QueryParam("startDate") String startDateString,
            @QueryParam("endDate") String endDateString) {
        if (startDateString == null) {
            throw new IllegalArgumentException("query parameter startDate is required");
        }
        if (endDateString == null) {
            throw new IllegalArgumentException("query parameter endDate is required");
        }

        return rosterService.getAvailabilityRosterView(tenantId, pageNumber, numberOfItemsPerPage,
                startDateString, endDateString);
    }

    // @ApiOperation("Get an availability roster view between two dates for a subset of the employees")
    @POST
    @Path("/availabilityRosterView/for")
    // TODO naming "for" is too abstract: we might add a sibling rest method that filters on another type than spots too
    public AvailabilityRosterView getAvailabilityRosterViewFor(
            @PathParam("tenantId") @Min(0) Integer tenantId,
            @QueryParam("startDate") String startDateString,
            @QueryParam("endDate") String endDateString,
            @Valid List<Employee> employees) {
        if (startDateString == null) {
            throw new IllegalArgumentException("query parameter startDate is required");
        }
        if (endDateString == null) {
            throw new IllegalArgumentException("query parameter endDate is required");
        }
        return rosterService.getAvailabilityRosterViewFor(tenantId, startDateString,
                endDateString, employees);
    }

    // ************************************************************************
    // Solver
    // ************************************************************************

    // @ApiOperation("Start solving the roster. This will assign each shift to an employee")
    @POST
    @Path("/solve")
    public void solveRoster(@PathParam("tenantId") @Min(0) Integer tenantId) {
        rosterService.solveRoster(tenantId);
    }

    // @ApiOperation("Start solving the roster in Nondisruptive mode. This will modify the publish" +
    //        "schedule to make it feasible with minimal changes.")
    @POST
    @Path("/replan")
    public void replanRoster(@PathParam("tenantId") @Min(0) Integer tenantId) {
        rosterService.replanRoster(tenantId);
    }

    // @ApiOperation("Stop solving the roster, if it hasn't terminated automatically already")
    @POST
    @Path("/terminate")
    public void terminateRosterEarly(@PathParam("tenantId") @Min(0) Integer tenantId) {
        rosterService.terminateRosterEarly(tenantId);
    }

    // @ApiOperation("Get the status of the Solver")
    @GET
    @Path("/status")
    public SolverStatus getSolverStatus(@PathParam("tenantId") @Min(0) Integer tenantId) {
        return rosterService.getSolverStatus(tenantId);
    }

    // ************************************************************************
    // Publish
    // ************************************************************************

    // @ApiOperation("Provision shifts from particular time buckets into the given period")
    @POST
    @Path("/provision")
    public void provision(@PathParam("tenantId") @Min(0) Integer tenantId,
            @QueryParam("startRotationOffset") Integer startRotationOffset, @QueryParam("fromDate") String fromDate,
            @QueryParam("toDate") String toDate, List<Long> timeBucketIdList) {
        if (startRotationOffset == null) {
            throw new IllegalArgumentException("query parameter startRotationOffset is required");
        }
        if (fromDate == null) {
            throw new IllegalArgumentException("query parameter fromDate is required");
        }
        if (toDate == null) {
            throw new IllegalArgumentException("query parameter toDate is required");
        }
        rosterService.provision(tenantId, startRotationOffset, LocalDate.parse(fromDate), LocalDate.parse(toDate),
                timeBucketIdList);
    }

    // @ApiOperation("Publish the next set of draft shifts and create new draft shifts from the rotation template")
    @POST
    @Path("/publishAndProvision")
    public PublishResult publishAndProvision(@PathParam("tenantId") @Min(0) Integer tenantId) {
        return rosterService.publishAndProvision(tenantId);
    }

    // @ApiOperation("Updates the original employee to match adjusted schedule; essentially a republish without provision")
    @POST
    @Path("/commitChanges")
    public void commitChanges(@PathParam("tenantId") @Min(0) Integer tenantId) {
        rosterService.commitChanges(tenantId);
    }
}
