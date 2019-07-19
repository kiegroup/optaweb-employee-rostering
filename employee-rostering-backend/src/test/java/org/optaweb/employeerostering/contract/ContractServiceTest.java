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
import org.optaweb.employeerostering.service.ContractService;
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

    @Test
    public void getContractListTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/contract", 1)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void getContractTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        Contract contract = new Contract(tenantId, name, maximumMinutesPerDay, maximumMinutesPerWeek,
                                         maximumMinutesPerMonth, maximumMinutesPerYear);
        contractService.createContract(tenantId, contract);

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
        Integer tenantId = 1;

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .get("/rest/tenant/{tenantId}/contract/{id}", tenantId, -1L)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                                     "Exception: No Contract entity found with ID (-1).");
    }

    @Test
    public void getNonMatchingContractTest() {
        Integer tenantId = 1;
        String name = "name";

        Contract contract = new Contract(tenantId, name);
        contractService.createContract(tenantId, contract);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .get("/rest/tenant/{tenantId}/contract/{id}", 2,
                                                           contract.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: The " +
                                     "tenantId (2) does not match the persistable (name)'s tenantId (1).");
    }

    @Test
    public void deleteContractTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";

        Contract contract = new Contract(tenantId, name);
        contractService.createContract(tenantId, contract);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/contract/{id}", tenantId, contract.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteNonExistentContractTest() {
        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .delete("/rest/tenant/{tenantId}/contract/{id}", 1, -1L)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                                     "Exception: No Contract entity found with ID (-1).");
    }

    @Test
    public void deleteNonMatchingContractTest() {
        Integer tenantId = 1;
        String name = "name";

        Contract contract = new Contract(tenantId, name);
        contractService.createContract(tenantId, contract);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .delete("/rest/tenant/{tenantId}/contract/{id}",
                                                              2, contract.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (2) does not match the persistable (name)'s tenantId (1).");
    }

    @Test
    public void createContractTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        Contract contract = new Contract(tenantId, name, maximumMinutesPerDay, maximumMinutesPerWeek,
                                         maximumMinutesPerMonth, maximumMinutesPerYear);
        String body = (new ObjectMapper()).writeValueAsString(contract);

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
        Integer tenantId = 1;
        String name = "name";

        Contract contract = new Contract(tenantId, name);
        String body = (new ObjectMapper()).writeValueAsString(contract);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .post("/rest/tenant/{tenantId}/contract/add", 2)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (2) does not match the persistable (name)'s tenantId (1).");
    }

    @Test
    public void updateContractTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        Contract contract = new Contract(tenantId, name);
        contractService.createContract(tenantId, contract);

        Contract contract2 = new Contract(tenantId, "name2", maximumMinutesPerDay, maximumMinutesPerWeek,
                                          maximumMinutesPerMonth, maximumMinutesPerYear);
        contract2.setId(contract.getId());
        String body = (new ObjectMapper()).writeValueAsString(contract2);

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
        Integer tenantId = 1;
        String name = "name";

        Contract contract = new Contract(tenantId, name);
        contractService.createContract(tenantId, contract);

        Contract contract2 = new Contract(tenantId, "name2");
        String body = (new ObjectMapper()).writeValueAsString(contract2);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/contract/update", 2)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (2) does not match the persistable (name2)'s tenantId (1).");
    }

    @Test
    public void updateNonExistentContractTest() throws Exception {
        Contract contract = new Contract(1, "name");
        contract.setId(-1L);
        String body = (new ObjectMapper()).writeValueAsString(contract);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/contract/update", 1)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                                     "Exception: Contract entity with ID (-1) not found.");
    }

    @Test
    public void updateChangeTenantIdContractTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";

        Contract contract = new Contract(tenantId, name);
        contractService.createContract(tenantId, contract);

        Contract contract2 = new Contract(2, name);
        contract2.setId(contract.getId());
        String body = (new ObjectMapper()).writeValueAsString(contract2);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/contract/update", 2)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalState" +
                                     "Exception: Contract entity with tenantId (1) cannot change tenants.");
    }
}
