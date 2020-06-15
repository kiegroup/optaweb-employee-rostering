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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.view.ContractView;
import org.optaweb.employeerostering.domain.employee.CovidRiskType;
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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
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

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet,
                                                     CovidRiskType.INOCULATED);
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
    public void getNonExistentEmployeeTest() throws Exception {
        String exceptionMessage = "No Employee entity found with ID (0).";
        String exceptionClass = "javax.persistence.EntityNotFoundException";
        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/employee/{id}", TENANT_ID, 0)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void getNonMatchingEmployeeTest() throws Exception {
        String exceptionMessage = "The tenantId (0) does not match the persistable (employee)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet,
                                                     CovidRiskType.INOCULATED);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/employee/{id}", 0, employee.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void deleteEmployeeTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet,
                                                     CovidRiskType.INOCULATED);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/employee/{id}", TENANT_ID, employee.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("true"));
    }

    @Test
    public void deleteNonExistentEmployeeTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/employee/{id}", TENANT_ID, 0)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("false"));
    }

    @Test
    public void deleteNonMatchingEmployeeTest() throws Exception {
        String exceptionMessage = "The tenantId (0) does not match the persistable (employee)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet,
                                                     CovidRiskType.INOCULATED);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/employee/{id}", 0, employee.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void createEmployeeTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet,
                                                     CovidRiskType.INOCULATED);
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
        String exceptionMessage = "The tenantId (0) does not match the persistable (employee)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet,
                                                     CovidRiskType.INOCULATED);
        String body = (new ObjectMapper()).writeValueAsString(employeeView);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/employee/add", 0)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void createNonMatchingSkillProficiencyTest() throws Exception {
        String exceptionMessage = "The tenantId (" + TENANT_ID + ") does not match the skillProficiency (A)'s " +
                "tenantId (0).";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(0, "A");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet,
                                                     CovidRiskType.INOCULATED);
        String body = (new ObjectMapper()).writeValueAsString(employeeView);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/employee/add", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void updateEmployeeTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet(),
                                                     CovidRiskType.INOCULATED);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        EmployeeView updatedEmployee = new EmployeeView(TENANT_ID, "updatedEmployee", contract, testSkillSet,
                                                        CovidRiskType.INOCULATED);
        updatedEmployee.setId(employee.getId());
        String body = (new ObjectMapper()).writeValueAsString(updatedEmployee);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/employee/update", TENANT_ID)
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
        String exceptionMessage = "The tenantId (0) does not match the persistable (updatedEmployee)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet,
                                                     CovidRiskType.INOCULATED);
        employeeService.createEmployee(TENANT_ID, employeeView);

        EmployeeView updatedEmployee = new EmployeeView(TENANT_ID, "updatedEmployee", contract, testSkillSet,
                                                        CovidRiskType.INOCULATED);
        String body = (new ObjectMapper()).writeValueAsString(updatedEmployee);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/employee/update", 0)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void updateNonExistentEmployeeTest() throws Exception {
        String exceptionMessage = "Employee entity with ID (0) not found.";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        Contract contract = new Contract();

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet(),
                                                     CovidRiskType.INOCULATED);
        employeeView.setId(0L);
        String body = (new ObjectMapper()).writeValueAsString(employeeView);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/employee/update", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void updateChangeTenantIdEmployeeTest() throws Exception {
        String exceptionMessage = "Employee entity with tenantId (" + TENANT_ID + ") cannot change tenants.";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contractA = createContract(TENANT_ID, "A");
        Contract contractB = createContract(0, "B");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contractA, testSkillSet,
                                                     CovidRiskType.INOCULATED);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        EmployeeView updatedEmployee = new EmployeeView(0, "updatedEmployee", contractB, Collections.emptySet(),
                                                        CovidRiskType.INOCULATED);
        updatedEmployee.setId(employee.getId());
        String body = (new ObjectMapper()).writeValueAsString(updatedEmployee);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/employee/update", 0)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    // ************************************************************************
    // EmployeeAvailability
    // ************************************************************************

    @Test
    public void getEmployeeAvailabilityTest() throws Exception {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet(),
                                                     CovidRiskType.INOCULATED);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                                                                                         startDateTime, endDateTime,
                                                                                         EmployeeAvailabilityState
                                                                                                 .UNAVAILABLE);
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
    public void getNonExistentEmployeeAvailabilityTest() throws Exception {
        String exceptionMessage = "No EmployeeAvailability entity found with ID (0).";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/employee/availability/{id}", TENANT_ID, 0)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void getNonMatchingEmployeeAvailabilityTest() throws Exception {
        String exceptionMessage = "The tenantId (0) does not match the persistable " +
                "(employee:1999-12-31T23:59Z-2000-01-01T00:00Z)'s tenantId (" + TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet(),
                                                     CovidRiskType.INOCULATED);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                                                                                         startDateTime, endDateTime,
                                                                                         EmployeeAvailabilityState
                                                                                                 .UNAVAILABLE);
        EmployeeAvailabilityView persistedEmployeeAvailabilityView =
                employeeService.createEmployeeAvailability(TENANT_ID, employeeAvailabilityView);

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/employee/availability/{id}", 0,
                                 persistedEmployeeAvailabilityView.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void deleteEmployeeAvailabilityTest() throws Exception {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet(),
                                                     CovidRiskType.INOCULATED);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                                                                                         startDateTime, endDateTime,
                                                                                         EmployeeAvailabilityState
                                                                                                 .UNAVAILABLE);
        EmployeeAvailabilityView persistedEmployeeAvailabilityView =
                employeeService.createEmployeeAvailability(TENANT_ID, employeeAvailabilityView);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/employee/availability/{id}", TENANT_ID,
                                    persistedEmployeeAvailabilityView.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("true"));
    }

    @Test
    public void deleteNonExistentEmployeeAvailabilityTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/employee/availability/{id}", TENANT_ID, 0)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("false"));
    }

    @Test
    public void deleteNonMatchingEmployeeAvailabilityTest() throws Exception {
        String exceptionMessage = "The tenantId (0) does not match the persistable " +
                "(employee:1999-12-31T23:59Z-2000-01-01T00:00Z)'s tenantId (" + TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet(),
                                                     CovidRiskType.INOCULATED);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                                                                                         startDateTime, endDateTime,
                                                                                         EmployeeAvailabilityState
                                                                                                 .UNAVAILABLE);
        EmployeeAvailabilityView persistedEmployeeAvailabilityView =
                employeeService.createEmployeeAvailability(TENANT_ID, employeeAvailabilityView);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/employee/availability/{id}", 0,
                                    persistedEmployeeAvailabilityView.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void createEmployeeAvailabilityTest() throws Exception {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet(),
                                                     CovidRiskType.INOCULATED);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                                                                                         startDateTime, endDateTime,
                                                                                         EmployeeAvailabilityState
                                                                                                 .UNAVAILABLE);
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

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet(),
                                                     CovidRiskType.INOCULATED);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                                                                                         startDateTime, endDateTime,
                                                                                         EmployeeAvailabilityState
                                                                                                 .UNAVAILABLE);
        String body = (new ObjectMapper()).writeValueAsString(employeeAvailabilityView);
        String employeeAvailabilityName =
                employeeAvailabilityView.getEmployeeId() + ":" + startDateTime + "-" + endDateTime;

        String exceptionMessage = "The tenantId (0) does not match the persistable (" + employeeAvailabilityName +
                ")'s tenantId (" + TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/employee/availability/add", 0)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void updateEmployeeAvailabilityTest() throws Exception {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet(),
                                                     CovidRiskType.INOCULATED);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                                                                                         startDateTime, endDateTime,
                                                                                         EmployeeAvailabilityState
                                                                                                 .UNAVAILABLE);
        EmployeeAvailabilityView persistedEmployeeAvailabilityView =
                employeeService.createEmployeeAvailability(TENANT_ID, employeeAvailabilityView);

        EmployeeAvailabilityView updatedEmployeeAvailability = new EmployeeAvailabilityView(TENANT_ID, employee,
                                                                                            startDateTime, endDateTime,
                                                                                            EmployeeAvailabilityState
                                                                                                    .DESIRED);
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

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet(),
                                                     CovidRiskType.INOCULATED);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                                                                                         startDateTime, endDateTime,
                                                                                         EmployeeAvailabilityState
                                                                                                 .UNAVAILABLE);
        EmployeeAvailabilityView persistedEmployeeAvailabilityView =
                employeeService.createEmployeeAvailability(TENANT_ID, employeeAvailabilityView);

        EmployeeAvailabilityView updatedEmployeeAvailability = new EmployeeAvailabilityView(TENANT_ID, employee,
                                                                                            startDateTime, endDateTime,
                                                                                            EmployeeAvailabilityState
                                                                                                    .DESIRED);
        updatedEmployeeAvailability.setId(persistedEmployeeAvailabilityView.getId());
        String body = (new ObjectMapper()).writeValueAsString(updatedEmployeeAvailability);

        String employeeAvailabilityName =
                updatedEmployeeAvailability.getEmployeeId() + ":" + startDateTime + "-" + endDateTime;

        String exceptionMessage = "The tenantId (0) does not match the persistable (" + employeeAvailabilityName +
                ")'s tenantId (" + TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        mvc.perform(MockMvcRequestBuilders
                            .put("/rest/tenant/{tenantId}/employee/availability/update", 0)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void updateNonExistentEmployeeAvailabilityTest() throws Exception {
        String exceptionMessage = "EmployeeAvailability entity with ID (0) not found.";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet(),
                                                     CovidRiskType.INOCULATED);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                                                                                         startDateTime, endDateTime,
                                                                                         EmployeeAvailabilityState
                                                                                                 .DESIRED);
        employeeAvailabilityView.setId(0L);
        String body = (new ObjectMapper()).writeValueAsString(employeeAvailabilityView);

        mvc.perform(MockMvcRequestBuilders
                            .put("/rest/tenant/{tenantId}/employee/availability/update", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    private Set<Skill> getSkillSet(String... names) {
        AtomicLong id = new AtomicLong(0L);
        return Arrays.stream(names)
                .map(name -> {
                    Skill out = new Skill(TENANT_ID, name);
                    out.setId(id.incrementAndGet());
                    return out;
                })
                .collect(Collectors.toSet());
    }

    @Test
    public void employeeListImportTest() throws IOException {
        Contract defaultContract = contractService.getOrCreateDefaultContract(TENANT_ID);
        CovidRiskType defaultCovidRisk = CovidRiskType.LOW;

        List<Employee> expectedEmployeeList = Arrays.asList(
                new Employee(TENANT_ID, "Amy Cole", defaultContract, getSkillSet("Nurse"), defaultCovidRisk),
                new Employee(TENANT_ID, "Beth Fox", defaultContract,
                             getSkillSet("Respiratory Specialist", "Nurse"), defaultCovidRisk),
                new Employee(TENANT_ID, "Chad Green", defaultContract, getSkillSet("Nurse"), defaultCovidRisk),
                new Employee(TENANT_ID, "Dan Jones", defaultContract, getSkillSet("Nurse"), defaultCovidRisk),
                new Employee(TENANT_ID, "Elsa King", defaultContract, getSkillSet("Nurse"), defaultCovidRisk),
                new Employee(TENANT_ID, "Flo Li", defaultContract,
                             getSkillSet("Nurse", "Emergency Person"), defaultCovidRisk),
                new Employee(TENANT_ID, "Gus Poe", defaultContract, getSkillSet("Nurse"), defaultCovidRisk),
                new Employee(TENANT_ID, "Hugo Rye", defaultContract, Collections.emptySet(), defaultCovidRisk),
                new Employee(TENANT_ID, "Ivy Smith", defaultContract, getSkillSet("Nurse"), defaultCovidRisk),
                new Employee(TENANT_ID, "Amy Fox", defaultContract, getSkillSet("Foo Faa fu"), defaultCovidRisk),
                new Employee(TENANT_ID, "Beth Green", defaultContract,
                             getSkillSet("Foo Faa fu"), defaultCovidRisk),
                new Employee(TENANT_ID, "Chad Jones", defaultContract, getSkillSet("Nurse"), defaultCovidRisk),
                new Employee(TENANT_ID, "Dan King", defaultContract, Collections.emptySet(), defaultCovidRisk),
                new Employee(TENANT_ID, "Elsa Li", defaultContract, getSkillSet("Nurse"), defaultCovidRisk),
                new Employee(TENANT_ID, "Flo Poe", defaultContract, getSkillSet("Doctor"), defaultCovidRisk),
                new Employee(TENANT_ID, "Gus Rye", defaultContract, getSkillSet("Nurse"), defaultCovidRisk),
                new Employee(TENANT_ID, "Hugo Smith", defaultContract, getSkillSet("Nurse"), defaultCovidRisk),
                new Employee(TENANT_ID, "Ivy Watt", defaultContract, getSkillSet("Nur se"), defaultCovidRisk),
                new Employee(TENANT_ID, "Jay Cole", defaultContract, getSkillSet("Nurse"), defaultCovidRisk),
                new Employee(TENANT_ID, "Amy Green", defaultContract, getSkillSet("Nurse"), defaultCovidRisk),
                new Employee(TENANT_ID, "Beth Jones", defaultContract,
                             getSkillSet("Respiratory Specialist"), defaultCovidRisk),
                new Employee(TENANT_ID, "Chad King", defaultContract, getSkillSet("Nurse"), defaultCovidRisk),
                new Employee(TENANT_ID, "Dan Li", defaultContract, getSkillSet("Doctor"), defaultCovidRisk),
                new Employee(TENANT_ID, "My Name", defaultContract, getSkillSet("Nur se"), defaultCovidRisk));

        Function<List<Employee>, Consumer<Employee>> assertListMatch = employeeList -> expected -> {
            Optional<Employee> maybeActual = employeeList.stream()
                    .filter(e -> e.getName().equals(expected.getName())).findAny();
            if (maybeActual.isPresent()) {
                Employee actual = maybeActual.get();
                assertEquals("Wrong contract for " + expected.getName(),
                             expected.getContract(), actual.getContract());
                assertEquals("Wrong covid risk type for " + expected.getName(),
                             expected.getCovidRiskType(), actual.getCovidRiskType());
                assertEquals("Wrong tenant id for " + expected.getName(),
                             expected.getTenantId(), actual.getTenantId());
                assertEquals("Wrong number of skills for " + expected.getName(),
                             expected.getSkillProficiencySet().size(), actual.getSkillProficiencySet().size());
                
                expected.getSkillProficiencySet().forEach(skill -> assertTrue("Missing skill " + skill.getName() +
                                                                                      " for employee " + 
                                                                                      expected.getName(),
                                                                              actual.getSkillProficiencySet().stream()
                                                                                      .anyMatch(s -> s.getName()
                                                                                              .equals(skill.getName()))
                                                                                      ));
            } else {
                fail("Expected an employee with name (" + expected.getName() + "), but no such employee was found.");
            }
        };

        final List<Employee> excelEmployeeList = employeeService
                .importEmployeesFromExcel(TENANT_ID, getClass().getResourceAsStream("/EmployeeList.xlsx"));
        expectedEmployeeList.forEach(assertListMatch.apply(excelEmployeeList));
        final List<Employee> allEmployeeList = employeeService.getEmployeeList(TENANT_ID);
        expectedEmployeeList.forEach(assertListMatch.apply(allEmployeeList));
    }
}
