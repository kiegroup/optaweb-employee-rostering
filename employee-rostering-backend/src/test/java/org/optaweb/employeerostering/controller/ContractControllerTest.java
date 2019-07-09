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

import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.domain.Contract;
import org.optaweb.employeerostering.service.ContractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ContractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(ContractControllerTest.class);

    @Autowired
    private ContractService contractService;

    @Test
    public void getContractListTest() {
        Integer tenantId = 1;
        Integer tenantId2 = 2;
        String name = "name";
        String name2 = "name2";

        Contract contract = new Contract(tenantId, name);
        Contract contract2 = new Contract(tenantId, name2);
        Contract contract3 = new Contract(tenantId2, name);

        contractService.createContract(tenantId, contract);
        contractService.createContract(tenantId, contract2);
        contractService.createContract(tenantId2, contract3);

        List<Contract> contractList = contractService.getContractList(tenantId);
        List<Contract> contractList2 = contractService.getContractList(tenantId2);

        assertEquals(contractList.get(0), contract);
        assertEquals(contractList.get(1), contract2);
        assertEquals(contractList2.get(0), contract3);
    }

    @Test
    public void getContractTest() {
        Integer tenantId = 1;
        String name = "name";

        Contract contract = new Contract(tenantId, name);

        contractService.createContract(tenantId, contract);

        Contract returnContract = contractService.getContract(tenantId, contract.getId());

        assertEquals(returnContract, contract);
    }

    @Test
    public void getNonExistentContractTest() {
        Integer tenantId = 1;
        String name = "name";

        Contract contract = new Contract(tenantId, name);

        contractService.createContract(tenantId, contract);

        try {
            contractService.getContract(1, -1L);
        } catch (EntityNotFoundException e) {
            String expectedMessage = "No Contract entity found with ID (-1).";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void getNonMatchingContractTest() {
        Integer tenantId = 1;
        String name = "name";

        Contract contract = new Contract(tenantId, name);

        contractService.createContract(tenantId, contract);

        try {
            contractService.getContract(2, contract.getId());
        } catch (IllegalStateException e) {
            String expectedMessage = "The tenantId (2) does not match the persistable (name)'s tenantId (1).";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void deleteContractTest() {
        Integer tenantId = 1;
        String name = "name";

        Contract contract = new Contract(tenantId, name);

        contractService.createContract(tenantId, contract);

        assertEquals(contractService.deleteContract(tenantId, contract.getId()), true);
    }

    @Test
    public void deleteNonExistentContractTest() {
        try {
            contractService.deleteContract(1, -1L);
        } catch (EntityNotFoundException e) {
            String expectedMessage = "No Contract entity found with ID (-1).";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void deleteNonMatchingContractTest() {
        Integer tenantId = 1;
        String name = "name";

        Contract contract = new Contract(tenantId, name);

        contractService.createContract(tenantId, contract);

        try {
            contractService.deleteContract(2, contract.getId());
        } catch (IllegalStateException e) {
            String expectedMessage = "The tenantId (2) does not match the persistable (name)'s tenantId (1).";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void createContractTest() {
        Integer tenantId = 1;
        String name = "name";

        Contract contract = new Contract(tenantId, name);

        Contract returnContract = contractService.createContract(tenantId, contract);

        assertEquals(returnContract, contract);
    }

    @Test
    public void createNonMatchingContractTest(){
        Integer tenantId = 1;
        String name = "name";

        Contract contract = new Contract(tenantId, name);

        try {
            contractService.createContract(2, contract);
        } catch (IllegalStateException e) {
            String expectedMessage = "The tenantId (2) does not match the persistable (name)'s tenantId (1).";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void updateContractTest() {
        Integer tenantId = 1;
        String name = "name";

        Contract contract = new Contract(tenantId, name);

        contractService.createContract(tenantId, contract);

        Contract contract2 = new Contract(tenantId, "name2");
        contract2.setId(contract.getId());

        Contract returnContract = contractService.updateContract(tenantId, contract2);

        assertEquals(returnContract, contract2);
    }

    @Test
    public void updateNonMatchingContractTest(){
        Integer tenantId = 1;
        String name = "name";

        Contract contract = new Contract(tenantId, name);

        contractService.createContract(tenantId, contract);

        Contract contract2 = new Contract(tenantId, "name2");

        try {
            contractService.updateContract(2, contract2);
        } catch (IllegalStateException e) {
            String expectedMessage = "The tenantId (2) does not match the persistable (name2)'s tenantId (1).";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void updateNonExistentContractTest() {
        Contract contract = new Contract(1, "name");
        contract.setId(-1L);

        try {
            contractService.updateContract(1, contract);
        } catch (EntityNotFoundException e) {
            String expectedMessage = "Contract entity with ID (-1) not found.";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void updateChangeTenantIdContractTest() {
        Integer tenantId = 1;
        String name = "name";

        Contract contract = new Contract(tenantId, name);

        contractService.createContract(tenantId, contract);

        Contract contract2 = new Contract(2, name);
        contract2.setId(contract.getId());

        try {
            contractService.updateContract(2, contract2);
        } catch (IllegalStateException e) {
            String expectedMessage = "Contract entity with tenantId (1) cannot change tenants.";
            assertEquals(expectedMessage, e.getMessage());
        }
    }
}
