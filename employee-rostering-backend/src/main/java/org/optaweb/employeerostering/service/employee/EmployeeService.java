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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.domain.employee.view.EmployeeView;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.service.common.AbstractRestService;
import org.optaweb.employeerostering.service.roster.RosterStateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService extends AbstractRestService {

    private final EmployeeRepository employeeRepository;

    private final EmployeeAvailabilityRepository employeeAvailabilityRepository;

    private final RosterStateRepository rosterStateRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           EmployeeAvailabilityRepository employeeAvailabilityRepository,
                           RosterStateRepository rosterStateRepository) {
        this.employeeRepository = employeeRepository;
        this.employeeAvailabilityRepository = employeeAvailabilityRepository;
        this.rosterStateRepository = rosterStateRepository;
    }

    // ************************************************************************
    // Employee
    // ************************************************************************

    public Employee convertFromEmployeeView(Integer tenantId, EmployeeView employeeView) {
        validateTenantIdParameter(tenantId, employeeView);
        Employee employee = new Employee(tenantId, employeeView.getName(), employeeView.getContract(),
                                         employeeView.getSkillProficiencySet());
        employee.setId(employeeView.getId());
        employee.setVersion(employeeView.getVersion());
        return employee;
    }

    @Transactional
    public List<Employee> getEmployeeList(Integer tenantId) {
        return employeeRepository.findAllByTenantId(tenantId);
    }

    @Transactional
    public Employee getEmployee(Integer tenantId, Long id) {
        Optional<Employee> employeeOptional = employeeRepository.findById(id);

        if (!employeeOptional.isPresent()) {
            throw new EntityNotFoundException("No Employee entity found with ID (" + id + ").");
        }

        validateTenantIdParameter(tenantId, employeeOptional.get());
        return employeeOptional.get();
    }

    @Transactional
    public Boolean deleteEmployee(Integer tenantId, Long id) {
        Optional<Employee> employeeOptional = employeeRepository.findById(id);

        if (!employeeOptional.isPresent()) {
            return false;
        }

        validateTenantIdParameter(tenantId, employeeOptional.get());
        employeeRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Employee createEmployee(Integer tenantId, EmployeeView employeeView) {
        Employee employee = convertFromEmployeeView(tenantId, employeeView);
        validateTenantIdParameter(tenantId, employee);

        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Integer tenantId, EmployeeView employeeView) {
        Employee employee = convertFromEmployeeView(tenantId, employeeView);
        validateTenantIdParameter(tenantId, employee);

        Optional<Employee> employeeOptional = employeeRepository.findById(employee.getId());

        if (!employeeOptional.isPresent()) {
            throw new EntityNotFoundException("Employee entity with ID (" + employee.getId() + ") not found.");
        } else if (!employeeOptional.get().getTenantId().equals(employee.getTenantId())) {
            throw new IllegalStateException("Employee entity with tenantId (" + employeeOptional.get().getTenantId()
                                                    + ") cannot change tenants.");
        }

        Employee databaseEmployee = employeeOptional.get();
        databaseEmployee.setName(employee.getName());
        databaseEmployee.setSkillProficiencySet(employee.getSkillProficiencySet());
        databaseEmployee.setContract(employee.getContract());
        return employeeRepository.save(databaseEmployee);
    }

    protected void validateTenantIdParameter(Integer tenantId, Employee employee) {
        super.validateTenantIdParameter(tenantId, employee);
        for (Skill skill : employee.getSkillProficiencySet()) {
            if (!Objects.equals(skill.getTenantId(), tenantId)) {
                throw new IllegalStateException("The tenantId (" + tenantId + ") does not match the skillProficiency ("
                                                        + skill + ")'s tenantId (" + skill.getTenantId() + ").");
            }
        }
    }

    // ************************************************************************
    // EmployeeAvailability
    // ************************************************************************

    private EmployeeAvailability convertFromEmployeeAvailabilityView(Integer tenantId,
                                                                     EmployeeAvailabilityView
                                                                             employeeAvailabilityView) {
        validateTenantIdParameter(tenantId, employeeAvailabilityView);

        Optional<Employee> employeeOptional
                = employeeRepository.findById(employeeAvailabilityView.getEmployeeId());
        if (!employeeOptional.isPresent()) {
            throw new EntityNotFoundException("Employee entity with ID (" + employeeAvailabilityView.getEmployeeId()
                                                      + ") not found.");
        }

        Employee employee = employeeOptional.get();
        validateTenantIdParameter(tenantId, employee);

        Optional<RosterState> rosterStateOptional = rosterStateRepository.findByTenantId(tenantId);
        if (!rosterStateOptional.isPresent()) {
            throw new EntityNotFoundException("RosterState entity with tenantId (" + tenantId + ") not found.");
        }

        EmployeeAvailability employeeAvailability =
                new EmployeeAvailability(rosterStateOptional.get().getTimeZone(), employeeAvailabilityView, employee);
        employeeAvailability.setState(employeeAvailabilityView.getState());
        return employeeAvailability;
    }

    @Transactional
    public EmployeeAvailabilityView getEmployeeAvailability(Integer tenantId, Long id) {
        Optional<EmployeeAvailability> employeeAvailabilityOptional =
                employeeAvailabilityRepository.findById(id);

        if (!employeeAvailabilityOptional.isPresent()) {
            throw new EntityNotFoundException("No EmployeeAvailability entity found with ID (" + id + ").");
        }

        validateTenantIdParameter(tenantId, employeeAvailabilityOptional.get());
        return new EmployeeAvailabilityView(rosterStateRepository.findByTenantId(tenantId).get().getTimeZone(),
                employeeAvailabilityOptional.get());
    }

    @Transactional
    public EmployeeAvailabilityView createEmployeeAvailability(Integer tenantId,
                                                               EmployeeAvailabilityView employeeAvailabilityView) {
        EmployeeAvailability employeeAvailability = convertFromEmployeeAvailabilityView(tenantId,
                                                                                        employeeAvailabilityView);
        employeeAvailabilityRepository.save(employeeAvailability);
        return new EmployeeAvailabilityView(rosterStateRepository.findByTenantId(tenantId).get().getTimeZone(),
                                            employeeAvailability);
    }

    @Transactional
    public EmployeeAvailabilityView updateEmployeeAvailability(Integer tenantId,
                                                               EmployeeAvailabilityView employeeAvailabilityView) {
        EmployeeAvailability employeeAvailability = convertFromEmployeeAvailabilityView(tenantId,
                                                                                        employeeAvailabilityView);

        Optional<EmployeeAvailability> employeeAvailabilityOptional =
                employeeAvailabilityRepository.findById(employeeAvailability.getId());

        if (!employeeAvailabilityOptional.isPresent()) {
            throw new EntityNotFoundException("EmployeeAvailability entity with ID (" + employeeAvailability.getId()
                                                      + ") not found.");
        } else if (!employeeAvailabilityOptional.get().getTenantId().equals(employeeAvailability.getTenantId())) {
            throw new IllegalStateException("EmployeeAvailability entity with tenantId ("
                                                    + employeeAvailabilityOptional.get().getTenantId()
                                                    + ") cannot change tenants.");
        }

        EmployeeAvailability databaseEmployeeAvailability = employeeAvailabilityOptional.get();
        databaseEmployeeAvailability.setEmployee(employeeAvailability.getEmployee());
        databaseEmployeeAvailability.setStartDateTime(employeeAvailability.getStartDateTime());
        databaseEmployeeAvailability.setEndDateTime(employeeAvailability.getEndDateTime());
        databaseEmployeeAvailability.setState(employeeAvailability.getState());

        // Flush to increase version number before we duplicate it to EmployeeAvailableView
        EmployeeAvailability updatedEmployeeAvailability =
                employeeAvailabilityRepository.saveAndFlush(databaseEmployeeAvailability);

        return new EmployeeAvailabilityView(rosterStateRepository.findByTenantId(tenantId).get().getTimeZone(),
                                            updatedEmployeeAvailability);
    }

    @Transactional
    public Boolean deleteEmployeeAvailability(Integer tenantId, Long id) {
        Optional<EmployeeAvailability> employeeAvailabilityOptional = employeeAvailabilityRepository.findById(id);

        if (!employeeAvailabilityOptional.isPresent()) {
            return false;
        }

        validateTenantIdParameter(tenantId, employeeAvailabilityOptional.get());
        employeeAvailabilityRepository.deleteById(id);
        return true;
    }
}
