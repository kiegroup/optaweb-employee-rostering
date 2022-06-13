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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.optaweb.employeerostering.domain.roster.view.RosterStateView;
import org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.domain.tenant.view.RosterConstraintConfigurationView;

@Path("/rest/tenant")
@ApplicationScoped
@Tag(name = "Tenant")
public class TenantController {

    private final TenantService tenantService;

    @Inject
    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    // ************************************************************************
    // Tenant
    // ************************************************************************

    @GET
    @Path("/")
    @Operation(summary = "List Tenants", description = "Get a list of all tenants")
    public List<Tenant> getTenantList() {
        return tenantService.getTenantList();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get Tenant", description = "Gets a tenant by id")
    public Tenant getTenant(@PathParam("id") @Min(0) Integer id) {
        return tenantService.getTenant(id);
    }

    @POST
    @Path("/add")
    @Operation(summary = "Add Tenant", description = "Adds a new tenant")
    public Tenant createTenant(@Valid RosterStateView initialRosterStateView) {
        return tenantService.createTenant(initialRosterStateView);
    }

    @POST
    @Path("/remove/{id}")
    @Operation(summary = "Delete Tenant", description = "Deletes a tenant by id")
    public Boolean deleteTenant(@PathParam("id") @Min(0) Integer id) {
        return tenantService.deleteTenant(id);
    }

    // ************************************************************************
    // RosterConstraintConfiguration
    // ************************************************************************

    @GET
    @Path("/{tenantId}/config/constraint")
    @Operation(summary = "Get Roster Parametrization", description = "Gets a tenant roster parametrization")
    public RosterConstraintConfiguration getRosterConstraintConfiguration(
            @PathParam("tenantId") @Min(0) Integer tenantId) {
        return tenantService.getRosterConstraintConfiguration(tenantId);
    }

    @POST
    @Path("/{tenantId}/config/constraint/update")
    @Operation(summary = "Update Roster Parametrization", description = "Updates a tenant roster parametrization")
    public RosterConstraintConfiguration updateRosterConstraintConfiguration(
            @Valid RosterConstraintConfigurationView rosterConstraintConfigurationView) {
        return tenantService.updateRosterConstraintConfiguration(rosterConstraintConfigurationView);
    }

    // TODO: Where should this be?
    @GET
    @Path("/supported/timezones")
    @Operation(summary = "Supported Timezones", description = "Get supported timezones")
    public List<ZoneId> getSupportedTimezones() {
        return tenantService.getSupportedTimezones();
    }
}
