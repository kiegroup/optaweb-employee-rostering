package org.optaplanner.openshift.employeerostering.shared.shift;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.nmorel.gwtjackson.rest.processor.GenRestBuilder;
import io.swagger.annotations.Api;
import org.optaplanner.openshift.employeerostering.shared.rotation.view.RotationView;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;

@Api(tags = {"Shift"})
@Path("/tenant/{tenantId}/shift")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface ShiftRestService {

    // TODO clean up the order, method names and annotations in this file to be consistent with SpotRestService and EmployeeRestService

    /**
     * @param id never null
     * @return never null, the id
     */
    @GET
    @Path("/{id}")
    ShiftView getShift(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id);

    @GET
    @Path("/")
    List<ShiftView> getShifts(@PathParam("tenantId") Integer tenantId);

    /**
     * @param shift never null
     * @return never null, the id
     */
    @POST
    @Path("/add")
    Long addShift(@PathParam("tenantId") Integer tenantId, ShiftView shift);

    /**
     * @param shift never null
     */
    @PUT
    @Path("/update")
    Shift updateShift(@PathParam("tenantId") Integer tenantId, ShiftView shift);

    /**
     * @param id never null
     * @return return true if the shift was removed, false otherwise
     */
    @DELETE
    @Path("/{id}")
    Boolean removeShift(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id);

    // TODO /update should update a single item. Use /bulkUpdate for list
    @PUT
    @Path("/template/update")
    void updateRotation(@PathParam("tenantId") Integer tenantId, RotationView rotationView);

    @GET
    @Path("/template")
    RotationView getRotation(@PathParam("tenantId") Integer tenantId);

}
