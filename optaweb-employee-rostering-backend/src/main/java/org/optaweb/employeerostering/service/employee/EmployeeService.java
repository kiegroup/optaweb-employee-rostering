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
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.EntityNotFoundException;

import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.domain.employee.view.EmployeeView;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.service.common.AbstractRestService;
import org.optaweb.employeerostering.service.roster.RosterStateRepository;
import org.optaweb.employeerostering.util.EmployeeListXlsxFileIO;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService extends AbstractRestService {

    private final EmployeeRepository employeeRepository;

    private final EmployeeAvailabilityRepository employeeAvailabilityRepository;

    private final RosterStateRepository rosterStateRepository;

    private final EmployeeListXlsxFileIO employeeListXlsxFileIO;

    public EmployeeService(EmployeeRepository employeeRepository,
                           EmployeeAvailabilityRepository employeeAvailabilityRepository,
                           RosterStateRepository rosterStateRepository,
                           EmployeeListXlsxFileIO employeeListXlsxFileIO) {
        this.employeeRepository = employeeRepository;
        this.employeeAvailabilityRepository = employeeAvailabilityRepository;
        this.rosterStateRepository = rosterStateRepository;
        this.employeeListXlsxFileIO = employeeListXlsxFileIO;
    }

    // ************************************************************************
    // Employee
    // ************************************************************************

    public Employee convertFromEmployeeView(Integer tenantId, EmployeeView employeeView) {
        validateTenantIdParameter(tenantId, employeeView);
        Employee employee = new Employee(tenantId, employeeView.getName(), employeeView.getContract(),
                                         employeeView.getSkillProficiencySet(), employeeView.getCovidRiskType());
        employee.setId(employeeView.getId());
        employee.setVersion(employeeView.getVersion());
        return employee;
    }

    @Transactional
    public List<Employee> getEmployeeList(Integer tenantId) {
        return employeeRepository.findAllByTenantId(tenantId, PageRequest.of(0, Integer.MAX_VALUE));
    }

    @Transactional
    public Employee getEmployee(Integer tenantId, Long id) {
        Employee employee = employeeRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No Employee entity found with ID (" + id + ")."));

        validateTenantIdParameter(tenantId, employee);
        return employee;
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
        Employee newEmployee = convertFromEmployeeView(tenantId, employeeView);

        Employee oldEmployee = employeeRepository
                .findById(newEmployee.getId())
                .orElseThrow(() -> new EntityNotFoundException("Employee entity with ID (" + newEmployee.getId() +
                                                                       ") not found."));

        if (!oldEmployee.getTenantId().equals(newEmployee.getTenantId())) {
            throw new IllegalStateException("Employee entity with tenantId (" + oldEmployee.getTenantId()
                                                    + ") cannot change tenants.");
        }

        oldEmployee.setName(newEmployee.getName());
        oldEmployee.setSkillProficiencySet(newEmployee.getSkillProficiencySet());
        oldEmployee.setContract(newEmployee.getContract());
        oldEmployee.setCovidRiskType(newEmployee.getCovidRiskType());
        return employeeRepository.save(oldEmployee);
    }

    @Transactional
    public List<Employee> importEmployeesFromExcel(Integer tenantId, InputStream excelInputStream) throws IOException {
        List<EmployeeView> excelEmployeeList = employeeListXlsxFileIO
                .getEmployeeListFromExcelFile(tenantId, excelInputStream);

        final Set<String> addedEmployeeSet = new HashSet<>();
        excelEmployeeList.stream().flatMap(employee -> {
            if (addedEmployeeSet.contains(employee.getName().toLowerCase())) {
                // Duplicate Employee; already in the stream
                return Stream.empty();
            }
            // Add employee to the stream
            addedEmployeeSet.add(employee.getName().toLowerCase());
            return Stream.of(employee);
        }).forEach(employee -> {
            Employee oldEmployee = employeeRepository.findEmployeeByName(tenantId, employee.getName());
            if (oldEmployee != null) {
                employee.setContract(oldEmployee.getContract());
                employee.setCovidRiskType(oldEmployee.getCovidRiskType());
                employee.setId(oldEmployee.getId());
                employee.setVersion(oldEmployee.getVersion());
                updateEmployee(tenantId, employee);
            } else {
                createEmployee(tenantId, employee);
            }
        });

        return getEmployeeList(tenantId);
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

        Employee employee = employeeRepository
                .findById(employeeAvailabilityView.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Employee entity with ID (" +
                                                                       employeeAvailabilityView.getEmployeeId() +
                                                                       ") not found."));

        validateTenantIdParameter(tenantId, employee);

        RosterState rosterState = rosterStateRepository
                .findByTenantId(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("RosterState entity with tenantId (" +
                                                                       tenantId + ") not found."));

        EmployeeAvailability employeeAvailability =
                new EmployeeAvailability(rosterState.getTimeZone(), employeeAvailabilityView, employee);
        employeeAvailability.setState(employeeAvailabilityView.getState());
        return employeeAvailability;
    }

    @Transactional
    public EmployeeAvailabilityView getEmployeeAvailability(Integer tenantId, Long id) {
        EmployeeAvailability employeeAvailability = employeeAvailabilityRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No EmployeeAvailability entity found with ID (" + id +
                                                                       ")."));

        validateTenantIdParameter(tenantId, employeeAvailability);

        RosterState rosterState = rosterStateRepository
                .findByTenantId(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("No RosterState entity found with tenantId (" +
                                                                       tenantId + ")."));
        return new EmployeeAvailabilityView(rosterState.getTimeZone(), employeeAvailability);
    }

    @Transactional
    public EmployeeAvailabilityView createEmployeeAvailability(Integer tenantId,
                                                               EmployeeAvailabilityView employeeAvailabilityView) {
        EmployeeAvailability employeeAvailability = convertFromEmployeeAvailabilityView(tenantId,
                                                                                        employeeAvailabilityView);
        employeeAvailabilityRepository.save(employeeAvailability);

        RosterState rosterState = rosterStateRepository
                .findByTenantId(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("No RosterState entity found with tenantId (" +
                                                                       tenantId + ")."));
        return new EmployeeAvailabilityView(rosterState.getTimeZone(), employeeAvailability);
    }

    @Transactional
    public EmployeeAvailabilityView updateEmployeeAvailability(Integer tenantId,
                                                               EmployeeAvailabilityView employeeAvailabilityView) {
        EmployeeAvailability newEmployeeAvailability = convertFromEmployeeAvailabilityView(tenantId,
                                                                                           employeeAvailabilityView);

        EmployeeAvailability oldEmployeeAvailability = employeeAvailabilityRepository
                .findById(newEmployeeAvailability.getId())
                .orElseThrow(() -> new EntityNotFoundException("EmployeeAvailability entity with ID (" +
                                                                       newEmployeeAvailability.getId() +
                                                                       ") not found."));

        if (!oldEmployeeAvailability.getTenantId().equals(newEmployeeAvailability.getTenantId())) {
            throw new IllegalStateException("EmployeeAvailability entity with tenantId (" +
                                                    newEmployeeAvailability.getTenantId() +
                                                    ") cannot change tenants.");
        }

        oldEmployeeAvailability.setEmployee(newEmployeeAvailability.getEmployee());
        oldEmployeeAvailability.setStartDateTime(newEmployeeAvailability.getStartDateTime());
        oldEmployeeAvailability.setEndDateTime(newEmployeeAvailability.getEndDateTime());
        oldEmployeeAvailability.setState(newEmployeeAvailability.getState());

        // Flush to increase version number before we duplicate it to EmployeeAvailableView
        EmployeeAvailability updatedEmployeeAvailability =
                employeeAvailabilityRepository.saveAndFlush(oldEmployeeAvailability);

        RosterState rosterState = rosterStateRepository
                .findByTenantId(tenantId)
                .orElseThrow(() -> new EntityNotFoundException("No RosterState entity found with tenantId (" +
                                                                       tenantId + ")."));
        return new EmployeeAvailabilityView(rosterState.getTimeZone(), updatedEmployeeAvailability);
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
