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

package org.optaweb.employeerostering.service.contract;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.view.ContractView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/tenant/{tenantId}/contract")
@CrossOrigin
@Validated
@Api(tags = "Contract")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
        Assert.notNull(contractService, "contractService must not be null.");
    }

    @ApiOperation("Get a list of all contracts")
    @GetMapping("/")
    public ResponseEntity<List<Contract>> getContractList(@PathVariable @Min(0) Integer tenantId) {
        return new ResponseEntity<>(contractService.getContractList(tenantId), HttpStatus.OK);
    }

    @ApiOperation("Get a contract by id")
    @GetMapping("/{id}")
    public ResponseEntity<Contract> getContract(@PathVariable @Min(0) Integer tenantId,
                                                @PathVariable @Min(0) Long id) {
        return new ResponseEntity<>(contractService.getContract(tenantId, id), HttpStatus.OK);
    }

    @ApiOperation("Delete a contract")
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteContract(@PathVariable @Min(0) Integer tenantId,
                                                  @PathVariable @Min(0) Long id) {
        return new ResponseEntity<>(contractService.deleteContract(tenantId, id), HttpStatus.OK);
    }

    @ApiOperation("Add a new contract")
    @PostMapping("/add")
    public ResponseEntity<Contract> createContract(@PathVariable @Min(0) Integer tenantId,
                                                   @RequestBody @Valid ContractView contractView) {
        return new ResponseEntity<>(contractService.createContract(tenantId, contractView), HttpStatus.OK);
    }

    @ApiOperation("Update a contract")
    @PostMapping("/update")
    public ResponseEntity<Contract> updateContract(@PathVariable @Min(0) Integer tenantId,
                                                   @RequestBody @Valid ContractView contractView) {
        return new ResponseEntity<>(contractService.updateContract(tenantId, contractView), HttpStatus.OK);
    }
}
