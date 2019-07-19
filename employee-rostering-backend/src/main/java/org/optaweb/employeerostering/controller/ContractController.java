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

import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.ContractDTO;
import org.optaweb.employeerostering.service.ContractService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/tenant/{tenantId}/contract")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
        Assert.notNull(contractService, "contractService must not be null.");
    }

    @GetMapping
    public ResponseEntity<List<Contract>> getContractList(@PathVariable Integer tenantId) {
        return new ResponseEntity<>(contractService.getContractList(tenantId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contract> getContract(@PathVariable Integer tenantId, @PathVariable Long id) {
        return new ResponseEntity<>(contractService.getContract(tenantId, id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteContract(@PathVariable Integer tenantId, @PathVariable Long id) {
        return new ResponseEntity<>(contractService.deleteContract(tenantId, id), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Contract> createContract(@PathVariable Integer tenantId,
                                                   @RequestBody ContractDTO contractDTO) {
        Contract contract = new Contract(contractDTO.getTenantId(), contractDTO.getName(),
                                         contractDTO.getMaximumMinutesPerDay(), contractDTO.getMaximumMinutesPerWeek(),
                                         contractDTO.getMaximumMinutesPerMonth(),
                                         contractDTO.getMaximumMinutesPerYear());
        contract.setId(contractDTO.getId());
        contract.setVersion(contractDTO.getVersion());
        return new ResponseEntity<>(contractService.createContract(tenantId, contract), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<Contract> updateContract(@PathVariable Integer tenantId,
                                                   @RequestBody ContractDTO contractDTO) {
        Contract contract = new Contract(contractDTO.getTenantId(), contractDTO.getName(),
                                         contractDTO.getMaximumMinutesPerDay(), contractDTO.getMaximumMinutesPerWeek(),
                                         contractDTO.getMaximumMinutesPerMonth(),
                                         contractDTO.getMaximumMinutesPerYear());
        contract.setId(contractDTO.getId());
        contract.setVersion(contractDTO.getVersion());
        return new ResponseEntity<>(contractService.updateContract(tenantId, contract), HttpStatus.OK);
    }
}
