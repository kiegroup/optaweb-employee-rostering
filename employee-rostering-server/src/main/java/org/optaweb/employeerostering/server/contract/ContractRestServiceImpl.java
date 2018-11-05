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

package org.optaweb.employeerostering.server.contract;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.optaweb.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaweb.employeerostering.shared.contract.Contract;
import org.optaweb.employeerostering.shared.contract.ContractRestService;

public class ContractRestServiceImpl extends AbstractRestServiceImpl
        implements
        ContractRestService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Contract> getContractList(Integer tenantId) {
        return entityManager.createNamedQuery("Contract.findAll", Contract.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
    }

    @Override
    public Contract getContract(Integer tenantId, Long id) {
        Contract contract = entityManager.find(Contract.class, id);
        if (contract == null) {
            throw new EntityNotFoundException("No Contract entity found with ID (" + id + ").");
        }
        validateTenantIdParameter(tenantId, contract);
        return contract;
    }

    @Override
    @Transactional
    public Contract addContract(Integer tenantId, Contract contract) {
        validateTenantIdParameter(tenantId, contract);
        entityManager.persist(contract);
        return contract;
    }

    @Override
    @Transactional
    public Contract updateContract(Integer tenantId, Contract contract) {
        validateTenantIdParameter(tenantId, contract);
        contract = entityManager.merge(contract);
        return contract;
    }

    @Override
    @Transactional
    public Boolean removeContract(Integer tenantId, Long id) {
        Contract contract = entityManager.find(Contract.class, id);
        if (contract == null) {
            return false;
        }
        validateTenantIdParameter(tenantId, contract);
        entityManager.remove(contract);
        return true;
    }
}
