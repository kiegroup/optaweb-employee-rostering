package org.optaplanner.openshift.employeerostering.shared.tenant;

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
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;

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
    Tenant addTenant(@ApiParam(value = "with no id", required = true) Tenant tenant);

    // ************************************************************************
    // TenantConfiguration
    // ************************************************************************
    /**
     * @param id never null
     * @return never null, the id
     */
    @ApiOperation("Get a tenant configuration")
    @GET
    @Path("/{id}")
    TenantConfiguration getTenantConfiguration(@ApiParam(required = true) @PathParam("id") Integer id);

    @ApiOperation("Update a tenant configuration")
    @POST
    @Path("/config/update")
    TenantConfiguration updateTenantConfiguration(@ApiParam(required = true) TenantConfiguration tenantConfiguration);

}
