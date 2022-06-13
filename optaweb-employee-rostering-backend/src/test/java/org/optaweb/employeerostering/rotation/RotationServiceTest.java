package org.optaweb.employeerostering.rotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.time.LocalTime;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.rotation.TimeBucket;
import org.optaweb.employeerostering.domain.rotation.view.TimeBucketView;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;
import org.optaweb.employeerostering.service.rotation.RotationService;
import org.optaweb.employeerostering.service.spot.SpotService;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusTest
public class RotationServiceTest extends AbstractEntityRequireTenantRestServiceTest {

    @Inject
    RotationService rotationService;

    @Inject
    SpotService spotService;

    private Spot createSpot(Integer tenantId, String name, Set<Skill> requiredSkillSet) {
        SpotView spotView = new SpotView(tenantId, name, requiredSkillSet);
        return spotService.createSpot(tenantId, spotView);
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
    public void getTimeBucketListTest() {
        RestAssured.get("/rest/tenant/{tenantId}/rotation/", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    public void getTimeBucketTest() {
        Spot spot = createSpot(TENANT_ID, "spot", Collections.emptySet());

        TimeBucketView timeBucketView = new TimeBucketView(new TimeBucket(TENANT_ID, spot, LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptyList()));
        TimeBucketView persistedTimeBucket = rotationService.createTimeBucket(TENANT_ID, timeBucketView);

        RestAssured.get("/rest/tenant/{tenantId}/rotation/{id}", TENANT_ID, persistedTimeBucket.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("spotId", equalTo(spot.getId().intValue()))
                .body("startTime", equalTo("09:00:00"))
                .body("endTime", equalTo("17:00:00"));
    }

    @Test
    public void getNonExistentTimeBucketTest() {
        String exceptionMessage = "No TimeBucket entity found with ID (0).";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        RestAssured.get("/rest/tenant/{tenantId}/rotation/{id}", TENANT_ID, 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void deleteTimeBucketTest() {
        Spot spot = createSpot(TENANT_ID, "spot", Collections.emptySet());

        TimeBucketView timeBucketView = new TimeBucketView(new TimeBucket(TENANT_ID, spot, LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptyList()));
        TimeBucketView persistedTimeBucket = rotationService.createTimeBucket(TENANT_ID, timeBucketView);

        boolean result = RestAssured.delete("/rest/tenant/{tenantId}/rotation/{id}", TENANT_ID, persistedTimeBucket.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(Boolean.class);
        assertThat(result).isTrue();
    }

    @Test
    public void deleteNonMatchingTimeBucketTest() {
        Spot spot = createSpot(TENANT_ID, "spot", Collections.emptySet());

        TimeBucketView timeBucketView = new TimeBucketView(new TimeBucket(TENANT_ID, spot, LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptyList()));
        TimeBucketView persistedTimeBucket = rotationService.createTimeBucket(TENANT_ID, timeBucketView);

        String timeBucketName = "[TimeBucket-" + persistedTimeBucket.getId() + "]";

        String exceptionMessage = "The tenantId (0) does not match the persistable (" + timeBucketName +
                ")'s tenantId (" + TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        RestAssured.delete("/rest/tenant/{tenantId}/rotation/{id}", 0, persistedTimeBucket.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void createTimeBucketTest() {
        Spot spot = createSpot(TENANT_ID, "spot", Collections.emptySet());

        TimeBucketView timeBucketView = new TimeBucketView(new TimeBucket(TENANT_ID, spot, LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptyList()));

        RestAssured.given()
                .body(timeBucketView)
                .post("/rest/tenant/{tenantId}/rotation/add", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("spotId", equalTo(spot.getId().intValue()))
                .body("startTime", equalTo("09:00:00"))
                .body("endTime", equalTo("17:00:00"));
    }

    @Test
    public void createNonMatchingTimeBucketTest() {
        Spot spot = createSpot(TENANT_ID, "spot", Collections.emptySet());

        TimeBucketView timeBucketView = new TimeBucketView(new TimeBucket(0, spot, LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptyList()));

        String timeBucketName = "[TimeBucket-null]";

        String exceptionMessage = "The tenantId (" + TENANT_ID + ") does not match the persistable (" +
                timeBucketName + ")'s tenantId (0).";
        String exceptionClass = "java.lang.IllegalStateException";

        RestAssured.given()
                .body(timeBucketView)
                .post("/rest/tenant/{tenantId}/rotation/add", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void updateTimeBucketTest() {
        Spot spotA = createSpot(TENANT_ID, "A", Collections.emptySet());
        Spot spotB = createSpot(TENANT_ID, "B", Collections.emptySet());

        TimeBucketView timeBucketView = new TimeBucketView(new TimeBucket(TENANT_ID, spotA, LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptyList()));

        TimeBucketView persistedTimeBucket = rotationService.createTimeBucket(TENANT_ID, timeBucketView);

        TimeBucketView updatedTimeBucket = new TimeBucketView(new TimeBucket(TENANT_ID, spotB, LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptyList()));
        updatedTimeBucket.setId(persistedTimeBucket.getId());

        RestAssured.given()
                .body(updatedTimeBucket)
                .put("/rest/tenant/{tenantId}/rotation/update", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("spotId", equalTo(spotB.getId().intValue()))
                .body("startTime", equalTo("09:00:00"))
                .body("endTime", equalTo("17:00:00"));
    }

    @Test
    public void updateNonExistentTimeBucketTest() {
        String exceptionMessage = "TimeBucket entity with ID (0) not found.";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        Spot spot = createSpot(TENANT_ID, "spot", Collections.emptySet());

        TimeBucketView timeBucketView = new TimeBucketView(new TimeBucket(TENANT_ID, spot, LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptyList()));
        timeBucketView.setId(0L);

        RestAssured.given()
                .body(timeBucketView)
                .put("/rest/tenant/{tenantId}/rotation/update", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }
}
