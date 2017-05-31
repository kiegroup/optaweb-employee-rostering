/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.server.employee;

import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestService;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeSkillProficiency;

public class EmployeeRestServiceImpl implements EmployeeRestService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public List<Employee> getEmployeeList(Integer tenantId) {
        return entityManager.createNamedQuery("Employee.findAll", Employee.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
    }

    @Override
    @Transactional
    public Employee getEmployee(Integer tenantId, Long id) {
        Employee employee = entityManager.find(Employee.class, id);
        validateTenantIdParameter(tenantId, employee);
        return employee;
    }

    @Override
    @Transactional
    public Long addEmployee(Integer tenantId, Employee employee) {
        validateTenantIdParameter(tenantId, employee);
        entityManager.persist(employee);
        return employee.getId();
    }

    @Override
    @Transactional
    public Boolean removeEmployee(Integer tenantId, Long id) {
        Employee employee = entityManager.find(Employee.class, id);
        if (employee == null) {
            return false;
        }
        validateTenantIdParameter(tenantId, employee);
        entityManager.remove(employee);
        return true;
    }

    private void validateTenantIdParameter(Integer tenantId, Employee employee) {
        if (!Objects.equals(employee.getTenantId(), tenantId)) {
            throw new IllegalStateException("The tenantId (" + tenantId
                    + ") does not match the employee (" + employee + ")'s tenantId (" + employee.getTenantId() + ").");
        }
        for (EmployeeSkillProficiency skillProficiency : employee.getSkillProficiencyList()) {
            if (!Objects.equals(skillProficiency.getTenantId(), tenantId)) {
                throw new IllegalStateException("The tenantId (" + tenantId
                        + ") does not match the skillProficiency (" + skillProficiency
                        + ")'s tenantId (" + skillProficiency.getTenantId() + ").");
            }
        }
    }

}
