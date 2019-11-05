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

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.view.ContractView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureTestDatabase
public class ContractRestControllerTest extends AbstractEntityRequireTenantRestServiceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String contractPathURI = "http://localhost:8080/rest/tenant/{tenantId}/contract/";

    private ResponseEntity<List<Contract>> getContracts(Integer tenantId) {
        return restTemplate.exchange(contractPathURI, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Contract>>() {
                }, tenantId);
    }

    private ResponseEntity<Contract> getContract(Integer tenantId, Long id) {
        return restTemplate.getForEntity(contractPathURI + id, Contract.class, tenantId);
    }

    private void deleteContract(Integer tenantId, Long id) {
        restTemplate.delete(contractPathURI + id, tenantId);
    }

    private ResponseEntity<Contract> addContract(Integer tenantId, ContractView contractView) {
        return restTemplate.postForEntity(contractPathURI + "add", contractView, Contract.class, tenantId);
    }

    private ResponseEntity<Contract> updateContract(Integer tenantId, ContractView contractView) {
        return restTemplate.postForEntity(contractPathURI + "update", contractView, Contract.class, tenantId);
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
    public void contractCrudTest() {
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        ContractView contractView = new ContractView(TENANT_ID, "contract", maximumMinutesPerDay, maximumMinutesPerWeek,
                maximumMinutesPerMonth, maximumMinutesPerYear);
        ResponseEntity<Contract> postResponse = addContract(TENANT_ID, contractView);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Contract> response = getContract(TENANT_ID, postResponse.getBody().getId());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualToComparingFieldByFieldRecursively(postResponse.getBody());

        ContractView updatedContractView = new ContractView(TENANT_ID, "updatedContract", maximumMinutesPerDay,
                maximumMinutesPerWeek, maximumMinutesPerMonth, maximumMinutesPerYear);
        updatedContractView.setId(postResponse.getBody().getId());
        ResponseEntity<Contract> putResponse = updateContract(TENANT_ID, updatedContractView);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = getContract(TENANT_ID, putResponse.getBody().getId());
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isEqualToComparingFieldByFieldRecursively(response.getBody());

        deleteContract(TENANT_ID, putResponse.getBody().getId());

        ResponseEntity<List<Contract>> getListResponse = getContracts(TENANT_ID);
        assertThat(getListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getListResponse.getBody()).isEmpty();
    }
}
