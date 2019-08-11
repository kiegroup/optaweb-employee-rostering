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
import java.util.Set;

import javax.transaction.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.view.ContractView;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.view.EmployeeView;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.skill.view.SkillView;
import org.optaweb.employeerostering.service.contract.ContractService;
import org.optaweb.employeerostering.service.employee.EmployeeService;
import org.optaweb.employeerostering.service.skill.SkillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.NestedServletException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EmployeeServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceTest.class);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private SkillService skillService;

    @Autowired
    private ContractService contractService;

    private Skill createSkill(Integer tenantId, String name) {
        SkillView skillView = new SkillView(tenantId, name);
        return skillService.createSkill(tenantId, skillView);
    }

    private Contract createContract(Integer tenantId, String name) {
        ContractView contractView = new ContractView(tenantId, name);
        return contractService.createContract(tenantId, contractView);
    }

    // TODO: Add createTestTenant() and deleteTestTenant() setup methods to persist tenant and rosterState entities
    //  before running tests once Tenant CRUD methods are implemented

    @Test
    public void getEmployeeListTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/employee/", 2)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void getEmployeeTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(tenantId, name);

        EmployeeView employeeView = new EmployeeView(tenantId, name, contract, testSkillSet);
        Employee employee = employeeService.createEmployee(tenantId, employeeView);

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/employee/{id}", tenantId, employee.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(tenantId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(name))
                .andExpect(MockMvcResultMatchers.jsonPath("$.contract").value(contract))
                .andExpect(MockMvcResultMatchers.jsonPath("$.skillProficiencySet").isNotEmpty());
    }

    @Test
    public void getNonExistentEmployeeTest() {
        Integer tenantId = 2;

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .get("/rest/tenant/{tenantId}/employee/{id}", tenantId, 1L)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                                     "Exception: No Employee entity found with ID (1).");
    }

    @Test
    public void getNonMatchingEmployeeTest() {
        Integer tenantId = 2;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(tenantId, name);

        EmployeeView employeeView = new EmployeeView(tenantId, name, contract, testSkillSet);
        Employee employee = employeeService.createEmployee(tenantId, employeeView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .get("/rest/tenant/{tenantId}/employee/{id}", 3,
                                                           employee.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: The " +
                                     "tenantId (3) does not match the persistable (name)'s tenantId (2).");
    }

    @Test
    public void deleteEmployeeTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(tenantId, name);

        EmployeeView employeeView = new EmployeeView(tenantId, name, contract, testSkillSet);
        Employee employee = employeeService.createEmployee(tenantId, employeeView);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/employee/{id}", tenantId, employee.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().string("true"));
    }

    @Test
    public void deleteNonExistentEmployeeTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/employee/{id}", 1, 0L)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().string("false"));
    }

    @Test
    public void deleteNonMatchingEmployeeTest() {
        Integer tenantId = 2;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(tenantId, name);

        EmployeeView employeeView = new EmployeeView(tenantId, name, contract, testSkillSet);
        Employee employee = employeeService.createEmployee(tenantId, employeeView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders.delete("/rest/tenant/{tenantId}/employee/{id}", 3,
                                                                            employee.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (3) does not match the persistable (name)'s tenantId (2).");
    }

    @Test
    public void createEmployeeTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(tenantId, name);

        EmployeeView employeeView = new EmployeeView(tenantId, name, contract, testSkillSet);
        String body = (new ObjectMapper()).writeValueAsString(employeeView);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/employee/add", tenantId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(tenantId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(name))
                .andExpect(MockMvcResultMatchers.jsonPath("$.contract").value(contract))
                .andExpect(MockMvcResultMatchers.jsonPath("$.skillProficiencySet").isNotEmpty());
    }

    @Test
    public void createNonMatchingEmployeeTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(tenantId, name);

        EmployeeView employeeView = new EmployeeView(tenantId, name, contract, testSkillSet);
        String body = (new ObjectMapper()).writeValueAsString(employeeView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .post("/rest/tenant/{tenantId}/employee/add", 3)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (3) does not match the persistable (name)'s tenantId (2).");
    }

    @Test
    public void createNonMatchingSkillProficiencyTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";

        Skill skillA = createSkill(3, "A");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);

        Contract contract = createContract(tenantId, name);

        EmployeeView employeeView = new EmployeeView(tenantId, name, contract, testSkillSet);
        String body = (new ObjectMapper()).writeValueAsString(employeeView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .post("/rest/tenant/{tenantId}/employee/add", tenantId)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (2) does not match the skillProficiency (A)'s tenantId (3).");
    }

    @Test
    public void updateEmployeeTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(tenantId, name);

        EmployeeView employeeView = new EmployeeView(tenantId, name, contract, testSkillSet);
        Employee employee = employeeService.createEmployee(tenantId, employeeView);

        EmployeeView employeeView2 = new EmployeeView(tenantId, "name2", contract, testSkillSet);
        employeeView2.setId(employee.getId());
        String body = (new ObjectMapper()).writeValueAsString(employeeView2);

        mvc.perform(MockMvcRequestBuilders
                            .put("/rest/tenant/{tenantId}/employee/update", tenantId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(tenantId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("name2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.contract").value(contract))
                .andExpect(MockMvcResultMatchers.jsonPath("$.skillProficiencySet").isNotEmpty());
    }

    @Test
    public void updateNonMatchingEmployeeTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(tenantId, name);

        EmployeeView employeeView = new EmployeeView(tenantId, name, contract, testSkillSet);
        Employee employee = employeeService.createEmployee(tenantId, employeeView);

        EmployeeView employeeView2 = new EmployeeView(tenantId, "name2", contract, testSkillSet);
        String body = (new ObjectMapper()).writeValueAsString(employeeView2);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/employee/update", 3)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (3) does not match the persistable (name2)'s tenantId (2).");
    }

    @Test
    public void updateNonExistentEmployeeTest() throws Exception {
        Integer tenantId = 2;
        Contract contract = new Contract();

        EmployeeView employeeView = new EmployeeView(tenantId, "name", contract, Collections.emptySet());
        employeeView.setId(-1L);
        String body = (new ObjectMapper()).writeValueAsString(employeeView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/employee/update", tenantId)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                                     "Exception: Employee entity with ID (-1) not found.");
    }

    @Test
    public void updateChangeTenantIdEmployeeTest() throws Exception {
        Integer tenantId = 2;
        Integer tenantId2 = 3;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(tenantId, name);
        Contract contract2 = createContract(tenantId2, name);

        EmployeeView employeeView = new EmployeeView(tenantId, name, contract, testSkillSet);
        Employee employee = employeeService.createEmployee(tenantId, employeeView);

        EmployeeView employeeView2 = new EmployeeView(tenantId2, name, contract2, Collections.emptySet());
        employeeView2.setId(employee.getId());
        String body = (new ObjectMapper()).writeValueAsString(employeeView2);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/employee/update", tenantId2)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalState" +
                                     "Exception: Employee entity with tenantId (2) cannot change tenants.");
    }

    // TODO: Add EmployeeAvailability CRUD tests once Tenant entity is moved; requires persisted RosterState entity

}
