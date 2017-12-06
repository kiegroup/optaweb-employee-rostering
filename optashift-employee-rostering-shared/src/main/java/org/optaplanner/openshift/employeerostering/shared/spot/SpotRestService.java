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
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;

@Path("/tenant/{tenantId}/spot")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface SpotRestService {

    @GET
    @Path("/")
    List<Spot> getSpotList(@PathParam("tenantId") Integer tenantId);

    /**
     * @param id never null
     * @return never null, the id
     */
    @GET
    @Path("/{id}")
    Spot getSpot(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id);

    /**
     * @param spot never null
     * @return never null, with a {@link AbstractPersistable#getId()} that is never null
     */
    @POST
    @Path("/add")
    Spot addSpot(@PathParam("tenantId") Integer tenantId, Spot spot);

    /**
     * @param spot never null
     * @return never null, with an updated {@link AbstractPersistable#getVersion()}
     */
    @POST
    @Path("/update/{id}")
    Spot updateSpot(@PathParam("tenantId") Integer tenantId, Spot spot);

    @GET
    @Path("/groups/")
    List<SpotGroup> getSpotGroups(@PathParam("tenantId") Integer tenantId);

    @GET
    @Path("/groups/{id}")
    SpotGroup getSpotGroup(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id);

    @GET
    @Path("/groups/find/{name}")
    SpotGroup findSpotGroupByName(@PathParam("tenantId") Integer tenantId, @PathParam("name") String name);

    @POST
    @Path("/groups/create")
    Long createSpotGroup(@PathParam("tenantId") Integer tenantId, SpotGroup spotGroup);

    @POST
    @Path("/groups/{id}/add")
    void addSpotToSpotGroup(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id, Spot spot);

    @POST
    @Path("/groups/{id}/remove")
    void removeSpotFromSpotGroup(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id, Spot spot);

    @POST
    @Path("/groups/delete/{id}")
    Boolean deleteSpotGroup(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id);

    /**
     * @param id never null
     * @return true if the spot was successfully removed, false otherwise
     */
    @DELETE
    @Path("/{id}")
    Boolean removeSpot(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id);
}
