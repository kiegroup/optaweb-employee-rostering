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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;

import com.github.nmorel.gwtjackson.rest.processor.GenRestBuilder;

import io.swagger.annotations.Api;

@Api( tags = {"Spot"})
@Path("/tenant/{tenantId}/spot")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface SpotRestService {

    // ************************************************************************
    // Spot
    // ************************************************************************

    @ApiOperation("Get a list of all spots")
    @GET
    @Path("/")
    List<Spot> getSpotList(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId);

    /**
     * @param id never null
     * @return never null, the id
     */
    @ApiOperation("Get a spot by id")
    @GET
    @Path("/{id}")
    Spot getSpot(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
            @ApiParam(required = true) @PathParam("id") Long id);

    /**
     * @param spot never null
     * @return never null, with a {@link AbstractPersistable#getId()} that is never null
     */
    @ApiOperation("Add a new spot")
    @POST
    @Path("/add")
    Spot addSpot(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
            @ApiParam(value = "with no id", required = true) Spot spot);

    /**
     * @param spot never null
     * @return never null, with an updated {@link AbstractPersistable#getVersion()}
     */
    @ApiOperation("Update a spot")
    @POST
    @Path("/update/{id}")
    Spot updateSpot(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
            @ApiParam(required = true) Spot spot);

    /**
     * @param id never null
     * @return true if the spot was successfully removed, false otherwise
     */
    @ApiOperation("Delete a spot")
    @DELETE
    @Path("/{id}")
    Boolean removeSpot(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
            @ApiParam(required = true) @PathParam("id") Long id);

    // ************************************************************************
    // SpotGroup
    // ************************************************************************

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
}
