package org.optaweb.employeerostering.skill;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.skill.view.SkillView;
import org.optaweb.employeerostering.service.skill.SkillService;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusTest
public class SkillServiceTest extends AbstractEntityRequireTenantRestServiceTest {

    @Inject
    SkillService skillService;

    @BeforeEach
    public void setup() {
        createTestTenant();
    }

    @AfterEach
    public void cleanup() {
        deleteTestTenant();
    }

    @Test
    public void getSkillListTest() {
        RestAssured.get("/rest/tenant/{tenantId}/skill/", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    public void getSkillTest() {
        SkillView skillView = new SkillView(TENANT_ID, "skill");
        Skill skill = skillService.createSkill(TENANT_ID, skillView);

        RestAssured.get("/rest/tenant/{tenantId}/skill/{id}", TENANT_ID, skill.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("name", equalTo("skill"));
    }

    @Test
    public void getNonExistentSkillTest() {
        String exceptionMessage = "No Skill entity found with ID (0).";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        RestAssured.get("/rest/tenant/{tenantId}/skill/{id}", TENANT_ID, 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void getNonMatchingSkillTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable (skill)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        SkillView skillView = new SkillView(TENANT_ID, "skill");
        Skill skill = skillService.createSkill(TENANT_ID, skillView);

        RestAssured.get("/rest/tenant/{tenantId}/skill/{id}", 0, skill.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void deleteSkillTest() {
        SkillView skillView = new SkillView(TENANT_ID, "skill");
        Skill skill = skillService.createSkill(TENANT_ID, skillView);

        boolean result = RestAssured.delete("/rest/tenant/{tenantId}/skill/{id}", TENANT_ID, skill.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(Boolean.class);
        assertThat(result).isTrue();
    }

    @Test
    public void deleteNonExistentSkillTest() {
        boolean result = RestAssured.delete("/rest/tenant/{tenantId}/skill/{id}", TENANT_ID, 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(Boolean.class);
        assertThat(result).isFalse();
    }

    @Test
    public void deleteNonMatchingSkillTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable (skill)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        SkillView skillView = new SkillView(TENANT_ID, "skill");
        Skill skill = skillService.createSkill(TENANT_ID, skillView);

        RestAssured.delete("/rest/tenant/{tenantId}/skill/{id}", 0, skill.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void createSkillTest() {
        SkillView skillView = new SkillView(TENANT_ID, "skill");

        RestAssured.given()
                .body(skillView)
                .post("/rest/tenant/{tenantId}/skill/add", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("name", equalTo("skill"));
    }

    @Test
    public void createNonMatchingSkillTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable (skill)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        SkillView skillView = new SkillView(TENANT_ID, "skill");

        RestAssured.given()
                .body(skillView)
                .post("/rest/tenant/{tenantId}/skill/add", 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void updateSkillTest() {
        SkillView skillView = new SkillView(TENANT_ID, "skill");
        Skill skill = skillService.createSkill(TENANT_ID, skillView);

        SkillView updatedSkill = new SkillView(TENANT_ID, "updatedSkill");
        updatedSkill.setId(skill.getId());

        RestAssured.given()
                .body(updatedSkill)
                .post("/rest/tenant/{tenantId}/skill/update", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("name", equalTo("updatedSkill"));
    }

    @Test
    public void updateNonMatchingSkillTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable (updatedSkill)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        SkillView skillView = new SkillView(TENANT_ID, "skill");
        skillService.createSkill(TENANT_ID, skillView);

        SkillView updatedSkill = new SkillView(TENANT_ID, "updatedSkill");

        RestAssured.given()
                .body(updatedSkill)
                .post("/rest/tenant/{tenantId}/skill/update", 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void updateNonExistentSkillTest() {
        String exceptionMessage = "Skill entity with ID (0) not found.";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        SkillView skillView = new SkillView(TENANT_ID, "skill");
        skillView.setId(0L);

        RestAssured.given()
                .body(skillView)
                .post("/rest/tenant/{tenantId}/skill/update", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void updateChangeTenantIdSkillTest() {
        String exceptionMessage = "Skill entity with tenantId (" + TENANT_ID + ") cannot change tenants.";
        String exceptionClass = "java.lang.IllegalStateException";

        SkillView skillView = new SkillView(TENANT_ID, "skill");
        Skill skill = skillService.createSkill(TENANT_ID, skillView);

        SkillView updatedSkill = new SkillView(0, "updatedSkill");
        updatedSkill.setId(skill.getId());

        RestAssured.given()
                .body(updatedSkill)
                .post("/rest/tenant/{tenantId}/skill/update", 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }
}
