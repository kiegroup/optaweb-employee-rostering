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

import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;

import com.github.nmorel.gwtjackson.rest.processor.GenRestBuilder;

import io.swagger.annotations.Api;

@Api(tags = { "Skill" })
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
     * @param skill never null
     * @return never null, with a {@link AbstractPersistable#getId()} that is never null
     */
    @POST
    @Path("/add")
    Skill addSkill(@PathParam("tenantId") Integer tenantId, Skill skill);

    /**
     * @param skill never null
     * @return never null, with an updated {@link AbstractPersistable#getVersion()}
     */
    @POST
    @Path("/update/{id}")
    Skill updateSkill(@PathParam("tenantId") Integer tenantId, Skill skill);

    /**
     * @param id never null
     * @return true if the skill was successfully removed, false otherwise
     */
    @DELETE
    @Path("/{id}")
    Boolean removeSkill(@PathParam("tenantId") Integer tenantId, @PathParam("id") Long id);

}
