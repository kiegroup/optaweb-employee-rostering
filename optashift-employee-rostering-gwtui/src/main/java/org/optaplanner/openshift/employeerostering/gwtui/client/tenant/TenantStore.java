/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.gwtui.client.tenant;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import elemental2.promise.Promise;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantConfiguration;
import org.optaplanner.openshift.employeerostering.shared.tenant.TenantRestServiceBuilder;

@ApplicationScoped
public class TenantStore {

    @Inject
    private PromiseUtils promiseUtils;

    private List<Tenant> tenantList;

    private Tenant current;

    private TenantConfiguration currentConfig;

    @Inject
    private Event<TenantChange> tenantChangeEvent;

    @Inject
    private Event<TenantsReady> tenantsReadyEvent;

    // @PostConstruct
    public void init() {
        TenantRestServiceBuilder.getTenantList(FailureShownRestCallback.onSuccess(tenantList -> {
            this.tenantList = tenantList;
            setCurrentTenant(tenantList.get(0)).then((v) -> {
                tenantsReadyEvent.fire(new TenantsReady());
                return promiseUtils.resolve();
            });
            tenantsReadyEvent.fire(new TenantsReady());
        }));
    }

    public Promise<Void> setCurrentTenant(final Tenant newTenant) {
        return promiseUtils.promise((res, rej) -> {
            current = newTenant;
            getTenantConfiguration(current).then(config -> {
                currentConfig = config;
                tenantChangeEvent.fire(new TenantChange());
                return promiseUtils.resolve();
            });
        });
    }

    public Integer getCurrentTenantId() {
        return current.getId();
    }

    public List<Tenant> getTenantList() {
        return tenantList;
    }

    public void updateTenant(Tenant updatedValue) {
        tenantList.set(tenantList.indexOf(updatedValue), updatedValue);
    }

    public Tenant getCurrentTenant() {
        return current;
    }

    public TenantConfiguration getCurrentTenantConfiguration() {
        return currentConfig;
    }

    private Promise<TenantConfiguration> getTenantConfiguration(Tenant tenant) {
        return promiseUtils.promise((res, rej) -> {
            TenantRestServiceBuilder.getTenantConfiguration(tenant.getId(), FailureShownRestCallback.onSuccess(config -> {
                res.onInvoke(config);
            }));
        });
    }

    public static class TenantChange {

    }

    public static class TenantsReady {

    }
}
