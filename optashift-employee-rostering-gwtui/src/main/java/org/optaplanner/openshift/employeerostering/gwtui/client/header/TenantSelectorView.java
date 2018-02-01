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

package org.optaplanner.openshift.employeerostering.gwtui.client.header;

import java.util.List;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.ui.ListBox;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;

@Templated
public class TenantSelectorView implements IsElement {

    @Inject
    @DataField("tenant-select")
    private ListBox tenantSelect; //FIXME: Remove GWT Widget?

    @Inject
    private TenantStore tenantStore;

    private List<Tenant> tenants;

    public void onTenantsReady(final @Observes TenantStore.TenantsReady tenantsReady) {
        tenants = tenantStore.getTenants();
        tenantSelect.clear();
        tenants.forEach(tenant -> tenantSelect.addItem(tenant.getName()));
    }

    @EventHandler("tenant-select")
    public void onTenantChanged(final ChangeEvent e) {
        int tenantIndex = tenantSelect.getSelectedIndex();
        Tenant tenant = tenantIndex < 0 ? null : tenants.get(tenantIndex);
        tenantStore.setCurrentTenant(tenant);
    }
}
