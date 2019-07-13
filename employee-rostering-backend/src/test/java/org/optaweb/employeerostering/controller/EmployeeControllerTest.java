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

package org.optaweb.employeerostering.controller;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.service.ContractService;
import org.optaweb.employeerostering.service.EmployeeService;
import org.optaweb.employeerostering.service.SkillService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class EmployeeControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeControllerTest.class);

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private SkillService skillService;

    @Autowired
    private ContractService contractService;

    private Skill createSkill(Integer tenantId, String name) {
        Skill skill = new Skill(tenantId, name);
        Skill out = skillService.createSkill(tenantId, skill);
        return out;
    }

    private Contract createContract(Integer tenantId, String name) {
        Contract contract = new Contract(tenantId, name);
        Contract out = contractService.createContract(tenantId, contract);
        return out;
    }

    @Test
    public void getEmployeeListTest() {
        Integer tenantId = 1;
        Integer tenantId2 = 2;
        String name = "name";
        String name2 = "name2";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Skill skillC = createSkill(tenantId2, "C");
        Set<Skill> testSkillSet2 = new HashSet<>();
        testSkillSet2.add(skillC);

        Contract contractA = createContract(tenantId, "A");
        Contract contractB = createContract(tenantId2, "B");

        Employee employee = new Employee(tenantId, name, contractA, testSkillSet);
        Employee employee2 = new Employee(tenantId, name2, contractA, testSkillSet);
        Employee employee3 = new Employee(tenantId2, name, contractB, testSkillSet2);

        employeeService.createEmployee(tenantId, employee);
        employeeService.createEmployee(tenantId, employee2);
        employeeService.createEmployee(tenantId2, employee3);

        List<Employee> employeeList = employeeService.getEmployeeList(tenantId);
        List<Employee> employeeList2 = employeeService.getEmployeeList(tenantId2);

        assertEquals(employee, employeeList.get(0));
        assertEquals(employee2, employeeList.get(1));
        assertEquals(employee3, employeeList2.get(0));
    }

    @Test
    public void getEmployeeTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contractA = createContract(tenantId, "A");

        Employee employee = new Employee(tenantId, name, contractA, testSkillSet);

        employeeService.createEmployee(tenantId, employee);

        Employee returnEmployee = employeeService.getEmployee(tenantId, employee.getId());

        assertEquals(tenantId, returnEmployee.getTenantId());
        assertEquals(name, returnEmployee.getName());
        assertEquals(testSkillSet, returnEmployee.getSkillProficiencySet());
        assertEquals(contractA, returnEmployee.getContract());
    }

    @Test
    public void getNonExistentEmployeeTest() {
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> employeeService.getEmployee(1, -1L))
                .withMessage("No Employee entity found with ID (-1).");
    }

    @Test
    public void getNonMatchingEmployeeTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contractA = createContract(tenantId, "A");

        Employee employee = new Employee(tenantId, name, contractA, testSkillSet);

        employeeService.createEmployee(tenantId, employee);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> employeeService.getEmployee(2, employee.getId()))
                .withMessage("The tenantId (2) does not match the persistable (name)'s tenantId (1).");
    }

    @Test
    public void deleteEmployeeTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contractA = createContract(tenantId, "A");

        Employee employee = new Employee(tenantId, name, contractA, testSkillSet);

        employeeService.createEmployee(tenantId, employee);

        assertEquals(true, employeeService.deleteEmployee(tenantId, employee.getId()));
    }

    @Test
    public void deleteNonExistentEmployeeTest() {
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> employeeService.deleteEmployee(1, -1L))
                .withMessage("No Employee entity found with ID (-1).");
    }

    @Test
    public void deleteNonMatchingEmployeeTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contractA = createContract(tenantId, "A");

        Employee employee = new Employee(tenantId, name, contractA, testSkillSet);

        employeeService.createEmployee(tenantId, employee);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> employeeService.deleteEmployee(2, employee.getId()))
                .withMessage("The tenantId (2) does not match the persistable (name)'s tenantId (1).");
    }

    @Test
    public void createEmployeeTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contractA = createContract(tenantId, "A");

        Employee employee = new Employee(tenantId, name, contractA, testSkillSet);

        Employee returnEmployee = employeeService.createEmployee(tenantId, employee);

        assertEquals(tenantId, returnEmployee.getTenantId());
        assertEquals(name, returnEmployee.getName());
        assertEquals(testSkillSet, returnEmployee.getSkillProficiencySet());
        assertEquals(contractA, returnEmployee.getContract());
    }

    @Test
    public void createNonMatchingEmployeeTest(){
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contractA = createContract(tenantId, "A");

        Employee employee = new Employee(tenantId, name, contractA, testSkillSet);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> employeeService.createEmployee(2, employee))
                .withMessage("The tenantId (2) does not match the persistable (name)'s tenantId (1).");
    }

    @Test
    public void updateEmployeeTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contractA = createContract(tenantId, "A");

        Employee employee = new Employee(tenantId, name, contractA, Collections.emptySet());

        employeeService.createEmployee(tenantId, employee);

        Employee employee2 = new Employee(tenantId, "name2", contractA, testSkillSet);
        employee2.setId(employee.getId());

        Employee returnEmployee = employeeService.updateEmployee(tenantId, employee2);

        assertEquals(tenantId, returnEmployee.getTenantId());
        assertEquals("name2", returnEmployee.getName());
        assertEquals(testSkillSet, returnEmployee.getSkillProficiencySet());
        assertEquals(contractA, returnEmployee.getContract());
    }

    @Test
    public void updateNonMatchingEmployeeTest(){
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contractA = createContract(tenantId, "A");

        Employee employee = new Employee(tenantId, name, contractA, Collections.emptySet());

        employeeService.createEmployee(tenantId, employee);

        Employee employee2 = new Employee(tenantId, "name2", contractA, testSkillSet);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> employeeService.updateEmployee(2, employee2))
                .withMessage("The tenantId (2) does not match the persistable (name2)'s tenantId (1).");
    }

    @Test
    public void updateNonExistentEmployeeTest() {
        Contract contractA = createContract(1, "A");
        Employee employee = new Employee(1, "name", contractA, Collections.emptySet());
        employee.setId(-1L);

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> employeeService.updateEmployee(1, employee))
                .withMessage("Employee entity with ID (-1) not found.");
    }

    @Test
    public void updateChangeTenantIdEmployeeTest() {
        Integer tenantId = 1;
        Integer tenantId2 = 2;
        String name = "name";

        Skill skillA = createSkill(tenantId2, "A");
        Skill skillB = createSkill(tenantId2, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contractA = createContract(tenantId, "A");

        Employee employee = new Employee(tenantId, name, contractA, Collections.emptySet());

        employeeService.createEmployee(tenantId, employee);

        Contract contractB = createContract(tenantId2, "B");

        Employee employee2 = new Employee(tenantId2, name, contractB, testSkillSet);
        employee2.setId(employee.getId());

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> employeeService.updateEmployee(tenantId2, employee2))
                .withMessage("Employee entity with tenantId (1) cannot change tenants.");
    }
}
