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

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.rotation.view.ShiftTemplateView;
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

    private final String shiftTemplatePathURI = "http://localhost:8080/rest/tenant/{tenantId}/rotation/";
    private final String spotPathURI = "http://localhost:8080/rest/tenant/{tenantId}/spot/";

    private ResponseEntity<List<ShiftTemplateView>> getShiftTemplates(Integer tenantId) {
        return restTemplate.exchange(shiftTemplatePathURI, HttpMethod.GET, null,
                                     new ParameterizedTypeReference<List<ShiftTemplateView>>() {
                                     }, tenantId);
    }

    private ResponseEntity<ShiftTemplateView> getShiftTemplate(Integer tenantId, Long id) {
        return restTemplate.getForEntity(shiftTemplatePathURI + id, ShiftTemplateView.class, tenantId);
    }

    private void deleteShiftTemplate(Integer tenantId, Long id) {
        restTemplate.delete(shiftTemplatePathURI + id, tenantId);
    }

    private ResponseEntity<ShiftTemplateView> addShiftTemplate(Integer tenantId, ShiftTemplateView shiftTemplateView) {
        return restTemplate.postForEntity(shiftTemplatePathURI + "add", shiftTemplateView, ShiftTemplateView.class,
                                          tenantId);
    }

    private ResponseEntity<ShiftTemplateView> updateShiftTemplate(Integer tenantId,
                                                                  HttpEntity<ShiftTemplateView> request) {
        return restTemplate.exchange(shiftTemplatePathURI + "update", HttpMethod.PUT, request,
                                     ShiftTemplateView.class, tenantId);
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
                                                                             Collections.emptySet(), false));
        Spot spotA = spotResponseA.getBody();

        ShiftTemplateView shiftTemplateView = new ShiftTemplateView(TENANT_ID, spotA.getId(), Duration.ofDays(0),
                                                                    Duration.ofDays(0), null, Collections.emptyList());
        ResponseEntity<ShiftTemplateView> postResponse = addShiftTemplate(TENANT_ID, shiftTemplateView);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<ShiftTemplateView> response = getShiftTemplate(TENANT_ID, postResponse.getBody().getId());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualToComparingFieldByFieldRecursively(postResponse.getBody());

        ShiftTemplateView updatedShiftTemplate = new ShiftTemplateView(TENANT_ID, spotA.getId(), Duration.ofDays(1),
                                                                       Duration.ofDays(1), null,
                                                                       Collections.emptyList());
        updatedShiftTemplate.setId(postResponse.getBody().getId());
        HttpEntity<ShiftTemplateView> request = new HttpEntity<>(updatedShiftTemplate);
        ResponseEntity<ShiftTemplateView> putResponse = updateShiftTemplate(TENANT_ID, request);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = getShiftTemplate(TENANT_ID, putResponse.getBody().getId());
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isEqualTo(response.getBody());

        deleteShiftTemplate(TENANT_ID, putResponse.getBody().getId());

        ResponseEntity<List<ShiftTemplateView>> getListResponse = getShiftTemplates(TENANT_ID);
        assertThat(getListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getListResponse.getBody()).isEmpty();
    }
}
