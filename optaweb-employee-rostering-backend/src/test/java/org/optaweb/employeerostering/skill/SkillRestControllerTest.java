package org.optaweb.employeerostering.skill;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.skill.view.SkillView;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@QuarkusTest
public class SkillRestControllerTest extends AbstractEntityRequireTenantRestServiceTest {

    private final String skillPathURI = "/rest/tenant/{tenantId}/skill/";

    private Response getSkills(Integer tenantId) {
        return RestAssured.get(skillPathURI, tenantId);
    }

    private Response getSkill(Integer tenantId, Long id) {
        return RestAssured.get(skillPathURI + id, tenantId);
    }

    private void deleteSkill(Integer tenantId, Long id) {
        RestAssured.delete(skillPathURI + id, tenantId);
    }

    private Response addSkill(Integer tenantId, SkillView skillView) {
        return RestAssured.given()
                .body(skillView)
                .post(skillPathURI + "add", tenantId);
    }

    private Response updateSkill(Integer tenantId, SkillView skillView) {
        return RestAssured.given()
                .body(skillView)
                .post(skillPathURI + "update", tenantId);
    }

    @BeforeEach
    public void setup() {
        createTestTenant();
    }

    @AfterEach
    public void cleanup() {
        deleteTestTenant();
    }

    @Test
    public void skillCrudTest() {
        SkillView skillView = new SkillView(TENANT_ID, "skill");
        Response postResponse = addSkill(TENANT_ID, skillView);
        assertThat(postResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());

        Response response = getSkill(TENANT_ID, postResponse.as(Skill.class).getId());
        assertThat(response.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(response.getBody()).usingRecursiveComparison().ignoringFields("groovyResponse")
                .isEqualTo(postResponse.getBody());

        SkillView updatedSkill = new SkillView(TENANT_ID, "updatedSkill");
        updatedSkill.setId(postResponse.as(Skill.class).getId());
        Response putResponse = updateSkill(TENANT_ID, updatedSkill);
        assertThat(putResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());

        response = getSkill(TENANT_ID, putResponse.as(Skill.class).getId());
        assertThat(putResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(putResponse.getBody()).usingRecursiveComparison().ignoringFields("groovyResponse")
                .isEqualTo(response.getBody());

        deleteSkill(TENANT_ID, postResponse.as(Skill.class).getId());

        Response getListResponse = getSkills(TENANT_ID);
        assertThat(getListResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(getListResponse.jsonPath().getList("$", Skill.class)).isEmpty();
    }
}
