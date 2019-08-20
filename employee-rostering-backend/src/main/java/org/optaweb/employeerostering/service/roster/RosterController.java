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

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.roster.view.ShiftRosterView;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/shiftRosterView/current")
    public ResponseEntity<ShiftRosterView> getCurrentShiftRosterView(@PathVariable @Min(0) Integer tenantId,
                                                                     @RequestParam(name = "p", required = false)
                                                                             Integer pageNumber,
                                                                     @RequestParam(name = "n", required = false)
                                                                             Integer numberOfItemsPerPage) {
        return new ResponseEntity<>(rosterService.getCurrentShiftRosterView(tenantId, pageNumber,
                                                                            numberOfItemsPerPage), HttpStatus.OK);
    }

    @GetMapping("/shiftRosterView")
    public ResponseEntity<ShiftRosterView> getShiftRosterView(@PathVariable @Min(0) Integer tenantId,
                                                              @RequestParam(name = "p", required = false)
                                                                      Integer pageNumber,
                                                              @RequestParam(name = "n", required = false)
                                                                      Integer numberOfItemsPerPage,
                                                              @RequestParam(name = "startDate") String startDateString,
                                                              @RequestParam(name = "endDate") String endDateString) {
        return new ResponseEntity<>(rosterService.getShiftRosterView(tenantId, pageNumber, numberOfItemsPerPage,
                                                                     startDateString, endDateString), HttpStatus.OK);
    }

    // TODO: find out if there a way to pass lists in GET requests
    // TODO naming "for" is too abstract: we might add a sibling rest method that filters on another type than spots too
    @PostMapping("/shiftRosterView/for")
    public ResponseEntity<ShiftRosterView> getShiftRosterViewFor(@PathVariable @Min(0) Integer tenantId,
                                                                 @RequestParam(name = "startDate")
                                                                         String startDateString,
                                                                 @RequestParam(name = "endDate") String endDateString,
                                                                 @RequestBody @Valid List<Spot> spots) {
        return new ResponseEntity<>(rosterService.getShiftRosterViewFor(tenantId, startDateString, endDateString,
                                                                        spots), HttpStatus.OK);
    }

    // ************************************************************************
    // AvailabilityRosterView
    // ************************************************************************

    // TODO: Add getAvailabilityRosterView() methods once SolverManager and IndictmentUtils are added

    // ************************************************************************
    // Solver methods
    // ************************************************************************

    // ************************************************************************
    // Publishing/Provisioning methods
    // ************************************************************************

    // TODO: Implement publishAndProvision()
}
