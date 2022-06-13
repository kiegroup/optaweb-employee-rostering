package org.optaweb.employeerostering.rotation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.Collections;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.rotation.TimeBucket;
import org.optaweb.employeerostering.domain.rotation.view.TimeBucketView;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@QuarkusTest
public class RotationRestControllerTest extends AbstractEntityRequireTenantRestServiceTest {

    private final String timeBucketPathURI = "/rest/tenant/{tenantId}/rotation/";
    private final String spotPathURI = "/rest/tenant/{tenantId}/spot/";

    private Response getTimeBuckets(Integer tenantId) {
        return RestAssured.get(timeBucketPathURI, tenantId);
    }

    private Response getTimeBucket(Integer tenantId, Long id) {
        return RestAssured.get(timeBucketPathURI + id, tenantId);
    }

    private void deleteTimeBucket(Integer tenantId, Long id) {
        RestAssured.delete(timeBucketPathURI + id, tenantId);
    }

    private Response addTimeBucket(Integer tenantId, TimeBucketView shiftTemplateView) {
        return RestAssured.given()
                .body(shiftTemplateView)
                .post(timeBucketPathURI + "add", tenantId);
    }

    private Response updateTimeBucket(Integer tenantId,
            TimeBucketView shiftTemplateView) {
        return RestAssured.given()
                .body(shiftTemplateView)
                .put(timeBucketPathURI + "update", tenantId);
    }

    private Response addSpot(Integer tenantId, SpotView spotView) {
        return RestAssured.given()
                .body(spotView)
                .post(spotPathURI + "add", tenantId);
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
    public void shiftTemplateCrudTest() {
        Response spotResponseA = addSpot(TENANT_ID, new SpotView(TENANT_ID, "A",
                Collections.emptySet()));
        Spot spotA = spotResponseA.as(Spot.class);

        TimeBucketView timeBucketView = new TimeBucketView(new TimeBucket(TENANT_ID, spotA, LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptyList()));
        Response postResponse = addTimeBucket(TENANT_ID, timeBucketView);
        assertThat(postResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());

        Response response = getTimeBucket(TENANT_ID, postResponse.as(TimeBucketView.class).getId());
        assertThat(response.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(response.getBody()).usingRecursiveComparison().ignoringFields("groovyResponse")
                .isEqualTo(postResponse.getBody());

        TimeBucketView updatedTimeBucket = new TimeBucketView(new TimeBucket(TENANT_ID, spotA, LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptyList()));
        updatedTimeBucket.setId(postResponse.as(TimeBucketView.class).getId());
        Response putResponse = updateTimeBucket(TENANT_ID, updatedTimeBucket);
        assertThat(putResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());

        response = getTimeBucket(TENANT_ID, putResponse.as(TimeBucketView.class).getId());
        assertThat(putResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(putResponse.getBody().as(TimeBucketView.class)).isEqualTo(response.getBody().as(TimeBucketView.class));

        deleteTimeBucket(TENANT_ID, putResponse.as(TimeBucketView.class).getId());

        Response getListResponse = getTimeBuckets(TENANT_ID);
        assertThat(getListResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(getListResponse.jsonPath().getList("$", TimeBucketView.class)).isEmpty();
    }
}
