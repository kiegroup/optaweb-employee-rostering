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

import org.optaplanner.openshift.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeGroup;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestService;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

public class EmployeeRestServiceImpl extends AbstractRestServiceImpl implements EmployeeRestService {

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
    public Employee addEmployee(Integer tenantId, Employee employee) {
        validateTenantIdParameter(tenantId, employee);
        entityManager.persist(employee);
        return employee;
    }

    @Override
    @Transactional
    public Employee updateEmployee(Integer tenantId, Employee employee) {
        validateTenantIdParameter(tenantId, employee);
        employee = entityManager.merge(employee);
        return employee;
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

    protected void validateTenantIdParameter(Integer tenantId, Employee employee) {
        super.validateTenantIdParameter(tenantId, employee);
        for (Skill skill : employee.getSkillProficiencySet()) {
            if (!Objects.equals(skill.getTenantId(), tenantId)) {
                throw new IllegalStateException("The tenantId (" + tenantId
                        + ") does not match the skillProficiency (" + skill
                        + ")'s tenantId (" + skill.getTenantId() + ").");
            }
        }
    }

    @Override
    @Transactional
    public Long addEmployeeAvailability(Integer tenantId, EmployeeAvailabilityView employeeAvailabilityView) {
        EmployeeAvailability employeeAvailability = convertFromView(tenantId, employeeAvailabilityView);
        entityManager.persist(employeeAvailability);
        return employeeAvailability.getId();
    }

    @Override
    @Transactional
    public void updateEmployeeAvailability(Integer tenantId, EmployeeAvailabilityView employeeAvailabilityView) {
        EmployeeAvailability employeeAvailability = convertFromView(tenantId, employeeAvailabilityView);
        entityManager.merge(employeeAvailability);
    }

    private EmployeeAvailability convertFromView(Integer tenantId, EmployeeAvailabilityView employeeAvailabilityView) {
        validateTenantIdParameter(tenantId, employeeAvailabilityView);
        Employee employee = entityManager.find(Employee.class, employeeAvailabilityView.getEmployeeId());
        validateTenantIdParameter(tenantId, employee);
        TimeSlot timeSlot = entityManager.find(TimeSlot.class, employeeAvailabilityView.getTimeSlotId());
        validateTenantIdParameter(tenantId, timeSlot);
        EmployeeAvailability employeeAvailability = new EmployeeAvailability(employeeAvailabilityView, employee, timeSlot);
        employeeAvailability.setState(employeeAvailabilityView.getState());
        return employeeAvailability;
    }

    @Override
    @Transactional
    public Boolean removeEmployeeAvailability(Integer tenantId, Long id) {
        EmployeeAvailability employeeAvailability = entityManager.find(EmployeeAvailability.class, id);
        if (employeeAvailability == null) {
            return false;
        }
        validateTenantIdParameter(tenantId, employeeAvailability);
        entityManager.remove(employeeAvailability);
        return true;
    }

    @Override
    public List<EmployeeGroup> getEmployeeGroups(Integer tenantId) {
        return entityManager.createNamedQuery("EmployeeGroup.findAll", EmployeeGroup.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
    }

    @Override
    public EmployeeGroup getEmployeeGroup(Integer tenantId, Long id) {
        EmployeeGroup group = entityManager.find(EmployeeGroup.class, id);
        validateTenantIdParameter(tenantId, group);
        return group;
    }

    @Override
    @Transactional
    public Long createEmployeeGroup(Integer tenantId, EmployeeGroup employeeGroup) {
        validateTenantIdParameter(tenantId, employeeGroup);
        if (employeeGroup.getName().equals("ALL")) {
            throw new IllegalArgumentException("ALL is a reserved EmployeeGroup");
        }
        entityManager.persist(employeeGroup);
        return employeeGroup.getId();
    }

    @Override
    @Transactional
    public void addEmployeeToEmployeeGroup(Integer tenantId, Long id, Employee employee) {
        EmployeeGroup group = getEmployeeGroup(tenantId, id);
        validateTenantIdParameter(tenantId, employee);
        group.getEmployees().add(employee);
        entityManager.merge(group);
    }

    @Override
    @Transactional
    public void removeEmployeeFromEmployeeGroup(Integer tenantId, Long id, Employee employee) {
        EmployeeGroup group = getEmployeeGroup(tenantId, id);
        validateTenantIdParameter(tenantId, employee);
        group.getEmployees().remove(employee);
        entityManager.merge(group);
    }

    @Override
    @Transactional
    public Boolean deleteEmployeeGroup(Integer tenantId, Long id) {
        EmployeeGroup group = entityManager.find(EmployeeGroup.class, id);
        if (group == null) {
            return false;
        }
        validateTenantIdParameter(tenantId, group);
        entityManager.remove(group);
        return true;
    }

    @Override
    public EmployeeGroup findEmployeeGroupByName(Integer tenantId, String name) {
        if (name.equals("ALL")) {
            EmployeeGroup out = EmployeeGroup.getAllGroup(tenantId);
            out.setEmployees(getEmployeeList(tenantId));
            return out;
        }
        else {
            return entityManager.createNamedQuery("EmployeeGroup.findByName", EmployeeGroup.class)
                .setParameter("tenantId", tenantId)
                .setParameter("name", name)
                .getSingleResult();
        }
    }

}
