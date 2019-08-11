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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.domain.skill.Skill;
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
public class EmployeeRestControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private String employeePathURI = "http://localhost:8080/rest/tenant/{tenantId}/employee/";
    private String contractPathURI = "http://localhost:8080/rest/tenant/{tenantId}/contract/";
    private String skillPathURI = "http://localhost:8080/rest/tenant/{tenantId}/skill/";
    private String employeeAvailabilityPathURI = "http://localhost:8080/rest/tenant/{tenantId}/employee/availability/";

    private ResponseEntity<List<Employee>> getEmployees(Integer tenantId) {
        return restTemplate.exchange(employeePathURI, HttpMethod.GET, null,
                                     new ParameterizedTypeReference<List<Employee>>() {}, tenantId);
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

    private ResponseEntity<Employee> updateEmployee(Integer tenantId, HttpEntity<Employee> request) {
        return restTemplate.exchange(employeePathURI + "update", HttpMethod.PUT, request, Employee.class, tenantId);
    }

    private ResponseEntity<Skill> addSkill(Integer tenantId, Skill skill) {
        return restTemplate.postForEntity(skillPathURI + "add", skill, Skill.class, tenantId);
    }

    private void deleteSkill(Integer tenantId, Long id) {
        restTemplate.delete(skillPathURI + id, tenantId);
    }

    private ResponseEntity<Contract> addContract(Integer tenantId, Contract contract) {
        return restTemplate.postForEntity(contractPathURI + "add", contract, Contract.class, tenantId);
    }

    private void deleteContract(Integer tenantId, Long id) {
        restTemplate.delete(contractPathURI + id, tenantId);
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
        return restTemplate.exchange(employeePathURI + "update", HttpMethod.PUT, request,
                                     EmployeeAvailabilityView.class, tenantId);
    }

    @Test
    public void getEmployeeListTest() {
        Integer tenantId = 2;
        Integer tenantId2 = 3;
        String name = "name";
        String name2 = "name2";

        ResponseEntity<Skill> skillResponseA = addSkill(tenantId, new Skill(tenantId, "A"));
        ResponseEntity<Skill> skillResponseB = addSkill(tenantId, new Skill(tenantId, "B"));
        ResponseEntity<Skill> skillResponseC = addSkill(tenantId2, new Skill(tenantId2, "C"));

        Skill skillA = skillResponseA.getBody();
        Skill skillB = skillResponseB.getBody();
        Skill skillC = skillResponseC.getBody();

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Set<Skill> testSkillSet2 = new HashSet<>();
        testSkillSet2.add(skillC);

        ResponseEntity<Contract> contractResponseA = addContract(tenantId, new Contract(tenantId, "A"));
        ResponseEntity<Contract> contractResponseB = addContract(tenantId2, new Contract(tenantId2, "B"));

        Contract contractA = contractResponseA.getBody();
        Contract contractB = contractResponseB.getBody();

        Employee employee = new Employee(tenantId, name, contractA, testSkillSet);
        Employee employee2 = new Employee(tenantId, name2, contractA, testSkillSet);
        Employee employee3 = new Employee(tenantId2, name, contractB, testSkillSet2);

        ResponseEntity<Employee> postResponse = addEmployee(tenantId, employee);
        ResponseEntity<Employee> postResponse2 = addEmployee(tenantId, employee2);
        ResponseEntity<Employee> postResponse3 = addEmployee(tenantId2, employee3);

        ResponseEntity<List<Employee>> response = getEmployees(tenantId);
        ResponseEntity<List<Employee>> response2 = getEmployees(tenantId2);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(postResponse.getBody());
        assertThat(response.getBody()).contains(postResponse2.getBody());

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).contains(postResponse3.getBody());

        deleteEmployee(tenantId, postResponse.getBody().getId());
        deleteEmployee(tenantId, postResponse2.getBody().getId());
        deleteEmployee(tenantId2, postResponse3.getBody().getId());

        deleteSkill(tenantId, skillA.getId());
        deleteSkill(tenantId, skillB.getId());
        deleteSkill(tenantId2, skillC.getId());

        deleteContract(tenantId, contractA.getId());
        deleteContract(tenantId2, contractB.getId());
    }

    @Test
    public void getAndCreateEmployeeTest() {
        Integer tenantId = 2;
        String name = "name";

        ResponseEntity<Skill> skillResponseA = addSkill(tenantId, new Skill(tenantId, "A"));
        ResponseEntity<Skill> skillResponseB = addSkill(tenantId, new Skill(tenantId, "B"));

        Skill skillA = skillResponseA.getBody();
        Skill skillB = skillResponseB.getBody();

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        ResponseEntity<Contract> contractResponseEntity = addContract(tenantId, new Contract(tenantId, "A"));
        Contract contractA = contractResponseEntity.getBody();

        Employee employee = new Employee(tenantId, name, contractA, testSkillSet);
        ResponseEntity<Employee> postResponse = addEmployee(tenantId, employee);

        ResponseEntity<Employee> response = getEmployee(tenantId, postResponse.getBody().getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(postResponse.getBody());

        deleteEmployee(tenantId, postResponse.getBody().getId());
        deleteSkill(tenantId, skillA.getId());
        deleteSkill(tenantId, skillB.getId());
        deleteContract(tenantId, contractA.getId());
    }

    @Test
    public void deleteEmployeeTest() {
        Integer tenantId = 2;
        String name = "name";

        ResponseEntity<Skill> skillResponseA = addSkill(tenantId, new Skill(tenantId, "A"));
        ResponseEntity<Skill> skillResponseB = addSkill(tenantId, new Skill(tenantId, "B"));

        Skill skillA = skillResponseA.getBody();
        Skill skillB = skillResponseB.getBody();

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        ResponseEntity<Contract> contractResponseEntity = addContract(tenantId, new Contract(tenantId, "A"));
        Contract contractA = contractResponseEntity.getBody();

        Employee employee = new Employee(tenantId, name, contractA, testSkillSet);
        ResponseEntity<Employee> postResponse = addEmployee(tenantId, employee);

        deleteEmployee(tenantId, postResponse.getBody().getId());

        ResponseEntity<List<Employee>> response = getEmployees(tenantId);

        assertThat(response.getBody()).isEmpty();

        deleteSkill(tenantId, skillA.getId());
        deleteSkill(tenantId, skillB.getId());
        deleteContract(tenantId, contractA.getId());
    }

    @Test
    public void updateEmployeeTest() {
        Integer tenantId = 2;
        String name = "name";

        ResponseEntity<Skill> skillResponseA = addSkill(tenantId, new Skill(tenantId, "A"));
        ResponseEntity<Skill> skillResponseB = addSkill(tenantId, new Skill(tenantId, "B"));

        Skill skillA = skillResponseA.getBody();
        Skill skillB = skillResponseB.getBody();

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        ResponseEntity<Contract> contractResponseEntity = addContract(tenantId, new Contract(tenantId, "A"));
        Contract contractA = contractResponseEntity.getBody();

        Employee employee = new Employee(tenantId, name, contractA, Collections.emptySet());

        ResponseEntity<Employee> postResponse = addEmployee(tenantId, employee);

        Employee employee2 = new Employee(tenantId, "name2", contractA, testSkillSet);
        employee2.setId(postResponse.getBody().getId());
        HttpEntity<Employee> request = new HttpEntity<>(employee2);

        ResponseEntity<Employee> putResponse = updateEmployee(tenantId, request);

        ResponseEntity<Employee> response = getEmployee(tenantId, putResponse.getBody().getId());

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isEqualTo(response.getBody());

        deleteEmployee(tenantId, putResponse.getBody().getId());
    }

    // TODO: Add EmployeeAvailability CRUD tests once Tenant entity is moved; requires persisted RosterState entity

}
