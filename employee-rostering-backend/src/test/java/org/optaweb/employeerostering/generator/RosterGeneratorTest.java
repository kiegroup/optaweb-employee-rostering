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

package org.optaweb.employeerostering.generator;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.rotation.view.ShiftTemplateView;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.springframework.beans.factory.annotation.Autowired;
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
public class RosterGeneratorTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String skillPathURI = "http://localhost:8080/rest/tenant/{tenantId}/skill/";
    private final String spotPathURI = "http://localhost:8080/rest/tenant/{tenantId}/spot/";
    private final String contractPathURI = "http://localhost:8080/rest/tenant/{tenantId}/contract/";
    private final String employeePathURI = "http://localhost:8080/rest/tenant/{tenantId}/employee/";
    private final String rotationPathURI = "http://localhost:8080/rest/tenant/{tenantId}/rotation/";
    private final String tenantPathURI = "http://localhost:8080/rest/tenant/";
    private final String shiftPathURI = "http://localhost:8080/rest/tenant/{tenantId}/shift/";

    private ResponseEntity<List<Skill>> getSkills(Integer tenantId) {
        return restTemplate.exchange(skillPathURI, HttpMethod.GET, null,
                                     new ParameterizedTypeReference<List<Skill>>() {}, tenantId);
    }

    private ResponseEntity<List<Spot>> getSpots(Integer tenantId) {
        return restTemplate.exchange(spotPathURI, HttpMethod.GET, null,
                                     new ParameterizedTypeReference<List<Spot>>() {}, tenantId);
    }

    private ResponseEntity<List<Contract>> getContracts(Integer tenantId) {
        return restTemplate.exchange(contractPathURI, HttpMethod.GET, null,
                                     new ParameterizedTypeReference<List<Contract>>() {}, tenantId);
    }

    private ResponseEntity<List<Employee>> getEmployees(Integer tenantId) {
        return restTemplate.exchange(employeePathURI, HttpMethod.GET, null,
                                     new ParameterizedTypeReference<List<Employee>>() {}, tenantId);
    }

    private ResponseEntity<List<ShiftTemplateView>> getShiftTemplates(Integer tenantId) {
        return restTemplate.exchange(rotationPathURI, HttpMethod.GET, null,
                                     new ParameterizedTypeReference<List<ShiftTemplateView>>() {}, tenantId);
    }

    private ResponseEntity<List<ShiftView>> getShifts(Integer tenantId) {
        return restTemplate.exchange(shiftPathURI, HttpMethod.GET, null,
                                     new ParameterizedTypeReference<List<ShiftView>>() {}, tenantId);
    }

    private ResponseEntity<List<Tenant>> getTenants() {
        return restTemplate.exchange(tenantPathURI, HttpMethod.GET, null,
                                     new ParameterizedTypeReference<List<Tenant>>() {});
    }

    @Test
    public void generateSkillListTest() {
        ResponseEntity<List<Skill>> response = getSkills(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Ambulatory care");
        assertThat(response.getBody().get(1).getName()).isEqualTo("Critical care");
    }

    @Test
    public void generateSpotListTest() {
        ResponseEntity<List<Spot>> response = getSpots(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Anaesthetics");
        assertThat(response.getBody().get(1).getName()).isEqualTo("Cardiology");
        assertThat(response.getBody().get(2).getName()).isEqualTo("Critical care");
        assertThat(response.getBody().get(3).getName()).isEqualTo("Ear nose throat");
        assertThat(response.getBody().get(4).getName()).isEqualTo("Emergency");
        assertThat(response.getBody().get(5).getName()).isEqualTo("Gastroenterology");
        assertThat(response.getBody().get(6).getName()).isEqualTo("Haematology");
        assertThat(response.getBody().get(7).getName()).isEqualTo("Maternity");
        assertThat(response.getBody().get(8).getName()).isEqualTo("Neurology");
        assertThat(response.getBody().get(9).getName()).isEqualTo("Oncology");
    }

    @Test
    public void generateContractListTest() {
        ResponseEntity<List<Contract>> response = getContracts(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Max 16 Hours Per Week Contract");
        assertThat(response.getBody().get(0).getMaximumMinutesPerWeek()).isEqualTo(960);
        assertThat(response.getBody().get(1).getName()).isEqualTo("Max 16 Hours Per Week, 32 Hours Per Month Contract");
        assertThat(response.getBody().get(1).getMaximumMinutesPerMonth()).isEqualTo(1920);
        assertThat(response.getBody().get(2).getName()).isEqualTo("Part Time Contract");
        assertThat(response.getBody().get(2).getMaximumMinutesPerYear()).isEqualTo(null);
    }

    @Test
    public void generateEmployeeListTest() {
        ResponseEntity<List<Employee>> response = getEmployees(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Amy Cole");
        assertThat(response.getBody().get(1).getName()).isEqualTo("Amy Fox");
        assertThat(response.getBody().get(2).getName()).isEqualTo("Amy Green");
    }

    @Test
    public void generateShiftTemplateListTest() {
        ResponseEntity<List<ShiftTemplateView>> response = getShiftTemplates(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get(0).getShiftTemplateDuration().toString()).isEqualTo("PT8H");
        assertThat(response.getBody().get(0).getDurationBetweenRotationStartAndTemplateStart().toString()).isEqualTo(
                "PT6H");
    }

    @Test
    public void generateShiftListTest() {
        ResponseEntity<List<ShiftView>> response = getShifts(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void generateTenantListTest() {
        ResponseEntity<List<Tenant>> response = getTenants();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Hospital Los Angeles (98 employees)");
    }
}
