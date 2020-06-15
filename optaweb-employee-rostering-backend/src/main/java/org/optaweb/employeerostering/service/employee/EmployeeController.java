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

package org.optaweb.employeerostering.service.employee;

import java.io.IOException;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.domain.employee.view.EmployeeView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/rest/tenant/{tenantId}/employee")
@CrossOrigin
@Validated
@Api(tags = "Employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
        Assert.notNull(employeeService, "employeeService must not be null.");
    }

    // ************************************************************************
    // Employee
    // ************************************************************************

    @ApiOperation("Get a list of all employees")
    @GetMapping("/")
    public ResponseEntity<List<Employee>> getEmployeeList(@PathVariable @Min(0) Integer tenantId) {
        return new ResponseEntity<>(employeeService.getEmployeeList(tenantId), HttpStatus.OK);
    }

    @ApiOperation("Get an employee by id")
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable @Min(0) Integer tenantId, @PathVariable @Min(0) Long id) {
        return new ResponseEntity<>(employeeService.getEmployee(tenantId, id), HttpStatus.OK);
    }

    @ApiOperation("Delete an employee")
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteEmployee(@PathVariable @Min(0) Integer tenantId,
                                                  @PathVariable @Min(0) Long id) {
        return new ResponseEntity<>(employeeService.deleteEmployee(tenantId, id), HttpStatus.OK);
    }

    @ApiOperation("Add a new employee")
    @PostMapping("/add")
    public ResponseEntity<Employee> createEmployee(@PathVariable @Min(0) Integer tenantId,
                                                   @RequestBody @Valid EmployeeView employeeView) {
        return new ResponseEntity<>(employeeService.createEmployee(tenantId, employeeView), HttpStatus.OK);
    }

    @ApiOperation("Import employees from an Excel file")
    @PostMapping("/import")
    public ResponseEntity<List<Employee>> addEmployeesFromExcelFile(@PathVariable @Min(0) Integer tenantId,
                                                                    @RequestParam("file") MultipartFile excelDataFile)
            throws IOException {

        return new ResponseEntity<>(employeeService.importEmployeesFromExcel(tenantId, excelDataFile.getInputStream()),
                                    HttpStatus.OK);
    }

    @ApiOperation("Update an employee")
    @PostMapping("/update")
    public ResponseEntity<Employee> updateEmployee(@PathVariable @Min(0) Integer tenantId,
                                                   @RequestBody @Valid EmployeeView employeeView) {
        return new ResponseEntity<>(employeeService.updateEmployee(tenantId, employeeView), HttpStatus.OK);
    }

    // ************************************************************************
    // EmployeeAvailability
    // ************************************************************************

    @ApiOperation("Get an employee availability by id")
    @GetMapping("/availability/{id}")
    public ResponseEntity<EmployeeAvailabilityView> getEmployeeAvailability(@PathVariable @Min(0) Integer tenantId,
                                                                            @PathVariable @Min(0) Long id) {
        return new ResponseEntity<>(employeeService.getEmployeeAvailability(tenantId, id), HttpStatus.OK);
    }

    @ApiOperation("Add a new employee availability")
    @PostMapping("/availability/add")
    public ResponseEntity<EmployeeAvailabilityView> createEmployeeAvailability(@PathVariable @Min(0) Integer tenantId,
                                                                               @RequestBody @Valid
                                                                                       EmployeeAvailabilityView
                                                                                       employeeAvailabilityView) {
        return new ResponseEntity<>(employeeService.createEmployeeAvailability(tenantId, employeeAvailabilityView),
                                    HttpStatus.OK);
    }

    @ApiOperation("Update an employee availability")
    @PutMapping("/availability/update")
    public ResponseEntity<EmployeeAvailabilityView> updateEmployeeAvailability(@PathVariable @Min(0) Integer tenantId,
                                                                               @RequestBody @Valid
                                                                                       EmployeeAvailabilityView
                                                                                       employeeAvailabilityView) {
        return new ResponseEntity<>(employeeService.updateEmployeeAvailability(tenantId, employeeAvailabilityView),
                                    HttpStatus.OK);
    }

    @ApiOperation("Delete an employee availability")
    @DeleteMapping("/availability/{id}")
    public ResponseEntity<Boolean> deleteEmployeeAvailability(@PathVariable @Min(0) Integer tenantId,
                                                              @PathVariable @Min(0) Long id) {
        return new ResponseEntity<>(employeeService.deleteEmployeeAvailability(tenantId, id), HttpStatus.OK);
    }
}
