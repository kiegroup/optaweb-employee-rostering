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

package org.optaweb.employeerostering.shared.tenant;

import java.time.ZoneId;
import java.util.List;

import javax.ws.rs.Consumes;
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
import org.optaweb.employeerostering.shared.roster.RosterState;

@Api(tags = {"Tenant"})
@Path("/tenant")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface TenantRestService {

    // ************************************************************************
    // Tenant
    // ************************************************************************

    @ApiOperation("Get a list of all tenants")
    @GET
    @Path("/")
    List<Tenant> getTenantList();

    /**
     * @param id never null
     * @return never null, the id
     */
    @ApiOperation("Get a tenant by id")
    @GET
    @Path("/{id : \\d+}")
    Tenant getTenant(@ApiParam(required = true) @PathParam("id") Integer id);

    /**
     * @param tenant never null
     * @return never null, with a {@link AbstractPersistable#getId()} that is never null
     */
    @ApiOperation("Add a new tenant")
    @POST
    @Path("/add")
    Tenant addTenant(@ApiParam(value = "with no id", required = true) RosterState intialRosterState);

    @ApiOperation("Removes a tenant")
    @POST
    @Path("/remove/{id}")
    Boolean removeTenant(@ApiParam(required = true) @PathParam("id") Integer id);

    // ************************************************************************
    // RosterParametrization
    // ************************************************************************

    /**
     * @param id never null
     * @return never null, the id
     */
    @ApiOperation("Get a tenant roster parametrization")
    @GET
    @Path("/{id}")
    RosterParametrization getRosterParametrization(@ApiParam(required = true) @PathParam("id") Integer id);

    @ApiOperation("Update a tenant roster parametrization")
    @POST
    @Path("/parametrization/update")
    RosterParametrization updateRosterParametrization(@ApiParam(required = true) RosterParametrization rosterParametrization);

    // TODO: Where should this be?
    @ApiOperation("Get supported timezones")
    @GET
    @Path("/supported/timezones")
    List<ZoneId> getSupportedTimezones();
}
