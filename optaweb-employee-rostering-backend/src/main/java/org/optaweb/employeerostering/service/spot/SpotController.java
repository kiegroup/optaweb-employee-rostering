package org.optaweb.employeerostering.service.spot;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;

@Path("/rest/tenant/{tenantId}/spot")
@ApplicationScoped
@Tag(name = "Spot")
public class SpotController {

    private final SpotService spotService;

    @Inject
    public SpotController(SpotService spotService) {
        this.spotService = spotService;
    }

    @GET
    @Path("/")
    @Operation(summary = "List Spots", description = "Get a list of all spots")
    public List<Spot> getSpotList(@PathParam("tenantId") @Min(0) Integer tenantId) {
        return spotService.getSpotList(tenantId);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get Spot", description = "Gets a spot by id")
    public Spot getSpot(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return spotService.getSpot(tenantId, id);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete Spot", description = "Deletes a spot by id")
    public Boolean deleteSpot(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return spotService.deleteSpot(tenantId, id);
    }

    @POST
    @Path("/add")
    @Operation(summary = "Add Spot", description = "Adds a new spot")
    public Spot createSpot(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid SpotView spotView) {
        return spotService.createSpot(tenantId, spotView);
    }

    @POST
    @Path("/update")
    @Operation(summary = "Update Spot", description = "Updates a spot")
    public Spot updateSpot(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid SpotView spotView) {
        return spotService.updateSpot(tenantId, spotView);
    }
}
