package org.optaplanner.openshift.employeerostering.shared.tenant;

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

@Api(tags = { "Tenant" })
@Path("/tenant")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface TenantRestService {

    @GET
    @Path("/")
    List<Tenant> getTenantList();

    /**
     * @param id never null
     * @return never null, the id
     */
    @GET
    @Path("/{id}")
    Tenant getTenant(@PathParam("id") Long id);

    @POST
    @Path("/{id}/config/update")
    Tenant updateTenantConfiguration(TenantConfiguration tenantConfiguration);

    /**
     * @param tenant never null
     * @return never null, with a {@link AbstractPersistable#getId()} that is never null
     */
    @POST
    @Path("/add")
    Tenant addTenant(Tenant tenant);

}
