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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
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
@Tag(name = "Roster")
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

    @GET
    @Path("/{id}")
    @Operation(summary = "Get Roster State", description = "Get the Roster State for a tenant")
    public RosterState getRosterState(@PathParam("tenantId") @Min(0) Integer tenantId) {
        return rosterService.getRosterState(tenantId);
    }

    // ************************************************************************
    // ShiftRosterView
    // ************************************************************************

    @GET
    @Path("/shiftRosterView/current")
    @Operation(summary = "Current Shift Roster", description = "Get the current shift roster view")
    public ShiftRosterView getCurrentShiftRosterView(@PathParam("tenantId") @Min(0) Integer tenantId,
            @QueryParam("p") Integer pageNumber,
            @QueryParam("n") Integer numberOfItemsPerPage) {
        return rosterService.getCurrentShiftRosterView(tenantId, pageNumber,
                numberOfItemsPerPage);
    }

    @GET
    @Path("/shiftRosterView")
    @Operation(summary = "View Shift Roster", description = "Get a shift roster view between two dates")
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
    @POST
    @Path("/shiftRosterView/for")
    @Operation(summary = "View Shift Roster For Spots",
            description = "Get a shift roster view between two dates for a subset of the spots")
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

    @GET
    @Path("/shiftRosterView/excel")
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Export Shift Roster",
            description = "Get a shift roster view between two dates for a subset of the spots as an excel file")
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

    @GET
    @Path("/availabilityRosterView/current")
    @Operation(summary = "Current Availability Roster", description = "Get the current availability roster view")
    public AvailabilityRosterView getCurrentAvailabilityRosterView(
            @PathParam("tenantId") @Min(0) Integer tenantId,
            @QueryParam("p") Integer pageNumber,
            @QueryParam("n") Integer numberOfItemsPerPage) {
        return rosterService.getCurrentAvailabilityRosterView(tenantId, pageNumber,
                numberOfItemsPerPage);
    }

    @GET
    @Path("/availabilityRosterView")
    @Operation(summary = "View Availability Roster", description = "Get an availability roster view between two dates")
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

    @POST
    @Path("/availabilityRosterView/for")
    @Operation(summary = "Availability Roster For Employees",
            description = "Get an availability roster view between two dates for a subset of the employees")
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

    @POST
    @Path("/solve")
    @Operation(summary = "Solve Roster", description = "Start solving the roster. This will assign each shift to an employee")
    public void solveRoster(@PathParam("tenantId") @Min(0) Integer tenantId) {
        rosterService.solveRoster(tenantId);
    }

    @POST
    @Path("/replan")
    @Operation(summary = "Replan Roster",
            description = "Start solving the roster in Nondisruptive mode. This will modify the published" +
                    " schedule to make it feasible with minimal changes.")
    public void replanRoster(@PathParam("tenantId") @Min(0) Integer tenantId) {
        rosterService.replanRoster(tenantId);
    }

    @POST
    @Path("/terminate")
    @Operation(summary = "Terminate Solver",
            description = "Stop solving the roster, if it hasn't terminated automatically already")
    public void terminateRosterEarly(@PathParam("tenantId") @Min(0) Integer tenantId) {
        rosterService.terminateRosterEarly(tenantId);
    }

    // @ApiOperation("Get the status of the Solver")
    @GET
    @Path("/status")
    @Operation(summary = "Solver Status", description = "Get the status of the Solver")
    public SolverStatus getSolverStatus(@PathParam("tenantId") @Min(0) Integer tenantId) {
        return rosterService.getSolverStatus(tenantId);
    }

    // ************************************************************************
    // Publish
    // ************************************************************************

    @POST
    @Path("/provision")
    @Operation(summary = "Provision Shifts",
            description = "Provision shifts from particular time buckets into the given period")
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

    @POST
    @Path("/publishAndProvision")
    @Operation(summary = "Publish and Provision",
            description = "Publish the next set of draft shifts and create new draft shifts from the rotation template")
    public PublishResult publishAndProvision(@PathParam("tenantId") @Min(0) Integer tenantId) {
        return rosterService.publishAndProvision(tenantId);
    }

    @POST
    @Path("/commitChanges")
    @Operation(summary = "Commit Changes",
            description = "Updates the original employee to match adjusted schedule; essentially a republish without provision")
    public void commitChanges(@PathParam("tenantId") @Min(0) Integer tenantId) {
        rosterService.commitChanges(tenantId);
    }
}
