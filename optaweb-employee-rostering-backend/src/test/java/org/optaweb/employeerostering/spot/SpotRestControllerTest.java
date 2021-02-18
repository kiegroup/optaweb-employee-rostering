/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.spot;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.spot.view.SpotView;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@QuarkusTest
public class SpotRestControllerTest extends AbstractEntityRequireTenantRestServiceTest {

    private final String spotPathURI = "/rest/tenant/{tenantId}/spot/";

    private Response getSpots(Integer tenantId) {
        return RestAssured.get(spotPathURI, tenantId);
    }

    private Response getSpot(Integer tenantId, Long id) {
        return RestAssured.get(spotPathURI + id, tenantId);
    }

    private void deleteSpot(Integer tenantId, Long id) {
        RestAssured.delete(spotPathURI + id, tenantId);
    }

    private Response addSpot(Integer tenantId, SpotView spotView) {
        return RestAssured.given()
                .body(spotView)
                .post(spotPathURI + "add", tenantId);
    }

    private Response updateSpot(Integer tenantId, SpotView spotView) {
        return RestAssured.given()
                .body(spotView)
                .post(spotPathURI + "update", tenantId);
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
    public void spotCrudTest() {
        SpotView spotView = new SpotView(TENANT_ID, "spot", Collections.emptySet());
        Response postResponse = addSpot(TENANT_ID, spotView);
        assertThat(postResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());

        Response response = getSpot(TENANT_ID, postResponse.as(SpotView.class).getId());
        assertThat(response.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(response.getBody()).usingRecursiveComparison().ignoringFields("groovyResponse")
                .isEqualTo(postResponse.getBody());

        SpotView updatedSpot = new SpotView(TENANT_ID, "updatedSpot", Collections.emptySet());
        updatedSpot.setId(postResponse.as(SpotView.class).getId());
        Response putResponse = updateSpot(TENANT_ID, updatedSpot);
        assertThat(putResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());

        response = getSpot(TENANT_ID, putResponse.as(SpotView.class).getId());
        assertThat(putResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(putResponse.getBody()).usingRecursiveComparison().ignoringFields("groovyResponse")
                .isEqualTo(response.getBody());

        deleteSpot(TENANT_ID, postResponse.as(SpotView.class).getId());

        Response getListResponse = getSpots(TENANT_ID);
        assertThat(getListResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(getListResponse.jsonPath().getList("$", SpotView.class)).isEmpty();
    }
}
