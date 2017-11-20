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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.github.nmorel.gwtjackson.rest.processor.GenRestBuilder;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;

@Path("/tenant/{tenantId}/shift")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface ShiftRestService {

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
    void updateShift(@PathParam("tenantId") Integer tenantId, ShiftView shift);

    @PUT
    @Path("/add/fromTemplate")
    List<Long> addShiftsFromTemplate(@PathParam("tenantId") Integer tenantId, @QueryParam("startDate") String startDateString, @QueryParam("endDate") String endDateString, @QueryParam("data") String data) throws Exception;

    /**
     * @param id never null
     * @return return true if the shift was removed, false otherwise
     */
    @DELETE
    @Path("/{id}")
    Boolean removeShift(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id);

}
