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

package org.optaweb.employeerostering.contract;

import javax.transaction.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.view.ContractView;
import org.optaweb.employeerostering.service.contract.ContractService;
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
public class ContractServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ContractServiceTest.class);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ContractService contractService;

    // TODO: Add createTestTenant() and deleteTestTenant() setup methods to persist tenant and rosterState entities
    //  before running tests once Tenant CRUD methods are implemented

    @Test
    public void getContractListTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/contract/", 2)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void getContractTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        ContractView contractView = new ContractView(tenantId, name, maximumMinutesPerDay, maximumMinutesPerWeek,
                                             maximumMinutesPerMonth, maximumMinutesPerYear);
        Contract contract = contractService.createContract(tenantId, contractView);

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/contract/{id}", tenantId, contract.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(tenantId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(name))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerDay").value(maximumMinutesPerDay))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerWeek").value(maximumMinutesPerWeek))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerMonth").value(maximumMinutesPerMonth))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerYear").value(maximumMinutesPerYear));
    }

    @Test
    public void getNonExistentContractTest() {
        Integer tenantId = 2;

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .get("/rest/tenant/{tenantId}/contract/{id}", tenantId, 1L)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                                     "Exception: No Contract entity found with ID (1).");
    }

    @Test
    public void getNonMatchingContractTest() {
        Integer tenantId = 2;
        String name = "name";

        ContractView contractView = new ContractView(tenantId, name);
        Contract contract = contractService.createContract(tenantId, contractView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .get("/rest/tenant/{tenantId}/contract/{id}", 3,
                                                           contract.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: The " +
                                     "tenantId (3) does not match the persistable (name)'s tenantId (2).");
    }

    @Test
    public void deleteContractTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";

        ContractView contractView = new ContractView(tenantId, name);
        Contract contract = contractService.createContract(tenantId, contractView);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/contract/{id}", tenantId, contract.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().string("true"));
    }

    @Test
    public void deleteNonExistentContractTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete("/rest/tenant/{tenantId}/contract/{id}", 1, 0L)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().string("false"));
    }

    @Test
    public void deleteNonMatchingContractTest() {
        Integer tenantId = 2;
        String name = "name";

        ContractView contractView = new ContractView(tenantId, name);
        Contract contract = contractService.createContract(tenantId, contractView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .delete("/rest/tenant/{tenantId}/contract/{id}",
                                                              3, contract.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (3) does not match the persistable (name)'s tenantId (2).");
    }

    @Test
    public void createContractTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        ContractView contractView = new ContractView(tenantId, name, maximumMinutesPerDay, maximumMinutesPerWeek,
                                         maximumMinutesPerMonth, maximumMinutesPerYear);
        String body = (new ObjectMapper()).writeValueAsString(contractView);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/contract/add", tenantId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(tenantId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(name))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerDay").value(maximumMinutesPerDay))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerWeek").value(maximumMinutesPerWeek))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerMonth").value(maximumMinutesPerMonth))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerYear").value(maximumMinutesPerYear));
    }

    @Test
    public void createNonMatchingContractTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";

        ContractView contractView = new ContractView(tenantId, name);
        String body = (new ObjectMapper()).writeValueAsString(contractView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .post("/rest/tenant/{tenantId}/contract/add", 3)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (3) does not match the persistable (name)'s tenantId (2).");
    }

    @Test
    public void updateContractTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        ContractView contractView = new ContractView(tenantId, name);
        Contract contract = contractService.createContract(tenantId, contractView);

        ContractView contractView2 = new ContractView(tenantId, "name2", maximumMinutesPerDay, maximumMinutesPerWeek,
                                          maximumMinutesPerMonth, maximumMinutesPerYear);
        contractView2.setId(contract.getId());
        String body = (new ObjectMapper()).writeValueAsString(contractView2);

        mvc.perform(MockMvcRequestBuilders
                            .put("/rest/tenant/{tenantId}/contract/update", tenantId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(tenantId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("name2"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerDay").value(maximumMinutesPerDay))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerWeek").value(maximumMinutesPerWeek))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerMonth").value(maximumMinutesPerMonth))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerYear").value(maximumMinutesPerYear));
    }

    @Test
    public void updateNonMatchingContractTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";

        ContractView contractView = new ContractView(tenantId, name);
        Contract contract = contractService.createContract(tenantId, contractView);

        ContractView contractView2 = new ContractView(tenantId, "name2");
        String body = (new ObjectMapper()).writeValueAsString(contractView2);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/contract/update", 3)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (3) does not match the persistable (name2)'s tenantId (2).");
    }

    @Test
    public void updateNonExistentContractTest() throws Exception {
        Integer tenantId = 2;
        ContractView contractView = new ContractView(tenantId, "name");
        contractView.setId(-1L);
        String body = (new ObjectMapper()).writeValueAsString(contractView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/contract/update", tenantId)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                                     "Exception: Contract entity with ID (-1) not found.");
    }

    @Test
    public void updateChangeTenantIdContractTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";

        ContractView contractView = new ContractView(tenantId, name);
        Contract contract = contractService.createContract(tenantId, contractView);

        ContractView contractView2 = new ContractView(3, name);
        contractView2.setId(contract.getId());
        String body = (new ObjectMapper()).writeValueAsString(contractView2);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/contract/update", 3)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalState" +
                                     "Exception: Contract entity with tenantId (2) cannot change tenants.");
    }
}
