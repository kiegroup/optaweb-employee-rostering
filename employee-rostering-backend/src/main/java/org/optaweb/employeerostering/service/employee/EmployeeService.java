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
import javax.transaction.Transactional;

import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.service.common.AbstractRestService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService extends AbstractRestService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
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
            throw new EntityNotFoundException("No Employee entity found with ID (" + id + ").");
        }

        validateTenantIdParameter(tenantId, employeeOptional.get());
        employeeRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Employee createEmployee(Integer tenantId, Employee employee) {
        validateTenantIdParameter(tenantId, employee);

        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(Integer tenantId, Employee employee) {
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

    //TODO: Add EmployeeAvailability CRUD methods
}
