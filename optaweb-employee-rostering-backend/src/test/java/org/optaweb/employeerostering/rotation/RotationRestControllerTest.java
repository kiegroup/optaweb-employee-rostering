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

package org.optaweb.employeerostering.rotation;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.rotation.TimeBucket;
import org.optaweb.employeerostering.domain.rotation.view.TimeBucketView;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureTestDatabase
public class RotationRestControllerTest extends AbstractEntityRequireTenantRestServiceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String timeBucketPathURI = "http://localhost:8080/rest/tenant/{tenantId}/rotation/";
    private final String spotPathURI = "http://localhost:8080/rest/tenant/{tenantId}/spot/";

    private ResponseEntity<List<TimeBucketView>> getTimeBuckets(Integer tenantId) {
        return restTemplate.exchange(timeBucketPathURI, HttpMethod.GET, null,
                                     new ParameterizedTypeReference<List<TimeBucketView>>() {
                                     }, tenantId);
    }

    private ResponseEntity<TimeBucketView> getTimeBucket(Integer tenantId, Long id) {
        return restTemplate.getForEntity(timeBucketPathURI + id, TimeBucketView.class, tenantId);
    }

    private void deleteTimeBucket(Integer tenantId, Long id) {
        restTemplate.delete(timeBucketPathURI + id, tenantId);
    }

    private ResponseEntity<TimeBucketView> addTimeBucket(Integer tenantId, TimeBucketView shiftTemplateView) {
        return restTemplate.postForEntity(timeBucketPathURI + "add", shiftTemplateView, TimeBucketView.class,
                                          tenantId);
    }

    private ResponseEntity<TimeBucketView> updateTimeBucket(Integer tenantId,
                                                                  HttpEntity<TimeBucketView> request) {
        return restTemplate.exchange(timeBucketPathURI + "update", HttpMethod.PUT, request,
                                     TimeBucketView.class, tenantId);
    }

    private ResponseEntity<Spot> addSpot(Integer tenantId, SpotView spotView) {
        return restTemplate.postForEntity(spotPathURI + "add", spotView, Spot.class, tenantId);
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
    public void shiftTemplateCrudTest() {
        ResponseEntity<Spot> spotResponseA = addSpot(TENANT_ID, new SpotView(TENANT_ID, "A",
                                                                             Collections.emptySet()));
        Spot spotA = spotResponseA.getBody();

        TimeBucketView timeBucketView = new TimeBucketView(new TimeBucket(TENANT_ID, spotA, LocalTime.of(9, 0),
                                                                          LocalTime.of(17, 0),
                                                                          Collections.emptySet(),
                                                                          Collections.emptySet(),
                                                                          Collections.emptyList()));
        ResponseEntity<TimeBucketView> postResponse = addTimeBucket(TENANT_ID, timeBucketView);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<TimeBucketView> response = getTimeBucket(TENANT_ID, postResponse.getBody().getId());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(postResponse.getBody());

        TimeBucketView updatedTimeBucket = new TimeBucketView(new TimeBucket(TENANT_ID, spotA, LocalTime.of(9, 0),
                                                                                LocalTime.of(17, 0),
                                                                                Collections.emptySet(),
                                                                                Collections.emptySet(),
                                                                                Collections.emptyList()));
        updatedTimeBucket.setId(postResponse.getBody().getId());
        HttpEntity<TimeBucketView> request = new HttpEntity<>(updatedTimeBucket);
        ResponseEntity<TimeBucketView> putResponse = updateTimeBucket(TENANT_ID, request);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = getTimeBucket(TENANT_ID, putResponse.getBody().getId());
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isEqualTo(response.getBody());

        deleteTimeBucket(TENANT_ID, putResponse.getBody().getId());

        ResponseEntity<List<TimeBucketView>> getListResponse = getTimeBuckets(TENANT_ID);
        assertThat(getListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getListResponse.getBody()).isEmpty();
    }
}
