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

package org.optaweb.employeerostering.gwtui.client.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import elemental2.promise.Promise;
import org.jboss.errai.databinding.client.api.Converter;
import org.optaweb.employeerostering.gwtui.client.common.EventManager.Event;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaweb.employeerostering.shared.contract.Contract;
import org.optaweb.employeerostering.shared.contract.ContractRestServiceBuilder;

@Singleton
public class StringToContractConverter implements Converter<Contract, String> {

    @Inject
    private TenantStore tenantStore;

    @Inject
    private PromiseUtils promiseUtils;

    @Inject
    private EventManager eventManager;

    private Map<String, Contract> contractMap;

    @PostConstruct
    private void init() {
        contractMap = new HashMap<>();
        updateContactMappings(Collections.emptyList());
        eventManager.subscribeToEventForever(Event.DATA_INVALIDATION, this::onContractListInvalidation);
    }

    @SuppressWarnings("unused")
    private void onTenantChanged(@Observes TenantStore.TenantChange event) {
        fetchContractListAndUpdateContractMapping();
    }

    @SuppressWarnings("unused")
    private void onContractListInvalidation(Class<?> dataInvalidated) {
        if (dataInvalidated.equals(Contract.class)) {
            fetchContractListAndUpdateContractMapping();
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Class getModelType() {
        return Set.class;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Class getComponentType() {
        return List.class;
    }

    @Override
    public Contract toModelValue(String componentValue) {
        return contractMap.get(componentValue);
    }

    @Override
    public String toWidgetValue(Contract modelValue) {
        if (null == modelValue) {
            return null;
        }
        return modelValue.getName();
    }

    public Map<String, Contract> getContractMap() {
        return contractMap;
    }

    private void updateContactMappings(List<Contract> contractList) {
        contractMap.clear();
        for (Contract contract : contractList) {
            contractMap.put(contract.getName(), contract);
        }
        eventManager.fireEvent(Event.CONTRACT_MAP_INVALIDATION, contractMap);
    }

    private Promise<List<Contract>> getContractList() {
        return new Promise<>((resolve, reject) -> ContractRestServiceBuilder.getContractList(tenantStore.getCurrentTenantId(), FailureShownRestCallback
                .onSuccess(resolve::onInvoke)));
    }

    private void fetchContractListAndUpdateContractMapping() {
        getContractList().then(contractList -> {
            updateContactMappings(contractList);
            return promiseUtils.resolve();
        });
    }
}
