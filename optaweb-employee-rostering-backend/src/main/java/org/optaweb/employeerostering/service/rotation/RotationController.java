package org.optaweb.employeerostering.service.rotation;

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
import org.optaweb.employeerostering.domain.rotation.view.TimeBucketView;

@Path("/rest/tenant/{tenantId}/rotation")
@ApplicationScoped
@Tag(name = "Rotation")
public class RotationController {

    private final RotationService rotationService;

    @Inject
    public RotationController(RotationService rotationService) {
        this.rotationService = rotationService;
    }

    @GET
    @Path("/")
    @Operation(summary = "List Time Buckets", description = "Get a list of all time buckets")
    public List<TimeBucketView> getTimeBucketList(@PathParam("tenantId") @Min(0) Integer tenantId) {
        return rotationService.getTimeBucketList(tenantId);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get Time Bucket", description = "Gets a time bucket by id")
    public TimeBucketView getTimeBucket(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return rotationService.getTimeBucket(tenantId, id);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete Time Bucket", description = "Deletes a time bucket by id")
    public Boolean deleteTimeBucket(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return rotationService.deleteTimeBucket(tenantId, id);
    }

    @POST
    @Path("/add")
    @Operation(summary = "Add Time Bucket", description = "Adds a new time bucket")
    public TimeBucketView createTimeBucket(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid TimeBucketView timeBucketView) {
        return rotationService.createTimeBucket(tenantId, timeBucketView);
    }

    @PUT
    @Path("/update")
    @Operation(summary = "Update Time Bucket", description = "Updates a time bucket")
    public TimeBucketView updateTimeBucket(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid TimeBucketView timeBucketView) {
        return rotationService.updateTimeBucket(tenantId, timeBucketView);
    }
}
