package org.optaplanner.openshift.employeerostering.shared.skill;

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

@Path("/tenant/{tenantId}/skill")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface SkillRestService {

    @GET
    @Path("/")
    List<Skill> getSkillList(@PathParam("tenantId") Integer tenantId);

    /**
     * @param id never null
     * @return never null, the id
     */
    @GET
    @Path("/{id}")
    Skill getSkill(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id);
    
    /**
     * @param id never null
     * @param skill never null
     * @return true if the skill was successfully updated, false otherwise
     */
    @POST
    @Path("/update/{id}")
    Boolean updateSkill(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id, Skill newValue);

    /**
     * @param skill never null
     * @return never null, the id
     */
    @POST
    @Path("/add")
    Long addSkill(@PathParam("tenantId") Integer tenantId, Skill skill);

    /**
     * @param id never null
     * @return true if the skill was successfully removed, false otherwise
     */
    @DELETE
    @Path("/{id}")
    Boolean removeSkill(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id);

}
