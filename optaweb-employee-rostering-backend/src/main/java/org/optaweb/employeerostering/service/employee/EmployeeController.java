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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.optaweb.employeerostering.domain.common.MultipartBody;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.domain.employee.view.EmployeeView;

@Path("/rest/tenant/{tenantId}/employee")
// @Api(tags = "Employee")
@ApplicationScoped
public class EmployeeController {

    private final EmployeeService employeeService;

    @Inject
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // ************************************************************************
    // Employee
    // ************************************************************************

    // @ApiOperation("Get a list of all employees")
    @GET
    @Path("/")
    public List<Employee> getEmployeeList(@PathParam("tenantId") @Min(0) Integer tenantId) {
        return employeeService.getEmployeeList(tenantId);
    }

    // @ApiOperation("Get an employee by id")
    @GET
    @Path("/{id}")
    public Employee getEmployee(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return employeeService.getEmployee(tenantId, id);
    }

    // @ApiOperation("Delete an employee")
    @DELETE
    @Path("/{id}")
    public Boolean deleteEmployee(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return employeeService.deleteEmployee(tenantId, id);
    }

    // @ApiOperation("Add a new employee")
    @POST
    @Path("/add")
    public Employee createEmployee(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid EmployeeView employeeView) {
        return employeeService.createEmployee(tenantId, employeeView);
    }

    // @ApiOperation("Import employees from an Excel file")
    @POST
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Employee> addEmployeesFromExcelFile(@PathParam("tenantId") @Min(0) Integer tenantId,
            @MultipartForm MultipartBody excelDataFile)
            throws IOException {

        return employeeService.importEmployeesFromExcel(tenantId, excelDataFile.file);
    }

    // @ApiOperation("Update an employee")
    @POST
    @Path("/update")
    public Employee updateEmployee(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid EmployeeView employeeView) {
        return employeeService.updateEmployee(tenantId, employeeView);
    }

    // ************************************************************************
    // EmployeeAvailability
    // ************************************************************************

    // @ApiOperation("Get an employee availability by id")
    @GET
    @Path("/availability/{id}")
    public EmployeeAvailabilityView getEmployeeAvailability(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return employeeService.getEmployeeAvailability(tenantId, id);
    }

    // @ApiOperation("Add a new employee availability")
    @POST
    @Path("/availability/add")
    public EmployeeAvailabilityView createEmployeeAvailability(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid EmployeeAvailabilityView employeeAvailabilityView) {
        return employeeService.createEmployeeAvailability(tenantId, employeeAvailabilityView);
    }

    // @ApiOperation("Update an employee availability")
    @PUT
    @Path("/availability/update")
    public EmployeeAvailabilityView updateEmployeeAvailability(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid EmployeeAvailabilityView employeeAvailabilityView) {
        return employeeService.updateEmployeeAvailability(tenantId, employeeAvailabilityView);
    }

    // @ApiOperation("Delete an employee availability")
    @DELETE
    @Path("/availability/{id}")
    public Boolean deleteEmployeeAvailability(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return employeeService.deleteEmployeeAvailability(tenantId, id);
    }
}
