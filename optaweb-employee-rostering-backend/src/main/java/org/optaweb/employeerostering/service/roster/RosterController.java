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

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.roster.PublishResult;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.roster.view.AvailabilityRosterView;
import org.optaweb.employeerostering.domain.roster.view.ShiftRosterView;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.service.solver.SolverStatus;
import org.optaweb.employeerostering.service.spot.SpotRepository;
import org.optaweb.employeerostering.util.ShiftRosterXlsxFileIO;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/tenant/{tenantId}/roster")
@CrossOrigin
@Validated
public class RosterController {

    private final RosterService rosterService;
    private final SpotRepository spotRepository;

    public RosterController(RosterService rosterService, SpotRepository spotRepository) {
        this.rosterService = rosterService;
        Assert.notNull(rosterService, "rosterService must not be null.");
        this.spotRepository = spotRepository;
        Assert.notNull(spotRepository, "spotRepository must not be null.");
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
            @RequestParam(name = "p", required = false) Integer pageNumber,
            @RequestParam(name = "n", required = false) Integer numberOfItemsPerPage) {
        return new ResponseEntity<>(rosterService.getCurrentShiftRosterView(tenantId, pageNumber,
                numberOfItemsPerPage), HttpStatus.OK);
    }

    @GetMapping("/shiftRosterView")
    public ResponseEntity<ShiftRosterView> getShiftRosterView(@PathVariable @Min(0) Integer tenantId,
            @RequestParam(name = "p", required = false) Integer pageNumber,
            @RequestParam(name = "n", required = false) Integer numberOfItemsPerPage,
            @RequestParam(name = "startDate") String startDateString,
            @RequestParam(name = "endDate") String endDateString) {
        return new ResponseEntity<>(rosterService.getShiftRosterView(tenantId, pageNumber, numberOfItemsPerPage,
                startDateString, endDateString), HttpStatus.OK);
    }

    // TODO: find out if there a way to pass lists in GET requests
    // TODO naming "for" is too abstract: we might add a sibling rest method that filters on another type than spots too
    @PostMapping("/shiftRosterView/for")
    public ResponseEntity<ShiftRosterView> getShiftRosterViewFor(@PathVariable @Min(0) Integer tenantId,
            @RequestParam(name = "startDate") String startDateString,
            @RequestParam(name = "endDate") String endDateString,
            @RequestBody @Valid List<Spot> spots) {
        return new ResponseEntity<>(rosterService.getShiftRosterViewFor(tenantId, startDateString, endDateString,
                spots), HttpStatus.OK);
    }

    @GetMapping(value = "/shiftRosterView/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> getShiftRosterViewAsExcel(@PathVariable @Min(0) Integer tenantId,
            @RequestParam(name = "startDate") String startDateString,
            @RequestParam(name = "endDate") String endDateString,
            @RequestParam(name = "spotList") String spotListString) {
        Set<Long> spotIdSet = Arrays.stream(spotListString.split(",")).map(Long::parseLong)
                .collect(Collectors.toSet());
        List<Spot> spotList = spotRepository.findAllByTenantId(tenantId, PageRequest.of(0, Integer.MAX_VALUE))
                .stream().filter(s -> spotIdSet.contains(s.getId()))
                .collect(Collectors.toList());

        if (spotList.size() != spotIdSet.size()) {
            return new ResponseEntity<>(new byte[] {},
                    HttpStatus.BAD_REQUEST);
        }
        ShiftRosterView shiftRosterView = rosterService.getShiftRosterViewFor(tenantId, startDateString, endDateString,
                spotList);
        try {
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("Roster-" + startDateString + "--" +
                            endDateString + ".xlsx")
                    .build());
            return new ResponseEntity<>(ShiftRosterXlsxFileIO.getExcelBytesForShiftRoster(shiftRosterView),
                    responseHeaders,
                    HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(new byte[] {},
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ************************************************************************
    // AvailabilityRosterView
    // ************************************************************************

    @GetMapping("/availabilityRosterView/current")
    public ResponseEntity<AvailabilityRosterView> getCurrentAvailabilityRosterView(
            @PathVariable @Min(0) Integer tenantId, @RequestParam(name = "p", required = false) Integer pageNumber,
            @RequestParam(name = "n", required = false) Integer numberOfItemsPerPage) {
        return new ResponseEntity<>(rosterService.getCurrentAvailabilityRosterView(tenantId, pageNumber,
                numberOfItemsPerPage),
                HttpStatus.OK);
    }

    @GetMapping("/availabilityRosterView")
    public ResponseEntity<AvailabilityRosterView> getAvailabilityRosterView(
            @PathVariable @Min(0) Integer tenantId, @RequestParam(name = "p", required = false) Integer pageNumber,
            @RequestParam(name = "n", required = false) Integer numberOfItemsPerPage,
            @RequestParam(name = "startDate") String startDateString,
            @RequestParam(name = "endDate") String endDateString) {
        return new ResponseEntity<>(rosterService.getAvailabilityRosterView(tenantId, pageNumber, numberOfItemsPerPage,
                startDateString, endDateString),
                HttpStatus.OK);
    }

    @PostMapping("/availabilityRosterView/for")
    // TODO naming "for" is too abstract: we might add a sibling rest method that filters on another type than spots too
    public ResponseEntity<AvailabilityRosterView> getAvailabilityRosterViewFor(
            @PathVariable @Min(0) Integer tenantId, @RequestParam(name = "startDate") String startDateString,
            @RequestParam(name = "endDate") String endDateString, @RequestBody @Valid List<Employee> employees) {
        return new ResponseEntity<>(rosterService.getAvailabilityRosterViewFor(tenantId, startDateString,
                endDateString, employees),
                HttpStatus.OK);
    }

    // ************************************************************************
    // Solver
    // ************************************************************************

    @PostMapping("/solve")
    public void solveRoster(@PathVariable @Min(0) Integer tenantId) {
        rosterService.solveRoster(tenantId);
    }

    @PostMapping("/replan")
    public void replanRoster(@PathVariable @Min(0) Integer tenantId) {
        rosterService.replanRoster(tenantId);
    }

    @PostMapping("/terminate")
    public void terminateRosterEarly(@PathVariable @Min(0) Integer tenantId) {
        rosterService.terminateRosterEarly(tenantId);
    }

    @GetMapping("/status")
    public ResponseEntity<SolverStatus> getSolverStatus(@PathVariable @Min(0) Integer tenantId) {
        return new ResponseEntity<>(rosterService.getSolverStatus(tenantId), HttpStatus.OK);
    }

    // ************************************************************************
    // Publish
    // ************************************************************************

    @PostMapping("/provision")
    public void provision(@PathVariable @Min(0) Integer tenantId,
            @RequestParam Integer startRotationOffset, @RequestParam String fromDate,
            @RequestParam String toDate, @RequestBody List<Long> timeBucketIdList) {
        rosterService.provision(tenantId, startRotationOffset, LocalDate.parse(fromDate), LocalDate.parse(toDate),
                timeBucketIdList);
    }

    @PostMapping("/publishAndProvision")
    public ResponseEntity<PublishResult> publishAndProvision(@PathVariable @Min(0) Integer tenantId) {
        return new ResponseEntity<>(rosterService.publishAndProvision(tenantId), HttpStatus.OK);
    }

    @PostMapping("/commitChanges")
    public void commitChanges(@PathVariable @Min(0) Integer tenantId) {
        rosterService.commitChanges(tenantId);
    }
}
