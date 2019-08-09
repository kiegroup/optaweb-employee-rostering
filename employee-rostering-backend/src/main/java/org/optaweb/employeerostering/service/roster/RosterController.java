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

package org.optaweb.employeerostering.service.roster;

import javax.validation.constraints.Min;

import org.optaweb.employeerostering.domain.roster.RosterState;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/tenant/{tenantId}/roster")
@Validated
public class RosterController {

    private final RosterService rosterService;

    public RosterController(RosterService rosterService) {
        this.rosterService = rosterService;
        Assert.notNull(rosterService, "rosterService must not be null.");
    }

    // ************************************************************************
    // RosterState
    // ************************************************************************

    @GetMapping("/{id}")
    public ResponseEntity<RosterState> getRosterState(@PathVariable @Min(0) Integer tenantId) {
        return new ResponseEntity<>(rosterService.getRosterState(tenantId), HttpStatus.OK);
    }

    // ************************************************************************
    // ShiftRosterView
    // ************************************************************************

    // TODO: Add getShiftRosterView() methods once SolverManager and IndictmentUtils are added

    // ************************************************************************
    // AvailabilityRosterView
    // ************************************************************************

    // TODO: Add getAvailabilityRosterView() methods once SolverManager and IndictmentUtils are added

}
