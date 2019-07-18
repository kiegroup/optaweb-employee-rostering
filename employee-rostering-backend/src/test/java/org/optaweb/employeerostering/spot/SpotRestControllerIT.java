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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SpotRestControllerIT {

    @Autowired
    private TestRestTemplate spotRestTemplate;

    private String spotPathURI = "http://localhost:8080/rest/tenant/{tenantId}/spot/";

    @Test
    public void getSpotListTest() {
        Integer tenantId = 1;
        Integer tenantId2 = 2;
        String name = "name";
        String name2 = "name2";

        Spot spot = new Spot(tenantId, name, Collections.emptySet());
        Spot spot2 = new Spot(tenantId, name2, Collections.emptySet());
        Spot spot3 = new Spot(tenantId2, name, Collections.emptySet());

        ResponseEntity<Spot> postResponse = spotRestTemplate.postForEntity(spotPathURI + "add", spot, Spot.class
                , tenantId);
        ResponseEntity<Spot> postResponse2 = spotRestTemplate.postForEntity(spotPathURI + "add", spot2, Spot.class
                , tenantId);
        ResponseEntity<Spot> postResponse3 = spotRestTemplate.postForEntity(spotPathURI + "add", spot3, Spot.class
                , tenantId2);

        ResponseEntity<List<Spot>> response = spotRestTemplate.exchange(spotPathURI, HttpMethod.GET, null,
                                                                          new ParameterizedTypeReference
                                                                                  <List<Spot>>() {}, tenantId);
        ResponseEntity<List<Spot>> response2 = spotRestTemplate.exchange(spotPathURI, HttpMethod.GET, null,
                                                                           new ParameterizedTypeReference
                                                                                   <List<Spot>>() {}, tenantId2);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(postResponse.getBody());
        assertThat(response.getBody()).contains(postResponse2.getBody());

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).contains(postResponse3.getBody());

        spotRestTemplate.delete(spotPathURI + postResponse.getBody().getId(), tenantId);
        spotRestTemplate.delete(spotPathURI + postResponse2.getBody().getId(), tenantId);
        spotRestTemplate.delete(spotPathURI + postResponse3.getBody().getId(), tenantId2);
    }

    @Test
    public void getSpotTest() {
        Integer tenantId = 1;
        String name = "name";

        Spot spot = new Spot(tenantId, name, Collections.emptySet());

        ResponseEntity<Spot> postResponse = spotRestTemplate.postForEntity(spotPathURI + "add", spot, Spot.class
                , tenantId);

        ResponseEntity<Spot> response = spotRestTemplate.getForEntity(spotPathURI + postResponse.getBody().getId(),
                                                                        Spot.class, tenantId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(postResponse.getBody());

        spotRestTemplate.delete(spotPathURI + postResponse.getBody().getId(), tenantId);
    }

    @Test
    public void deleteSpotTest() {
        Integer tenantId = 1;
        String name = "name";

        Spot spot = new Spot(tenantId, name, Collections.emptySet());

        ResponseEntity<Spot> postResponse = spotRestTemplate.postForEntity(spotPathURI + "add", spot, Spot.class
                , tenantId);

        spotRestTemplate.delete(spotPathURI + postResponse.getBody().getId(), tenantId);

        ResponseEntity<List<Spot>> response = spotRestTemplate.exchange(spotPathURI, HttpMethod.GET, null,
                                                                          new ParameterizedTypeReference
                                                                                  <List<Spot>>() {}, tenantId);

        assertThat(response.getBody()).isEmpty();
    }

    @Test
    public void createSpotTest() {
        Integer tenantId = 1;
        String name = "name";

        Spot spot = new Spot(tenantId, name, Collections.emptySet());

        ResponseEntity<Spot> postResponse = spotRestTemplate.postForEntity(spotPathURI + "add", spot, Spot.class
                , tenantId);

        ResponseEntity<Spot> response = spotRestTemplate.getForEntity(spotPathURI + postResponse.getBody().getId(),
                                                                        Spot.class, tenantId);

        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(postResponse.getBody()).isEqualTo(response.getBody());

        spotRestTemplate.delete(spotPathURI + postResponse.getBody().getId(), tenantId);
    }

    @Test
    public void updateSpotTest() {
        Integer tenantId = 1;
        String name = "name";

        Spot spot = new Spot(tenantId, name, Collections.emptySet());

        ResponseEntity<Spot> postResponse = spotRestTemplate.postForEntity(spotPathURI + "add", spot, Spot.class
                , tenantId);

        Spot spot2 = new Spot(tenantId, "name2", Collections.emptySet());
        spot2.setId(postResponse.getBody().getId());
        HttpEntity<Spot> request = new HttpEntity<>(spot2);

        ResponseEntity<Spot> putResponse = spotRestTemplate.exchange(spotPathURI + "update", HttpMethod.PUT,
                                                                       request, Spot.class, tenantId);

        ResponseEntity<Spot> response = spotRestTemplate.getForEntity(spotPathURI + putResponse.getBody().getId(),
                                                                        Spot.class, tenantId);

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isEqualTo(response.getBody());

        spotRestTemplate.delete(spotPathURI + putResponse.getBody().getId(), tenantId);
    }
}
