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

package org.optaweb.employeerostering.service.tenant;

import java.time.ZoneId;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.optaweb.employeerostering.domain.roster.view.RosterStateView;
import org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.domain.tenant.view.RosterConstraintConfigurationView;

@Path("/rest/tenant")
@ApplicationScoped
// @Api(tags = "Tenant")
public class TenantController {

    private final TenantService tenantService;

    @Inject
    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    // ************************************************************************
    // Tenant
    // ************************************************************************

    // @ApiOperation("Get a list of all tenants")
    @GET
    @Path("/")
    public List<Tenant> getTenantList() {
        return tenantService.getTenantList();
    }

    // @ApiOperation("Get a tenant by id")
    @GET
    @Path("/{id}")
    public Tenant getTenant(@PathParam("id") @Min(0) Integer id) {
        return tenantService.getTenant(id);
    }

    // @ApiOperation("Add a new tenant")
    @POST
    @Path("/add")
    public Tenant createTenant(@Valid RosterStateView initialRosterStateView) {
        return tenantService.createTenant(initialRosterStateView);
    }

    // @ApiOperation("Delete a tenant")
    @POST
    @Path("/remove/{id}")
    public Boolean deleteTenant(@PathParam("id") @Min(0) Integer id) {
        return tenantService.deleteTenant(id);
    }

    // ************************************************************************
    // RosterConstraintConfiguration
    // ************************************************************************

    // @ApiOperation("Get a tenant constraint configuration")
    @GET
    @Path("/{tenantId}/config/constraint")
    public RosterConstraintConfiguration getRosterConstraintConfiguration(
            @PathParam("tenantId") @Min(0) Integer tenantId) {
        return tenantService.getRosterConstraintConfiguration(tenantId);
    }

    // @ApiOperation("Update a tenant roster parametrization")
    @POST
    @Path("/{tenantId}/config/constraint/update")
    public RosterConstraintConfiguration updateRosterConstraintConfiguration(
            @Valid RosterConstraintConfigurationView rosterConstraintConfigurationView) {
        return tenantService.updateRosterConstraintConfiguration(rosterConstraintConfigurationView);
    }

    // TODO: Where should this be?
    // @ApiOperation("Get supported timezones")
    @GET
    @Path("/supported/timezones")
    public List<ZoneId> getSupportedTimezones() {
        return tenantService.getSupportedTimezones();
    }
}
