package org.optaplanner.openshift.employeerostering.shared.skill;

import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.nmorel.gwtjackson.rest.processor.GenRestBuilder;
import org.optaplanner.openshift.employeerostering.shared.domain.Roster;

@Path("/tenant/{tenantId}/skill")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface SkillRestService {

    @GET
    @Path("/")
    List<Skill> getSkillList(@PathParam("tenantId") Long tenantId);

    /**
     * @param id never null
     * @return never null, the id
     */
    @GET
    @Path("/{id}")
    Skill getSkill(@PathParam("tenantId") Long tenantId, @PathParam("id") Long id);

    /**
     * @param skill never null
     * @return never null, the id
     */
    @POST
    @Path("/add")
    Long addSkill(@PathParam("tenantId") Long tenantId, Skill skill);

    /**
     * @param id never null
     * @return never null, the id
     */
    @DELETE
    @Path("/{id}")
    Boolean removeSkill(@PathParam("tenantId") Long tenantId, @PathParam("id") Long id);


}
