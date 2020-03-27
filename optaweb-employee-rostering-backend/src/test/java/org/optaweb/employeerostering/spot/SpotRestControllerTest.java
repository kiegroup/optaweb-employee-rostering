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

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureTestDatabase
public class SpotRestControllerTest extends AbstractEntityRequireTenantRestServiceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String spotPathURI = "http://localhost:8080/rest/tenant/{tenantId}/spot/";

    private ResponseEntity<List<Spot>> getSpots(Integer tenantId) {
        return restTemplate.exchange(spotPathURI, HttpMethod.GET, null,
                                     new ParameterizedTypeReference<List<Spot>>() {
                                     }, tenantId);
    }

    private ResponseEntity<Spot> getSpot(Integer tenantId, Long id) {
        return restTemplate.getForEntity(spotPathURI + id, Spot.class, tenantId);
    }

    private void deleteSpot(Integer tenantId, Long id) {
        restTemplate.delete(spotPathURI + id, tenantId);
    }

    private ResponseEntity<Spot> addSpot(Integer tenantId, SpotView spotView) {
        return restTemplate.postForEntity(spotPathURI + "add", spotView, Spot.class, tenantId);
    }

    private ResponseEntity<Spot> updateSpot(Integer tenantId, SpotView spotView) {
        return restTemplate.postForEntity(spotPathURI + "update", spotView, Spot.class, tenantId);
    }

    @Before
    public void setup() {
        createTestTenant();
    }

    @After
    public void cleanup() {
        deleteTestTenant();
    }

    @Test
    public void spotCrudTest() {
        SpotView spotView = new SpotView(TENANT_ID, "spot", Collections.emptySet(), false);
        ResponseEntity<Spot> postResponse = addSpot(TENANT_ID, spotView);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Spot> response = getSpot(TENANT_ID, postResponse.getBody().getId());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualToComparingFieldByFieldRecursively(postResponse.getBody());

        SpotView updatedSpot = new SpotView(TENANT_ID, "updatedSpot", Collections.emptySet(), false);
        updatedSpot.setId(postResponse.getBody().getId());
        ResponseEntity<Spot> putResponse = updateSpot(TENANT_ID, updatedSpot);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = getSpot(TENANT_ID, putResponse.getBody().getId());
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isEqualToComparingFieldByFieldRecursively(response.getBody());

        deleteSpot(TENANT_ID, postResponse.getBody().getId());

        ResponseEntity<List<Spot>> getListResponse = getSpots(TENANT_ID);
        assertThat(getListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getListResponse.getBody()).isEmpty();
    }
}
