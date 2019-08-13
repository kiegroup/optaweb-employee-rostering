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

package org.optaweb.employeerostering.service.tenant;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.optaweb.employeerostering.domain.tenant.RosterParametrization;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.domain.tenant.view.RosterParametrizationView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/tenant")
@Validated
public class TenantMockController {

    private final TenantService tenantService;

    public TenantMockController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    final Tenant TENANT = createTenant();

    // ************************************************************************
    // Tenant
    // ************************************************************************

    @GetMapping("/")
    public ResponseEntity<List<Tenant>> getTenantList() {
        return new ResponseEntity<>(Arrays.asList(TENANT), HttpStatus.OK);
    }

    @GetMapping("/{id : \\d+}")
    public ResponseEntity<Tenant> getTenant(@PathVariable Integer id) {
        return new ResponseEntity<>(TENANT, HttpStatus.OK);
    }

    private Tenant createTenant() {
        Tenant tenant = new Tenant("mockTenant");
        tenant.setId(1);
        tenant.setVersion(0L);
        return tenant;
    }

    // ************************************************************************
    // RosterParametrization
    // ************************************************************************

    @GetMapping("/{id}")
    public ResponseEntity<RosterParametrization> getRosterParametrization(@PathVariable @Min(0) Integer tenantId) {
        return new ResponseEntity<>(tenantService.getRosterParametrization(tenantId), HttpStatus.OK);
    }

    @PostMapping("/parametrization/update")
    public ResponseEntity<RosterParametrization> updateRosterParametrization(
            @RequestBody @Valid RosterParametrizationView rosterParametrizationView) {
        return new ResponseEntity<>(tenantService.updateRosterParametrization(rosterParametrizationView),
                                    HttpStatus.OK);
    }

    // TODO: Where should this be?
    @GetMapping("/supported/timezones")
    public ResponseEntity<List<ZoneId>> getSupportedTimezones() {
        return new ResponseEntity<>(tenantService.getSupportedTimezones(), HttpStatus.OK);
    }
}
