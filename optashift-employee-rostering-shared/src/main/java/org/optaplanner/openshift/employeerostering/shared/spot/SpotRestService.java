package org.optaplanner.openshift.employeerostering.shared.spot;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.nmorel.gwtjackson.rest.processor.GenRestBuilder;

@Path("/tenant/{tenantId}/spot")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface SpotRestService {

    @GET
    @Path("/")
    List<Spot> getSpotList(@PathParam("tenantId") Long tenantId);

    /**
     * @param id never null
     * @return never null, the id
     */
    @GET
    @Path("/{id}")
    Spot getSpot(@PathParam("tenantId") Long tenantId, @PathParam("id") Long id);

    /**
     * @param spot never null
     * @return never null, the id
     */
    @POST
    @Path("/add")
    Long addSpot(@PathParam("tenantId") Long tenantId, Spot spot);

    /**
     * @param id never null
     * @return never null, the id
     */
    @DELETE
    @Path("/{id}")
    Boolean removeSpot(@PathParam("tenantId") Long tenantId, @PathParam("id") Long id);

}
