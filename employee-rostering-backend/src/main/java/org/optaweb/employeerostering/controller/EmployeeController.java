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

package org.optaweb.employeerostering.controller;

import java.util.List;

import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeDTO;
import org.optaweb.employeerostering.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/tenant/{tenantId}/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
        Assert.notNull(employeeService, "employeeService must not be null.");
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getEmployeeList(@PathVariable Integer tenantId) {
        return new ResponseEntity<>(employeeService.getEmployeeList(tenantId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployee(@PathVariable Integer tenantId, @PathVariable Long id) {
        return new ResponseEntity<>(employeeService.getEmployee(tenantId, id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteEmployee(@PathVariable Integer tenantId, @PathVariable Long id) {
        return new ResponseEntity<>(employeeService.deleteEmployee(tenantId, id), HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Employee> createEmployee(@PathVariable Integer tenantId,
                                                   @RequestBody EmployeeDTO employeeDTO) {
        Employee employee = new Employee(employeeDTO.getTenantId(), employeeDTO.getName(), employeeDTO.getContract(),
                                         employeeDTO.getSkillProficiencySet());
        employee.setId(employeeDTO.getId());
        employee.setVersion(employeeDTO.getVersion());
        return new ResponseEntity<>(employeeService.createEmployee(tenantId, employee), HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Integer tenantId,
                                                   @RequestBody EmployeeDTO employeeDTO) {
        Employee employee = new Employee(employeeDTO.getTenantId(), employeeDTO.getName(), employeeDTO.getContract(),
                                         employeeDTO.getSkillProficiencySet());
        employee.setId(employeeDTO.getId());
        employee.setVersion(employeeDTO.getVersion());
        return new ResponseEntity<>(employeeService.updateEmployee(tenantId, employee), HttpStatus.OK);
    }

    //TODO: Add EmployeeAvailability CRUD handlers
}
