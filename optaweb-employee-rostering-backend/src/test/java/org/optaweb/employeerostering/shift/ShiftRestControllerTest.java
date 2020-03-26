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

package org.optaweb.employeerostering.shift;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.employee.CovidRiskType;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;
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
public class ShiftRestControllerTest extends AbstractEntityRequireTenantRestServiceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String shiftPathURI = "http://localhost:8080/rest/tenant/{tenantId}/shift/";
    private final String employeePathURI = "http://localhost:8080/rest/tenant/{tenantId}/employee/";
    private final String contractPathURI = "http://localhost:8080/rest/tenant/{tenantId}/contract/";
    private final String spotPathURI = "http://localhost:8080/rest/tenant/{tenantId}/spot/";

    private ResponseEntity<List<ShiftView>> getShifts(Integer tenantId) {
        return restTemplate.exchange(shiftPathURI, HttpMethod.GET, null,
                                     new ParameterizedTypeReference<List<ShiftView>>() {
                                     }, tenantId);
    }

    private ResponseEntity<ShiftView> getShift(Integer tenantId, Long id) {
        return restTemplate.getForEntity(shiftPathURI + id, ShiftView.class, tenantId);
    }

    private void deleteShift(Integer tenantId, Long id) {
        restTemplate.delete(shiftPathURI + id, tenantId);
    }

    private ResponseEntity<ShiftView> addShift(Integer tenantId, ShiftView shiftView) {
        return restTemplate.postForEntity(shiftPathURI + "add", shiftView, ShiftView.class, tenantId);
    }

    private ResponseEntity<ShiftView> updateShift(Integer tenantId, HttpEntity<ShiftView> request) {
        return restTemplate.exchange(shiftPathURI + "update", HttpMethod.PUT, request, ShiftView.class, tenantId);
    }

    private ResponseEntity<Employee> addEmployee(Integer tenantId, Employee employee) {
        return restTemplate.postForEntity(employeePathURI + "add", employee, Employee.class, tenantId);
    }

    private ResponseEntity<Contract> addContract(Integer tenantId, Contract contract) {
        return restTemplate.postForEntity(contractPathURI + "add", contract, Contract.class, tenantId);
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
    public void shiftCrudTest() {
        ResponseEntity<Spot> spotResponseEntity = addSpot(TENANT_ID, new SpotView(TENANT_ID, "spot",
                                                                                  Collections.emptySet(), false));
        Spot spot = spotResponseEntity.getBody();

        ResponseEntity<Contract> contractResponseEntity = addContract(TENANT_ID, new Contract(TENANT_ID, "contract"));
        Contract contract = contractResponseEntity.getBody();

        ResponseEntity<Employee> rotationEmployeeResponseEntity = addEmployee(TENANT_ID,
                                                                              new Employee(TENANT_ID,
                                                                                           "rotationEmployee", contract,
                                                                                           Collections.emptySet(),
                                                                                           CovidRiskType.INOCULATED));
        Employee rotationEmployee = rotationEmployeeResponseEntity.getBody();

        LocalDateTime startDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(8);
        ShiftView shiftView = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime, rotationEmployee);
        ResponseEntity<ShiftView> postResponse = addShift(TENANT_ID, shiftView);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<ShiftView> getResponse = getShift(TENANT_ID, postResponse.getBody().getId());
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isEqualToComparingFieldByFieldRecursively(postResponse.getBody());

        ShiftView updatedShiftView = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime);
        updatedShiftView.setId(postResponse.getBody().getId());
        HttpEntity<ShiftView> request = new HttpEntity<>(updatedShiftView);
        ResponseEntity<ShiftView> putResponse = updateShift(TENANT_ID, request);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        getResponse = getShift(TENANT_ID, putResponse.getBody().getId());
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isEqualToComparingFieldByFieldRecursively(getResponse.getBody());

        deleteShift(TENANT_ID, putResponse.getBody().getId());

        ResponseEntity<List<ShiftView>> getShiftListResponse = getShifts(TENANT_ID);
        assertThat(getShiftListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getShiftListResponse.getBody()).isEmpty();
    }
}
