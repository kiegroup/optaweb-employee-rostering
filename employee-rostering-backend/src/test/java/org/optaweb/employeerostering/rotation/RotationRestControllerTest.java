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

import java.util.List;

import org.optaweb.employeerostering.domain.rotation.view.ShiftTemplateView;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RotationRestControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String shiftTemplatePathURI = "http://localhost:8080/rest/tenant/{tenantId}/rotation/";
    private String spotPathURI = "http://localhost:8080/rest/tenant/{tenantId}/spot/";

    private ResponseEntity<List<ShiftTemplateView>> getShiftTemplates(Integer tenantId) {
        return restTemplate.exchange(shiftTemplatePathURI, HttpMethod.GET, null,
                                     new ParameterizedTypeReference<List<ShiftTemplateView>>() {}, tenantId);
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

    private void deleteSpot(Integer tenantId, Long id) {
        restTemplate.delete(spotPathURI + id, tenantId);
    }

    // TODO: Add getShiftTemplateListTest when createRosterState() is implemented in RosterGenerator;
    //  getShiftTemplates() requires persisted RosterState entity

    // TODO: Add getShiftTemplateTest when createRosterState() is implemented in RosterGenerator;
    //  getShiftTemplate() requires persisted RosterState entity

    // TODO: Add deleteShiftTemplateTest when createRosterState() is implemented in RosterGenerator;
    //  deleteShiftTemplate() requires persisted RosterState entity

    // TODO: Add addShiftTemplateTest when createRosterState() is implemented in RosterGenerator;
    //  addShiftTemplate() requires persisted RosterState entity

    // TODO: Add updateShiftTemplateTest when createRosterState() is implemented in RosterGenerator;
    //  updateShiftTemplate() requires persisted RosterState entity

    /*@Test
    public void addShiftTemplateTest() {
        Integer tenantId = 1;

        ResponseEntity<Spot> spotResponseA = addSpot(tenantId, new Spot(tenantId, "A", Collections.emptySet()));
        Spot spotA = spotResponseA.getBody();

        ShiftTemplateView shiftTemplateView = new ShiftTemplateView(tenantId, spotA.getId(), null, null, null);
        ResponseEntity<ShiftTemplateView> postResponse = addShiftTemplate(tenantId, shiftTemplateView);

        ResponseEntity<ShiftTemplateView> response = getShiftTemplate(tenantId, postResponse.getBody().getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(postResponse.getBody());

        deleteShiftTemplate(tenantId, postResponse.getBody().getId());
        deleteSpot(tenantId, spotA.getId());
    }*/
}
