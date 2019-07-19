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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ContractRestControllerTest {

    @Autowired
    private TestRestTemplate contractRestTemplate;

    private String contractPathURI = "http://localhost:8080/rest/tenant/{tenantId}/contract/";

    private ResponseEntity<List<Contract>> getContracts(Integer tenantId) {
        return contractRestTemplate.exchange(contractPathURI, HttpMethod.GET, null, new ParameterizedTypeReference
                <List<Contract>>() {}, tenantId);
    }

    private ResponseEntity<Contract> getContract(Integer tenantId, Long id) {
        return contractRestTemplate.getForEntity(contractPathURI + id, Contract.class, tenantId);
    }

    private void deleteContract(Integer tenantId, Long id) {
        contractRestTemplate.delete(contractPathURI + id, tenantId);
    }

    private ResponseEntity<Contract> addContract(Integer tenantId, Contract contract) {
        return contractRestTemplate.postForEntity(contractPathURI + "add", contract, Contract.class, tenantId);
    }

    private ResponseEntity<Contract> updateContract(Integer tenantId, HttpEntity<Contract> request) {
        return contractRestTemplate.exchange(contractPathURI + "update", HttpMethod.PUT, request, Contract.class,
                                             tenantId);
    }

    @Test
    public void getContractListTest() {
        Integer tenantId = 1;
        Integer tenantId2 = 2;
        String name = "name";
        String name2 = "name2";
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        Contract contract = new Contract(tenantId, name, maximumMinutesPerDay, maximumMinutesPerWeek,
                                         maximumMinutesPerMonth, maximumMinutesPerYear);
        Contract contract2 = new Contract(tenantId, name2, maximumMinutesPerDay, maximumMinutesPerWeek,
                                          maximumMinutesPerMonth, maximumMinutesPerYear);
        Contract contract3 = new Contract(tenantId2, name, maximumMinutesPerDay, maximumMinutesPerWeek,
                                          maximumMinutesPerMonth, maximumMinutesPerYear);

        ResponseEntity<Contract> postResponse = addContract(tenantId, contract);
        ResponseEntity<Contract> postResponse2 = addContract(tenantId, contract2);
        ResponseEntity<Contract> postResponse3 = addContract(tenantId2, contract3);

        ResponseEntity<List<Contract>> response = getContracts(tenantId);
        ResponseEntity<List<Contract>> response2 = getContracts(tenantId2);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(postResponse.getBody());
        assertThat(response.getBody()).contains(postResponse2.getBody());

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).contains(postResponse3.getBody());

        deleteContract(tenantId, postResponse.getBody().getId());
        deleteContract(tenantId, postResponse2.getBody().getId());
        deleteContract(tenantId2, postResponse3.getBody().getId());
    }

    @Test
    public void getContractTest() {
        Integer tenantId = 1;
        String name = "name";
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        Contract contract = new Contract(tenantId, name, maximumMinutesPerDay, maximumMinutesPerWeek,
                                         maximumMinutesPerMonth, maximumMinutesPerYear);

        ResponseEntity<Contract> postResponse = addContract(tenantId, contract);

        ResponseEntity<Contract> response = getContract(tenantId, postResponse.getBody().getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(postResponse.getBody());

        deleteContract(tenantId, postResponse.getBody().getId());
    }

    @Test
    public void deleteContractTest() {
        Integer tenantId = 1;
        String name = "name";
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        Contract contract = new Contract(tenantId, name, maximumMinutesPerDay, maximumMinutesPerWeek,
                                         maximumMinutesPerMonth, maximumMinutesPerYear);

        ResponseEntity<Contract> postResponse = addContract(tenantId, contract);

        deleteContract(tenantId, postResponse.getBody().getId());

        ResponseEntity<List<Contract>> response = getContracts(tenantId);

        assertThat(response.getBody()).isEmpty();
    }

    @Test
    public void createContractTest() {
        Integer tenantId = 1;
        String name = "name";
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        Contract contract = new Contract(tenantId, name, maximumMinutesPerDay, maximumMinutesPerWeek,
                                         maximumMinutesPerMonth, maximumMinutesPerYear);

        ResponseEntity<Contract> postResponse = addContract(tenantId, contract);

        ResponseEntity<Contract> response = getContract(tenantId, postResponse.getBody().getId());

        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(postResponse.getBody()).isEqualTo(response.getBody());

        deleteContract(tenantId, postResponse.getBody().getId());
    }

    @Test
    public void updateContractTest() {
        Integer tenantId = 1;
        String name = "name";
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        Contract contract = new Contract(tenantId, name);

        ResponseEntity<Contract> postResponse = addContract(tenantId, contract);

        Contract contract2 = new Contract(tenantId, "name2", maximumMinutesPerDay, maximumMinutesPerWeek,
                                          maximumMinutesPerMonth, maximumMinutesPerYear);
        contract2.setId(postResponse.getBody().getId());
        HttpEntity<Contract> request = new HttpEntity<>(contract2);

        ResponseEntity<Contract> putResponse = updateContract(tenantId, request);

        ResponseEntity<Contract> response = getContract(tenantId, putResponse.getBody().getId());

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isEqualTo(response.getBody());

        deleteContract(tenantId, putResponse.getBody().getId());
    }
}
