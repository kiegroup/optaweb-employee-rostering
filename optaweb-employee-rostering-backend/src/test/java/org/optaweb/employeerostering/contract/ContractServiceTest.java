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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.view.ContractView;
import org.optaweb.employeerostering.service.contract.ContractService;
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
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@Transactional
public class ContractServiceTest extends AbstractEntityRequireTenantRestServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ContractServiceTest.class);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ContractService contractService;

    @Before
    public void setup() {
        createTestTenant();
    }

    @After
    public void cleanup() {
        deleteTestTenant();
    }

    @Test
    public void getContractListTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/contract/", TENANT_ID)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void getContractTest() throws Exception {
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        ContractView contractView = new ContractView(TENANT_ID, "contract", maximumMinutesPerDay, maximumMinutesPerWeek,
                                                     maximumMinutesPerMonth, maximumMinutesPerYear);
        Contract contract = contractService.createContract(TENANT_ID, contractView);

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/contract/{id}", TENANT_ID, contract.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("contract"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerDay").value(maximumMinutesPerDay))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerWeek").value(maximumMinutesPerWeek))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerMonth").value(maximumMinutesPerMonth))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerYear").value(maximumMinutesPerYear));
    }

    @Test
    public void getNonExistentContractTest() throws Exception {
        String exceptionMessage = "No Contract entity found with ID (0).";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/contract/{id}", TENANT_ID, 0)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void getNonMatchingContractTest() throws Exception {
        String exceptionMessage = "The tenantId (0) does not match the persistable (contract)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        ContractView contractView = new ContractView(TENANT_ID, "contract");
        Contract contract = contractService.createContract(TENANT_ID, contractView);

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/contract/{id}", 0, contract.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void deleteContractTest() throws Exception {
        ContractView contractView = new ContractView(TENANT_ID, "contract");
        Contract contract = contractService.createContract(TENANT_ID, contractView);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/contract/{id}", TENANT_ID, contract.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("true"));
    }

    @Test
    public void deleteNonExistentContractTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete("/rest/tenant/{tenantId}/contract/{id}", TENANT_ID, 0)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("false"));
    }

    @Test
    public void deleteNonMatchingContractTest() throws Exception {
        String exceptionMessage = "The tenantId (0) does not match the persistable (contract)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        ContractView contractView = new ContractView(TENANT_ID, "contract");
        Contract contract = contractService.createContract(TENANT_ID, contractView);
        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/contract/{id}", 0, contract.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void createContractTest() throws Exception {
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        ContractView contractView = new ContractView(TENANT_ID, "contract", maximumMinutesPerDay, maximumMinutesPerWeek,
                                                     maximumMinutesPerMonth, maximumMinutesPerYear);
        String body = (new ObjectMapper()).writeValueAsString(contractView);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/contract/add", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("contract"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerDay").value(maximumMinutesPerDay))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerWeek").value(maximumMinutesPerWeek))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerMonth").value(maximumMinutesPerMonth))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerYear").value(maximumMinutesPerYear));
    }

    @Test
    public void createNonMatchingContractTest() throws Exception {
        String exceptionMessage = "The tenantId (0) does not match the persistable (contract)'s tenantId ("
                + TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        ContractView contractView = new ContractView(TENANT_ID, "contract");
        String body = (new ObjectMapper()).writeValueAsString(contractView);
        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/contract/add", 0)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void updateContractTest() throws Exception {
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        ContractView contractView = new ContractView(TENANT_ID, "contract");
        Contract contract = contractService.createContract(TENANT_ID, contractView);

        ContractView updatedContractView = new ContractView(TENANT_ID, "updatedContract", maximumMinutesPerDay,
                                                            maximumMinutesPerWeek,
                                                            maximumMinutesPerMonth, maximumMinutesPerYear);
        updatedContractView.setId(contract.getId());
        String body = (new ObjectMapper()).writeValueAsString(updatedContractView);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/contract/update", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("updatedContract"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerDay").value(maximumMinutesPerDay))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerWeek").value(maximumMinutesPerWeek))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerMonth").value(maximumMinutesPerMonth))
                .andExpect(MockMvcResultMatchers.jsonPath("$.maximumMinutesPerYear").value(maximumMinutesPerYear));
    }

    @Test
    public void updateNonMatchingContractTest() throws Exception {
        String exceptionMessage = "The tenantId (0) does not match the persistable (updatedContract)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        ContractView contractView = new ContractView(TENANT_ID, "contract");
        contractService.createContract(TENANT_ID, contractView);

        ContractView updatedContractView = new ContractView(TENANT_ID, "updatedContract");
        String body = (new ObjectMapper()).writeValueAsString(updatedContractView);
        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/contract/update", 0)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void updateNonExistentContractTest() throws Exception {
        String exceptionMessage = "Contract entity with ID (0) not found.";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        ContractView contractView = new ContractView(TENANT_ID, "contract");
        contractView.setId(0L);
        String body = (new ObjectMapper()).writeValueAsString(contractView);
        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/contract/update", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void updateChangeTenantIdContractTest() throws Exception {
        String exceptionMessage = "Contract entity with tenantId (" + TENANT_ID + ") cannot change tenants.";
        String exceptionClass = "java.lang.IllegalStateException";

        ContractView contractView = new ContractView(TENANT_ID, "contract");
        Contract contract = contractService.createContract(TENANT_ID, contractView);

        ContractView updatedContract = new ContractView(0, "updatedContract");
        updatedContract.setId(contract.getId());
        String body = (new ObjectMapper()).writeValueAsString(updatedContract);
        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/contract/update", 0)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void getOrCreateDefaultContractNotExistsTest() {
        Contract contract = contractService.getOrCreateDefaultContract(TENANT_ID);
        assertEquals(contract.getName(), "Default Contract");
        assertNull(contract.getMaximumMinutesPerDay());
        assertNull(contract.getMaximumMinutesPerWeek());
        assertNull(contract.getMaximumMinutesPerMonth());
        assertNull(contract.getMaximumMinutesPerYear());
    }

    @Test
    public void getOrCreateDefaultContractExistsTest() {
        ContractView contractView = new ContractView();
        contractView.setTenantId(TENANT_ID);
        contractView.setName("Default Contract");
        contractView.setMaximumMinutesPerDay(10);

        Contract contract = contractService.createContract(TENANT_ID, contractView);
        Contract defaultContract = contractService.getOrCreateDefaultContract(TENANT_ID);

        assertEquals(contract, defaultContract);
    }
}
