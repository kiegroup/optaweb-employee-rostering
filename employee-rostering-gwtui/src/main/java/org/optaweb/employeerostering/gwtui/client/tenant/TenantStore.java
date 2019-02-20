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

package org.optaweb.employeerostering.gwtui.client.tenant;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.shared.tenant.Tenant;
import org.optaweb.employeerostering.shared.tenant.TenantRestServiceBuilder;

@ApplicationScoped
public class TenantStore {

    private List<Tenant> tenantList;

    private Tenant current;

    @Inject
    private Event<TenantChange> tenantChangeEvent;

    @Inject
    private Event<TenantsReady> tenantsReadyEvent;

    @Inject
    private Event<NoTenants> noTenantsEvent;

    @Inject
    private EventManager eventManager;

    // @PostConstruct
    public void init() {
        refresh();
        eventManager.subscribeToEventForever(EventManager.Event.DATA_INVALIDATION, this::onAnyDataInvalidation);
    }

    public void setCurrentTenant(final Tenant newTenant) {
        current = newTenant;
        tenantChangeEvent.fire(new TenantChange());
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

    public static class TenantChange {

    }

    public static class TenantsReady {

    }

    public static class NoTenants {

    }

    public void onAnyDataInvalidation(Class<?> dataInvalidated) {
        if (dataInvalidated.equals(Tenant.class)) {
            refresh();
        }
    }

    private void refresh() {
        TenantRestServiceBuilder.getTenantList(FailureShownRestCallback.onSuccess(tenantList -> {
            this.tenantList = tenantList;
            if (tenantList.isEmpty()) {
                current = null;
                noTenantsEvent.fire(new NoTenants());
            } else {
                setCurrentTenant(tenantList.get(0));
                tenantsReadyEvent.fire(new TenantsReady());
            }
        }));
    }
}
