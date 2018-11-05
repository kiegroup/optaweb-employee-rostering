/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.webapp.employee;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.optaweb.employeerostering.shared.contract.Contract;
import org.optaweb.employeerostering.shared.contract.ContractRestService;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeRestService;
import org.optaweb.employeerostering.shared.skill.Skill;
import org.optaweb.employeerostering.shared.skill.SkillRestService;
import org.optaweb.employeerostering.webapp.AbstractEntityRequireTenantRestServiceIT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class EmployeeRestServiceIT extends AbstractEntityRequireTenantRestServiceIT {

    private EmployeeRestService employeeRestService;
    private SkillRestService skillRestService;
    private ContractRestService contractRestService;

    public EmployeeRestServiceIT() {
        employeeRestService = serviceClientFactory.createEmployeeRestServiceClient();
        skillRestService = serviceClientFactory.createSkillRestServiceClient();
        contractRestService = serviceClientFactory.createContractRestServiceClient();
    }

    @Before
    public void setup() {
        createTestTenant();
    }

    @After
    public void cleanup() {
        deleteTestTenant();
    }

    private Skill createSkill(String name) {
        Skill skill = new Skill(TENANT_ID, name);
        Skill out = skillRestService.addSkill(TENANT_ID, skill);
        assertClientResponseOk();
        return out;
    }

    private Contract createContract(String name) {
        Contract contract = new Contract(TENANT_ID, name);
        Contract out = contractRestService.addContract(TENANT_ID, contract);
        assertClientResponseOk();
        return out;
    }

    @Test
    public void testDeleteNonExistingEmployee() {
        final long nonExistingEmployeeId = 123456L;
        boolean result = employeeRestService.removeEmployee(TENANT_ID, nonExistingEmployeeId);
        assertThat(result).isFalse();
        assertClientResponseOk();
    }

    @Test
    public void testUpdateNonExistingEmployee() {
        final long nonExistingEmployeeId = 123456L;
        Contract contract = createContract("Contract");
        Employee nonExistingEmployee = new Employee(TENANT_ID, "Non-existing employee", contract, Collections.emptySet());
        nonExistingEmployee.setSkillProficiencySet(Collections.emptySet());
        nonExistingEmployee.setId(nonExistingEmployeeId);
        Employee updatedEmployee = employeeRestService.updateEmployee(TENANT_ID, nonExistingEmployee);

        assertClientResponseOk();
        assertThat(updatedEmployee.getName()).isEqualTo(nonExistingEmployee.getName());
        assertThat(updatedEmployee.getSkillProficiencySet()).isEqualTo(nonExistingEmployee.getSkillProficiencySet());
        assertThat(updatedEmployee.getId()).isNotNull().isNotEqualTo(nonExistingEmployee.getId());
    }

    @Test
    public void testGetOfNonExistingEmployee() {
        final long nonExistingEmployeeId = 123456L;
        assertThatExceptionOfType(javax.ws.rs.NotFoundException.class)
                .isThrownBy(() -> employeeRestService.getEmployee(TENANT_ID, nonExistingEmployeeId));
        assertClientResponseError(Response.Status.NOT_FOUND);
    }

    @Test
    public void testCrudEmployee() {
        Skill skillA = createSkill("A");
        Skill skillB = createSkill("B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract("Contract");
        Employee testAddEmployee = new Employee(TENANT_ID, "Test Employee", contract, testSkillSet);
        employeeRestService.addEmployee(TENANT_ID, testAddEmployee);
        assertClientResponseOk();

        List<Employee> employees = employeeRestService.getEmployeeList(TENANT_ID);
        assertClientResponseOk();
        assertThat(employees).usingElementComparatorIgnoringFields(IGNORED_FIELDS).containsExactly(testAddEmployee);

        Employee testUpdateEmployee = employees.get(0);
        testUpdateEmployee.setName("ZZZ");

        Skill skillC = createSkill("C");
        testSkillSet.remove(skillA);
        testSkillSet.add(skillC);

        testUpdateEmployee.setSkillProficiencySet(testSkillSet);
        employeeRestService.updateEmployee(TENANT_ID, testUpdateEmployee);

        Employee retrievedEmployee = employeeRestService.getEmployee(TENANT_ID, testUpdateEmployee.getId());
        assertClientResponseOk();
        assertThat(retrievedEmployee).isNotNull().isEqualToIgnoringGivenFields(testUpdateEmployee, "version");

        boolean result = employeeRestService.removeEmployee(TENANT_ID, retrievedEmployee.getId());
        assertThat(result).isTrue();
        assertClientResponseOk();

        employees = employeeRestService.getEmployeeList(TENANT_ID);
        assertThat(employees).isEmpty();
    }
}
