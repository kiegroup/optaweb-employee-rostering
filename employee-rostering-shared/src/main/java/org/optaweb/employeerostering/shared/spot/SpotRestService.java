/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.shared.spot;

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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.optaweb.employeerostering.shared.common.AbstractPersistable;

@Api(tags = {"Spot"})
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
    @Path("/update")
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

}
