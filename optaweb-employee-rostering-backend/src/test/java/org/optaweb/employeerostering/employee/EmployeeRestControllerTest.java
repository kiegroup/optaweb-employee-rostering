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

package org.optaweb.employeerostering.employee;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.employee.CovidRiskType;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.domain.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.domain.skill.Skill;
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
public class EmployeeRestControllerTest extends AbstractEntityRequireTenantRestServiceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String employeePathURI = "http://localhost:8080/rest/tenant/{tenantId}/employee/";
    private final String contractPathURI = "http://localhost:8080/rest/tenant/{tenantId}/contract/";
    private final String skillPathURI = "http://localhost:8080/rest/tenant/{tenantId}/skill/";
    private final String employeeAvailabilityPathURI =
            "http://localhost:8080/rest/tenant/{tenantId}/employee/availability/";

    private ResponseEntity<List<Employee>> getEmployees(Integer tenantId) {
        return restTemplate.exchange(employeePathURI, HttpMethod.GET, null,
                                     new ParameterizedTypeReference<List<Employee>>() {
                                     }, tenantId);
    }

    private ResponseEntity<Employee> getEmployee(Integer tenantId, Long id) {
        return restTemplate.getForEntity(employeePathURI + id, Employee.class, tenantId);
    }

    private void deleteEmployee(Integer tenantId, Long id) {
        restTemplate.delete(employeePathURI + id, tenantId);
    }

    private ResponseEntity<Employee> addEmployee(Integer tenantId, Employee employee) {
        return restTemplate.postForEntity(employeePathURI + "add", employee, Employee.class, tenantId);
    }

    private ResponseEntity<Employee> updateEmployee(Integer tenantId, Employee employee) {
        return restTemplate.postForEntity(employeePathURI + "update", employee, Employee.class, tenantId);
    }

    private ResponseEntity<Skill> addSkill(Integer tenantId, Skill skill) {
        return restTemplate.postForEntity(skillPathURI + "add", skill, Skill.class, tenantId);
    }

    private ResponseEntity<Contract> addContract(Integer tenantId, Contract contract) {
        return restTemplate.postForEntity(contractPathURI + "add", contract, Contract.class, tenantId);
    }

    private ResponseEntity<EmployeeAvailabilityView> getEmployeeAvailability(Integer tenantId, Long id) {
        return restTemplate.getForEntity(employeeAvailabilityPathURI + id, EmployeeAvailabilityView.class, tenantId);
    }

    private void deleteEmployeeAvailability(Integer tenantId, Long id) {
        restTemplate.delete(employeeAvailabilityPathURI + id, tenantId);
    }

    private ResponseEntity<EmployeeAvailabilityView> addEmployeeAvailability(Integer tenantId, EmployeeAvailabilityView
            employeeAvailabilityView) {
        return restTemplate.postForEntity(employeeAvailabilityPathURI + "add", employeeAvailabilityView,
                                          EmployeeAvailabilityView.class, tenantId);
    }

    private ResponseEntity<EmployeeAvailabilityView> updateEmployeeAvailability(Integer tenantId,
                                                                                HttpEntity<EmployeeAvailabilityView>
                                                                                        request) {
        return restTemplate.exchange(employeeAvailabilityPathURI + "update", HttpMethod.PUT, request,
                                     EmployeeAvailabilityView.class, tenantId);
    }

    @Before
    public void setup() {
        createTestTenant();
    }

    @After
    public void cleanup() {
        deleteTestTenant();
    }

    // ************************************************************************
    // Employee
    // ************************************************************************

    @Test
    public void employeeCrudTest() {
        ResponseEntity<Skill> skillResponseA = addSkill(TENANT_ID, new Skill(TENANT_ID, "A"));
        ResponseEntity<Skill> skillResponseB = addSkill(TENANT_ID, new Skill(TENANT_ID, "B"));

        Skill skillA = skillResponseA.getBody();
        Skill skillB = skillResponseB.getBody();

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        ResponseEntity<Contract> contractResponseEntity = addContract(TENANT_ID, new Contract(TENANT_ID, "A"));
        Contract contractA = contractResponseEntity.getBody();

        Employee employee = new Employee(TENANT_ID, "employee", contractA, testSkillSet, CovidRiskType.INOCULATED);
        ResponseEntity<Employee> postResponse = addEmployee(TENANT_ID, employee);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Employee> response = getEmployee(TENANT_ID, postResponse.getBody().getId());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualToComparingFieldByFieldRecursively(postResponse.getBody());

        Employee updatedEmployee = new Employee(TENANT_ID, "updatedEmployee", contractA,
                                                testSkillSet, CovidRiskType.INOCULATED);
        updatedEmployee.setId(postResponse.getBody().getId());
        ResponseEntity<Employee> putResponse = updateEmployee(TENANT_ID, updatedEmployee);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = getEmployee(TENANT_ID, putResponse.getBody().getId());
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isEqualToComparingFieldByFieldRecursively(response.getBody());

        deleteEmployee(TENANT_ID, putResponse.getBody().getId());

        ResponseEntity<List<Employee>> getListResponse = getEmployees(TENANT_ID);
        assertThat(getListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getListResponse.getBody()).isEmpty();
    }

    // ************************************************************************
    // EmployeeAvailability
    // ************************************************************************

    @Test
    public void employeeAvailabilityCrudTest() {
        ResponseEntity<Contract> contractResponseEntity = addContract(TENANT_ID, new Contract(TENANT_ID, "contract"));
        Contract contract = contractResponseEntity.getBody();

        ResponseEntity<Employee> employeeResponseEntity = addEmployee(TENANT_ID,
                                                                      new Employee(TENANT_ID, "employee",
                                                                                   contract,
                                                                                   Collections.emptySet(),
                                                                                   CovidRiskType.INOCULATED));
        Employee employee = employeeResponseEntity.getBody();

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        ResponseEntity<EmployeeAvailabilityView> postResponse =
                addEmployeeAvailability(TENANT_ID, new EmployeeAvailabilityView(TENANT_ID, employee,
                                                                                startDateTime,
                                                                                endDateTime,
                                                                                EmployeeAvailabilityState.UNAVAILABLE));
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<EmployeeAvailabilityView> getResponse = getEmployeeAvailability(TENANT_ID,
                                                                                       postResponse.getBody().getId());
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isEqualToComparingFieldByFieldRecursively(postResponse.getBody());

        EmployeeAvailabilityView newEmployeeAvailabilityView =
                new EmployeeAvailabilityView(TENANT_ID, employee,
                                             startDateTime, endDateTime,
                                             EmployeeAvailabilityState.DESIRED);
        newEmployeeAvailabilityView.setId(postResponse.getBody().getId());
        HttpEntity<EmployeeAvailabilityView> request = new HttpEntity<>(newEmployeeAvailabilityView);
        ResponseEntity<EmployeeAvailabilityView> putResponse = updateEmployeeAvailability(TENANT_ID, request);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        getResponse = getEmployeeAvailability(TENANT_ID, putResponse.getBody().getId());
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isEqualToComparingFieldByFieldRecursively(putResponse.getBody());

        deleteEmployeeAvailability(TENANT_ID, putResponse.getBody().getId());
    }
}
