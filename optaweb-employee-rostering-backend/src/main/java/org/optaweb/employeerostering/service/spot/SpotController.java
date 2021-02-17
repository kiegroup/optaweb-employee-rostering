/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;

@Path("/rest/tenant/{tenantId}/spot")
@ApplicationScoped
// @Api(tags = "Spot")
public class SpotController {

    private final SpotService spotService;

    @Inject
    public SpotController(SpotService spotService) {
        this.spotService = spotService;
        // Assert.notNull(spotService, "spotService must not be null.");
    }

    // @ApiOperation("Get a list of all spots")
    @GET
    @Path("/")
    public List<Spot> getSpotList(@PathParam("tenantId") @Min(0) Integer tenantId) {
        return spotService.getSpotList(tenantId);
    }

    // @ApiOperation("Get a spot by id")
    @GET
    @Path("/{id}")
    public Spot getSpot(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return spotService.getSpot(tenantId, id);
    }

    // @ApiOperation("Delete a spot")
    @DELETE
    @Path("/{id}")
    public Boolean deleteSpot(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return spotService.deleteSpot(tenantId, id);
    }

    // @ApiOperation("Add a new spot")
    @POST
    @Path("/add")
    public Spot createSpot(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid SpotView spotView) {
        return spotService.createSpot(tenantId, spotView);
    }

    // @ApiOperation("Update a spot")
    @POST
    @Path("/update")
    public Spot updateSpot(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid SpotView spotView) {
        return spotService.updateSpot(tenantId, spotView);
    }
}
