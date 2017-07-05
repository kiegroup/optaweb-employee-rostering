package org.optaplanner.openshift.employeerostering.shared.shift;

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
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
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

    /**
     * @param id never null
     * @return never null, the id
     */
    @DELETE
    @Path("/{id}")
    Boolean removeShift(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id);

}
