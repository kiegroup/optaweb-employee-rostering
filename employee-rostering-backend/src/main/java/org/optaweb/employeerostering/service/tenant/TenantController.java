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
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.optaweb.employeerostering.domain.roster.view.RosterStateView;
import org.optaweb.employeerostering.domain.tenant.RosterParametrization;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.domain.tenant.view.RosterParametrizationView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/tenant")
@Validated
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    // ************************************************************************
    // Tenant
    // ************************************************************************

    @GetMapping("/")
    public ResponseEntity<List<Tenant>> getTenantList() {
        return new ResponseEntity<>(tenantService.getTenantList(), HttpStatus.OK);
    }

    @GetMapping("/{id : \\d+}")
    public ResponseEntity<Tenant> getTenant(@PathVariable @Min(0) Integer id) {
        return new ResponseEntity<>(tenantService.getTenant(id), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Tenant> createTenant(@RequestBody @Valid RosterStateView initialRosterStateView) {
        return new ResponseEntity<>(tenantService.createTenant(initialRosterStateView), HttpStatus.OK);
    }

    @DeleteMapping("/remove/{id}")
    public ResponseEntity<Boolean> deleteTenant(@PathVariable @Min(0) Integer id) {
        return new ResponseEntity<>(tenantService.deleteTenant(id), HttpStatus.OK);
    }

    // ************************************************************************
    // RosterParametrization
    // ************************************************************************

    @GetMapping("/{id}")
    public ResponseEntity<RosterParametrization> getRosterParametrization(@PathVariable @Min(0) Integer id) {
        return new ResponseEntity<>(tenantService.getRosterParametrization(id), HttpStatus.OK);
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
