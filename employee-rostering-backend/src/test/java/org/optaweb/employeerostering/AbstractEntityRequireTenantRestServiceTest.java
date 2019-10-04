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

package org.optaweb.employeerostering;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.optaweb.employeerostering.domain.roster.view.RosterStateView;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.service.tenant.TenantService;
import org.springframework.beans.factory.annotation.Autowired;

public class AbstractEntityRequireTenantRestServiceTest {

    @Autowired
    protected TenantService tenantService;

    protected Integer TENANT_ID;

    /**
     * Create a tenant with timezone UTC
     */
    protected Tenant createTestTenant() {
        return createTestTenant(ZoneOffset.UTC);
    }

    /**
     * Create a tenant with timezone zoneId
     * @param zoneId the timezone for the tenant to be in, not null
     */
    protected Tenant createTestTenant(ZoneId zoneId) {
        return createTestTenant(new RosterStateView(null, 7, LocalDate.of(2000, 1, 1), 7, 24, 0, 7,
                                                    LocalDate.of(1999, 12, 24), zoneId));
    }

    /**
     * Create a tenant with the specified roster state
     * @param rosterStateView the initial roster state for the tenant, not null
     */
    protected Tenant createTestTenant(RosterStateView rosterStateView) {
        rosterStateView.setTenant(new Tenant("TestTenant"));
        Tenant tenant = tenantService.createTenant(rosterStateView);
        TENANT_ID = tenant.getId();
        return tenant;
    }

    protected void deleteTestTenant() {
        tenantService.deleteTenant(TENANT_ID);
        TENANT_ID = null;
    }
}
