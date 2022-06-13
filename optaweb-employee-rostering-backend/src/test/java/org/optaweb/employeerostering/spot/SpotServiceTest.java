package org.optaweb.employeerostering.spot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.skill.view.SkillView;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;
import org.optaweb.employeerostering.service.skill.SkillService;
import org.optaweb.employeerostering.service.spot.SpotService;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusTest
public class SpotServiceTest extends AbstractEntityRequireTenantRestServiceTest {

    @Inject
    SpotService spotService;

    @Inject
    SkillService skillService;

    private Skill createSkill(Integer tenantId, String name) {
        SkillView skillView = new SkillView(tenantId, name);
        return skillService.createSkill(tenantId, skillView);
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
    public void getSpotListTest() {
        RestAssured.get("/rest/tenant/{tenantId}/spot/", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    public void getSpotTest() {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        List<Skill> skillList = RestAssured.get("/rest/tenant/{tenantId}/spot/{id}", TENANT_ID, spot.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("name", equalTo("spot"))
                .extract().jsonPath().getList("requiredSkillSet", Skill.class);
        assertThat(skillList).containsExactlyInAnyOrderElementsOf(testSkillSet);
    }

    @Test
    public void getNonExistentSpotTest() {
        String exceptionMessage = "No Spot entity found with ID (0).";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        RestAssured.get("/rest/tenant/{tenantId}/spot/{id}", TENANT_ID, 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void getNonMatchingSpotTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable (spot)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        RestAssured.get("/rest/tenant/{tenantId}/spot/{id}", 0, spot.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void deleteSpotTest() {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        boolean result = RestAssured.delete("/rest/tenant/{tenantId}/spot/{id}", TENANT_ID, spot.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(Boolean.class);
        assertThat(result).isTrue();
    }

    @Test
    public void deleteNonExistentSpotTest() {
        boolean result = RestAssured.delete("/rest/tenant/{tenantId}/spot/{id}", TENANT_ID, 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(Boolean.class);
        assertThat(result).isFalse();
    }

    @Test
    public void deleteNonMatchingSpotTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable (spot)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        RestAssured.delete("/rest/tenant/{tenantId}/spot/{id}", 0, spot.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void createSpotTest() {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);

        List<Skill> skillList = RestAssured.given()
                .body(spotView)
                .post("/rest/tenant/{tenantId}/spot/add", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("name", equalTo("spot"))
                .extract().jsonPath().getList("requiredSkillSet", Skill.class);
        assertThat(skillList).containsExactlyInAnyOrderElementsOf(testSkillSet);
    }

    @Test
    public void createNonMatchingSpotTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable (spot)'s tenantId ("
                + TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);

        RestAssured.given()
                .body(spotView)
                .post("/rest/tenant/{tenantId}/spot/add", 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void updateSpotTest() {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        SpotView updatedSpot = new SpotView(TENANT_ID, "updatedSpot", testSkillSet);
        updatedSpot.setId(spot.getId());

        List<Skill> skillList = RestAssured.given()
                .body(updatedSpot)
                .post("/rest/tenant/{tenantId}/spot/update", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("name", equalTo("updatedSpot"))
                .extract().jsonPath().getList("requiredSkillSet", Skill.class);
        assertThat(skillList).containsExactlyInAnyOrderElementsOf(testSkillSet);
    }

    @Test
    public void updateNonMatchingSpotTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable (updatedSpot)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);
        spotService.createSpot(TENANT_ID, spotView);

        SpotView updatedSpot = new SpotView(TENANT_ID, "updatedSpot", testSkillSet);

        RestAssured.given()
                .body(updatedSpot)
                .post("/rest/tenant/{tenantId}/spot/update", 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void updateNonExistentSpotTest() {
        String exceptionMessage = "Spot entity with ID (0) not found.";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        SpotView spotView = new SpotView(TENANT_ID, "spot", Collections.emptySet());
        spotView.setId(0L);

        RestAssured.given()
                .body(spotView)
                .post("/rest/tenant/{tenantId}/spot/update", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void updateChangeTenantIdSpotTest() {
        String exceptionMessage = "Spot entity with tenantId (" + TENANT_ID + ") cannot change tenants.";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        SpotView updatedSpot = new SpotView(0, "updatedSpot", testSkillSet);
        updatedSpot.setId(spot.getId());

        RestAssured.given()
                .body(updatedSpot)
                .post("/rest/tenant/{tenantId}/spot/update", 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }
}
