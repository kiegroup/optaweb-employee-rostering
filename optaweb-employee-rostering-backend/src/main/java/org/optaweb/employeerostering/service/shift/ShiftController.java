package org.optaweb.employeerostering.service.shift;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;

@Path("/rest/tenant/{tenantId}/shift")
@ApplicationScoped
@Tag(name = "Shift")
public class ShiftController {

    private final ShiftService shiftService;

    @Inject
    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GET
    @Path("/")
    @Operation(summary = "List Shifts", description = "Get a list of all shifts")
    public List<ShiftView> getShiftList(@PathParam("tenantId") @Min(0) Integer tenantId) {
        return shiftService.getShiftList(tenantId);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get Shift", description = "Gets a shift by id")
    public ShiftView getShift(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return shiftService.getShift(tenantId, id);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete Shift", description = "Deletes a shift by id")
    public Boolean deleteShift(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return shiftService.deleteShift(tenantId, id);
    }

    @POST
    @Path("/add")
    @Operation(summary = "Add Shift", description = "Adds a new shift")
    public ShiftView createShift(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid ShiftView shiftView) {
        return shiftService.createShift(tenantId, shiftView);
    }

    @PUT
    @Path("/update")
    @Operation(summary = "Update Shift", description = "Updates a shift")
    public ShiftView updateShift(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid ShiftView shiftView) {
        return shiftService.updateShift(tenantId, shiftView);
    }
}
