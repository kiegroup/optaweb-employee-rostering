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
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.view.ContractView;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.domain.employee.view.EmployeeAvailabilityView;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.NestedServletException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EmployeeServiceTest extends AbstractEntityRequireTenantRestServiceTest {

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

    private EmployeeAvailabilityView createEmployeeAvailability(Integer tenantId, Employee employee,
                                                                LocalDateTime startDateTime, LocalDateTime endDateTime,
                                                                EmployeeAvailabilityState state) {
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(tenantId, employee,
                startDateTime, endDateTime,
                state);
        return employeeService.createEmployeeAvailability(tenantId, employeeAvailabilityView);
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
    public void getEmployeeListTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .get("/rest/tenant/{tenantId}/employee/", TENANT_ID)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void getEmployeeTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        mvc.perform(MockMvcRequestBuilders
                .get("/rest/tenant/{tenantId}/employee/{id}", TENANT_ID, employee.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("employee"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.contract").value(contract))
                .andExpect(MockMvcResultMatchers.jsonPath("$.skillProficiencySet").isNotEmpty());
    }

    @Test
    public void getNonExistentEmployeeTest() {
        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .get("/rest/tenant/{tenantId}/employee/{id}", TENANT_ID, 0)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                        "Exception: No Employee entity found with ID (0).");
    }

    @Test
    public void getNonMatchingEmployeeTest() {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .get("/rest/tenant/{tenantId}/employee/{id}", 0,
                                employee.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: The " +
                        "tenantId (0) does not match the persistable (employee)'s tenantId (" + TENANT_ID + ").");
    }

    @Test
    public void deleteEmployeeTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        mvc.perform(MockMvcRequestBuilders
                .delete("/rest/tenant/{tenantId}/employee/{id}", TENANT_ID, employee.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().string("true"));
    }

    @Test
    public void deleteNonExistentEmployeeTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .delete("/rest/tenant/{tenantId}/employee/{id}", TENANT_ID, 0)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().string("false"));
    }

    @Test
    public void deleteNonMatchingEmployeeTest() {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders.delete("/rest/tenant/{tenantId}/employee/{id}", 0,
                        employee.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                        "The tenantId (0) does not match the persistable (employee)'s tenantId (" + TENANT_ID + ").");
    }

    @Test
    public void createEmployeeTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet);
        String body = (new ObjectMapper()).writeValueAsString(employeeView);

        mvc.perform(MockMvcRequestBuilders
                .post("/rest/tenant/{tenantId}/employee/add", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("employee"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.contract").value(contract))
                .andExpect(MockMvcResultMatchers.jsonPath("$.skillProficiencySet").isNotEmpty());
    }

    @Test
    public void createNonMatchingEmployeeTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet);
        String body = (new ObjectMapper()).writeValueAsString(employeeView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .post("/rest/tenant/{tenantId}/employee/add", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                        "The tenantId (0) does not match the persistable (employee)'s tenantId (" + TENANT_ID + ").");
    }

    @Test
    public void createNonMatchingSkillProficiencyTest() throws Exception {
        Skill skillA = createSkill(0, "A");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet);
        String body = (new ObjectMapper()).writeValueAsString(employeeView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .post("/rest/tenant/{tenantId}/employee/add", TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                        "The tenantId (" + TENANT_ID + ") does not match the skillProficiency (A)'s " +
                        "tenantId (0).");
    }

    @Test
    public void updateEmployeeTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        EmployeeView updatedEmployee = new EmployeeView(TENANT_ID, "updatedEmployee", contract, testSkillSet);
        updatedEmployee.setId(employee.getId());
        String body = (new ObjectMapper()).writeValueAsString(updatedEmployee);

        mvc.perform(MockMvcRequestBuilders
                .put("/rest/tenant/{tenantId}/employee/update", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("updatedEmployee"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.contract").value(contract))
                .andExpect(MockMvcResultMatchers.jsonPath("$.skillProficiencySet").isNotEmpty());
    }

    @Test
    public void updateNonMatchingEmployeeTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet);
        employeeService.createEmployee(TENANT_ID, employeeView);

        EmployeeView updatedEmployee = new EmployeeView(TENANT_ID, "updatedEmployee", contract, testSkillSet);
        String body = (new ObjectMapper()).writeValueAsString(updatedEmployee);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .put("/rest/tenant/{tenantId}/employee/update", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                        "The tenantId (0) does not match the persistable (updatedEmployee)'s tenantId " + "(" +
                        TENANT_ID + ").");
    }

    @Test
    public void updateNonExistentEmployeeTest() throws Exception {
        Contract contract = new Contract();

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        employeeView.setId(0L);
        String body = (new ObjectMapper()).writeValueAsString(employeeView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .put("/rest/tenant/{tenantId}/employee/update", TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                        "Exception: Employee entity with ID (0) not found.");
    }

    @Test
    public void updateChangeTenantIdEmployeeTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contractA = createContract(TENANT_ID, "A");
        Contract contractB = createContract(0, "B");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contractA, testSkillSet);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        EmployeeView updatedEmployee = new EmployeeView(0, "updatedEmployee", contractB, Collections.emptySet());
        updatedEmployee.setId(employee.getId());
        String body = (new ObjectMapper()).writeValueAsString(updatedEmployee);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .put("/rest/tenant/{tenantId}/employee/update", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalState" +
                        "Exception: Employee entity with tenantId (" + TENANT_ID + ") " +
                        "cannot change tenants.");
    }

    // ************************************************************************
    // EmployeeAvailability
    // ************************************************************************

    @Test
    public void getEmployeeAvailabilityTest() throws Exception {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime, EmployeeAvailabilityState.UNAVAILABLE);
        EmployeeAvailabilityView persistedEmployeeAvailabilityView =
                employeeService.createEmployeeAvailability(TENANT_ID, employeeAvailabilityView);

        mvc.perform(MockMvcRequestBuilders
                .get("/rest/tenant/{tenantId}/employee/availability/{id}", TENANT_ID,
                        persistedEmployeeAvailabilityView.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employeeId").value(employee.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.startDateTime").value("1999-12-31T23:59:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.endDateTime").value("2000-01-01T00:00:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.state").value("UNAVAILABLE"));
    }

    @Test
    public void getNonExistentEmployeeAvailabilityTest() {
        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .get("/rest/tenant/{tenantId}/employee/availability/{id}",
                                TENANT_ID, 0)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                        "Exception: No EmployeeAvailability entity found with ID (0).");
    }

    @Test
    public void getNonMatchingEmployeeAvailabilityTest() {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime, EmployeeAvailabilityState.UNAVAILABLE);
        EmployeeAvailabilityView persistedEmployeeAvailabilityView =
                employeeService.createEmployeeAvailability(TENANT_ID, employeeAvailabilityView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .get("/rest/tenant/{tenantId}/employee/availability/{id}", 0,
                                persistedEmployeeAvailabilityView.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: The " +
                        "tenantId (0) does not match the persistable " + "(employee:1999-12-31T23:59Z-2000-01-01T00" +
                        ":00Z)'s tenantId (" + TENANT_ID + ").");
    }

    @Test
    public void deleteEmployeeAvailabilityTest() throws Exception {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime, EmployeeAvailabilityState.UNAVAILABLE);
        EmployeeAvailabilityView persistedEmployeeAvailabilityView =
                employeeService.createEmployeeAvailability(TENANT_ID, employeeAvailabilityView);

        mvc.perform(MockMvcRequestBuilders
                .delete("/rest/tenant/{tenantId}/employee/availability/{id}", TENANT_ID,
                        persistedEmployeeAvailabilityView.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().string("true"));
    }

    @Test
    public void deleteNonExistentEmployeeAvailabilityTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                .delete("/rest/tenant/{tenantId}/employee/availability/{id}", TENANT_ID, 0)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().string("false"));
    }

    @Test
    public void deleteNonMatchingEmployeeAvailabilityTest() {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime, EmployeeAvailabilityState.UNAVAILABLE);
        EmployeeAvailabilityView persistedEmployeeAvailabilityView =
                employeeService.createEmployeeAvailability(TENANT_ID, employeeAvailabilityView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders.delete("/rest/tenant/{tenantId}/employee" +
                                "/availability/{id}", 0,
                        persistedEmployeeAvailabilityView.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                        "The tenantId (0) does not match the persistable " +
                        "(employee:1999-12-31T23:59Z-2000-01-01T00:00Z)'s" + " tenantId (" + TENANT_ID + ").");
    }

    @Test
    public void createEmployeeAvailabilityTest() throws Exception {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime, EmployeeAvailabilityState.UNAVAILABLE);
        String body = (new ObjectMapper()).writeValueAsString(employeeAvailabilityView);

        mvc.perform(MockMvcRequestBuilders
                .post("/rest/tenant/{tenantId}/employee/availability/add", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employeeId").value(employee.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.startDateTime").value("1999-12-31T23:59:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.endDateTime").value("2000-01-01T00:00:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.state").value("UNAVAILABLE"));
    }

    @Test
    public void createNonMatchingEmployeeAvailabilityTest() throws Exception {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime, EmployeeAvailabilityState.UNAVAILABLE);
        String body = (new ObjectMapper()).writeValueAsString(employeeAvailabilityView);
        String employeeAvailabilityName =
                employeeAvailabilityView.getEmployeeId() + ":" + startDateTime + "-" + endDateTime;

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .post("/rest/tenant/{tenantId}/employee/availability/add", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                        "The tenantId (0) does not match the persistable (" + employeeAvailabilityName +
                        ")'s tenantId (" + TENANT_ID + ").");
    }

    @Test
    public void updateEmployeeAvailabilityTest() throws Exception {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime, EmployeeAvailabilityState.UNAVAILABLE);
        EmployeeAvailabilityView persistedEmployeeAvailabilityView =
                employeeService.createEmployeeAvailability(TENANT_ID, employeeAvailabilityView);

        EmployeeAvailabilityView updatedEmployeeAvailability = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime, EmployeeAvailabilityState.DESIRED);
        updatedEmployeeAvailability.setId(persistedEmployeeAvailabilityView.getId());
        String body = (new ObjectMapper()).writeValueAsString(updatedEmployeeAvailability);

        mvc.perform(MockMvcRequestBuilders
                .put("/rest/tenant/{tenantId}/employee/availability/update", TENANT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.employeeId").value(employee.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.startDateTime").value("1999-12-31T23:59:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.endDateTime").value("2000-01-01T00:00:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.state").value("DESIRED"));
    }

    @Test
    public void updateNonMatchingEmployeeAvailabilityTest() throws Exception {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime, EmployeeAvailabilityState.UNAVAILABLE);
        EmployeeAvailabilityView persistedEmployeeAvailabilityView =
                employeeService.createEmployeeAvailability(TENANT_ID, employeeAvailabilityView);

        EmployeeAvailabilityView updatedEmployeeAvailability = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime, EmployeeAvailabilityState.DESIRED);
        updatedEmployeeAvailability.setId(persistedEmployeeAvailabilityView.getId());
        String body = (new ObjectMapper()).writeValueAsString(updatedEmployeeAvailability);

        String employeeAvailabilityName =
                updatedEmployeeAvailability.getEmployeeId() + ":" + startDateTime + "-" + endDateTime;

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .put("/rest/tenant/{tenantId}/employee/availability/update", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                        "The tenantId (0) does not match the persistable (" + employeeAvailabilityName +
                        ")'s tenantId (" + TENANT_ID + ").");
    }

    @Test
    public void updateNonExistentEmployeeAvailabilityTest() throws Exception {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime, EmployeeAvailabilityState.DESIRED);
        employeeAvailabilityView.setId(0L);
        String body = (new ObjectMapper()).writeValueAsString(employeeAvailabilityView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                        .put("/rest/tenant/{tenantId}/employee/availability/update",
                                TENANT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                        "Exception: EmployeeAvailability entity with ID (0) not found.");
    }
}
