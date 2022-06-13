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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.optaweb.employeerostering.domain.common.MultipartBody;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.domain.employee.view.EmployeeView;

@Path("/rest/tenant/{tenantId}/employee")
@Tag(name = "Employee")
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

    @GET
    @Path("/")
    @Operation(summary = "List Employees", description = "Get a list of all employees")
    public List<Employee> getEmployeeList(@PathParam("tenantId") @Min(0) Integer tenantId) {
        return employeeService.getEmployeeList(tenantId);
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get Employee", description = "Get an employee by id")
    public Employee getEmployee(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return employeeService.getEmployee(tenantId, id);
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Delete Employee", description = "Delete an employee by id")
    public Boolean deleteEmployee(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return employeeService.deleteEmployee(tenantId, id);
    }

    @POST
    @Path("/add")
    @Operation(summary = "Add Employee", description = "Add a new employee")
    public Employee createEmployee(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid EmployeeView employeeView) {
        return employeeService.createEmployee(tenantId, employeeView);
    }

    @POST
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Import Employees", description = "Import employees from an Excel file")
    public List<Employee> addEmployeesFromExcelFile(@PathParam("tenantId") @Min(0) Integer tenantId,
            @MultipartForm MultipartBody excelDataFile)
            throws IOException {

        return employeeService.importEmployeesFromExcel(tenantId, excelDataFile.file);
    }

    @POST
    @Path("/update")
    @Operation(summary = "Update Employee", description = "Updates an employee")
    public Employee updateEmployee(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid EmployeeView employeeView) {
        return employeeService.updateEmployee(tenantId, employeeView);
    }

    // ************************************************************************
    // EmployeeAvailability
    // ************************************************************************

    @GET
    @Path("/availability/{id}")
    @Operation(summary = "Get Employee Availability", description = "Get an employee availability by id")
    public EmployeeAvailabilityView getEmployeeAvailability(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return employeeService.getEmployeeAvailability(tenantId, id);
    }

    @POST
    @Path("/availability/add")
    @Operation(summary = "Add Employee Availability", description = "Add a new employee availability")
    public EmployeeAvailabilityView createEmployeeAvailability(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid EmployeeAvailabilityView employeeAvailabilityView) {
        return employeeService.createEmployeeAvailability(tenantId, employeeAvailabilityView);
    }

    @PUT
    @Path("/availability/update")
    @Operation(summary = "Update Employee Availability", description = "Updates an employee availability")
    public EmployeeAvailabilityView updateEmployeeAvailability(@PathParam("tenantId") @Min(0) Integer tenantId,
            @Valid EmployeeAvailabilityView employeeAvailabilityView) {
        return employeeService.updateEmployeeAvailability(tenantId, employeeAvailabilityView);
    }

    @DELETE
    @Path("/availability/{id}")
    @Operation(summary = "Delete Employee Availability", description = "Deletes an employee availability")
    public Boolean deleteEmployeeAvailability(@PathParam("tenantId") @Min(0) Integer tenantId,
            @PathParam("id") @Min(0) Long id) {
        return employeeService.deleteEmployeeAvailability(tenantId, id);
    }
}
