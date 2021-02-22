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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.view.ContractView;

@Path("/rest/tenant/{tenantId}/contract")
@ApplicationScoped
@Tag(name = "Contract")
public class ContractController {

    private final ContractService contractService;

    @Inject
    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @Operation(summary = "List Contracts", description = "Get a list of all contracts")
    @GET
    @Path("/")
    public List<Contract> getContractList(@PathParam("tenantId") @Min(0) Integer tenantId) {
        return contractService.getContractList(tenantId);
    }

    @Operation(summary = "Get Contract", description = "Get a contract by id")
    @GET
    @Path("/{id}")
    public Contract getContract(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return contractService.getContract(tenantId, id);
    }

    @Operation(summary = "Delete Contract", description = "Deletes a contract")
    @DELETE
    @Path("/{id}")
    public Boolean deleteContract(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return contractService.deleteContract(tenantId, id);
    }

    @Operation(summary = "Add Contract", description = "Adds a contract")
    @POST
    @Path("/add")
    public Contract createContract(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid ContractView contractView) {
        return contractService.createContract(tenantId, contractView);
    }

    @Operation(summary = "Update Contract", description = "Updates a contract")
    @POST
    @Path("/update")
    public Contract updateContract(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid ContractView contractView) {
        return contractService.updateContract(tenantId, contractView);
    }
}
