/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.shared.employee;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.nmorel.gwtjackson.rest.processor.GenRestBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.optaweb.employeerostering.shared.common.AbstractPersistable;
import org.optaweb.employeerostering.shared.employee.view.EmployeeAvailabilityView;

@Api(tags = {"Employee"})
@Path("/tenant/{tenantId}/employee")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@GenRestBuilder
public interface EmployeeRestService {

    // ************************************************************************
    // Employee
    // ************************************************************************

    @ApiOperation("Get a list of all employees")
    @GET
    @Path("/")
    List<Employee> getEmployeeList(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId);

    /**
     * @param id never null
     * @return never null, the employee with the id
     */
    @ApiOperation("Get an employee by id")
    @GET
    @Path("/{id}")
    Employee getEmployee(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
                         @ApiParam(required = true) @PathParam("id") Long id);

    /**
     * @param employee never null
     * @return never null, with a {@link AbstractPersistable#getId()} that is never null
     */
    @ApiOperation("Add a new employee")
    @POST
    @Path("/add")
    Employee addEmployee(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
                         @ApiParam(value = "with no id", required = true) Employee employee);

    /**
     * @param employee never null
     * @return never null, with an updated {@link AbstractPersistable#getVersion()}
     */
    @ApiOperation("Update an employee")
    @POST
    @Path("/update")
    Employee updateEmployee(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
                            @ApiParam(required = true) Employee employee);

    /**
     * @param id never null
     * @return true if the employee was removed, false otherwise
     */
    @ApiOperation("Delete an employee")
    @DELETE
    @Path("/{id}")
    Boolean removeEmployee(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
                           @ApiParam(required = true) @PathParam("id") Long id);

    // ************************************************************************
    // EmployeeAvailability
    // ************************************************************************

    /**
     * @param employeeAvailability never null
     * @return never null, the id
     */
    @POST
    @Path("/availability/add")
    EmployeeAvailabilityView addEmployeeAvailability(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
                                 @ApiParam(value = "with no id", required = true) EmployeeAvailabilityView employeeAvailability);

    /**
     * @param employeeAvailability never null
     */
    @PUT
    @Path("/availability/update")
    EmployeeAvailabilityView updateEmployeeAvailability(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
                                    @ApiParam(required = true) EmployeeAvailabilityView employeeAvailability);

    /**
     * @param id never null
     * @return never null, the id
     */
    @DELETE
    @Path("/availability/{id}")
    Boolean removeEmployeeAvailability(@ApiParam(required = true) @PathParam("tenantId") Integer tenantId,
                                       @ApiParam(required = true) @PathParam("id") Long id);

}
