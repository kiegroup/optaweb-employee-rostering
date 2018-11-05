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

package org.optaweb.employeerostering.webapp.contract;

import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.optaweb.employeerostering.shared.contract.Contract;
import org.optaweb.employeerostering.shared.contract.ContractRestService;
import org.optaweb.employeerostering.webapp.AbstractEntityRequireTenantRestServiceIT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ContractRestServiceIT extends AbstractEntityRequireTenantRestServiceIT {

    private ContractRestService contractRestService;

    public ContractRestServiceIT() {
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

    @Test
    public void testDeleteNonExistingContract() {
        final long nonExistingContractId = 123456L;
        boolean result = contractRestService.removeContract(TENANT_ID, nonExistingContractId);
        assertThat(result).isFalse();
        assertClientResponseOk();
    }

    @Test
    public void testUpdateNonExistingContract() {
        final long nonExistingContractId = 123456L;
        Contract nonExistingContract = new Contract(TENANT_ID, "Non-existing contract");
        nonExistingContract.setId(nonExistingContractId);
        Contract updatedContract = contractRestService.updateContract(TENANT_ID, nonExistingContract);

        assertClientResponseOk();
        assertThat(updatedContract.getName()).isEqualTo(nonExistingContract.getName());
        assertThat(updatedContract.getId()).isNotNull().isNotEqualTo(nonExistingContract);
    }

    @Test
    public void testGetOfNonExistingContract() {
        final long nonExistingContractId = 123456L;
        assertThatExceptionOfType(javax.ws.rs.NotFoundException.class)
                .isThrownBy(() -> contractRestService.getContract(TENANT_ID, nonExistingContractId));
        assertClientResponseError(Response.Status.NOT_FOUND);
    }

    @Test
    public void testInvalidAdd() {
        Contract invalidContract = new Contract(TENANT_ID, "invalid contract", -1, null, null, null);
        assertThatExceptionOfType(javax.ws.rs.InternalServerErrorException.class)
                .isThrownBy(() -> {
                    contractRestService.updateContract(TENANT_ID, invalidContract);
                });
        assertClientResponseError(Response.Status.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testCrudContract() {
        Contract testAddContract = new Contract(TENANT_ID, "A", null, 8, null, null);
        contractRestService.addContract(TENANT_ID, testAddContract);
        assertClientResponseOk();

        List<Contract> contracts = contractRestService.getContractList(TENANT_ID);
        assertClientResponseOk();
        assertThat(contracts).usingElementComparatorIgnoringFields(IGNORED_FIELDS).containsExactly(testAddContract);

        Contract testUpdateContract = contracts.get(0);
        testUpdateContract.setName("B");
        testUpdateContract.setMaximumMinutesPerYear(100);
        contractRestService.updateContract(TENANT_ID, testUpdateContract);

        Contract retrievedContract = contractRestService.getContract(TENANT_ID, testUpdateContract.getId());
        assertClientResponseOk();
        assertThat(retrievedContract).isNotNull().isEqualToIgnoringGivenFields(testUpdateContract, "version");

        boolean result = contractRestService.removeContract(TENANT_ID, retrievedContract.getId());
        assertThat(result).isTrue();
        assertClientResponseOk();

        contracts = contractRestService.getContractList(TENANT_ID);
        assertThat(contracts).isEmpty();
    }
}
