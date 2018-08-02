/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.server.tenant;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.optaweb.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaweb.employeerostering.shared.common.AbstractPersistable;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeAvailability;
import org.optaweb.employeerostering.shared.roster.RosterState;
import org.optaweb.employeerostering.shared.rotation.ShiftTemplate;
import org.optaweb.employeerostering.shared.shift.Shift;
import org.optaweb.employeerostering.shared.skill.Skill;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.tenant.RosterParametrization;
import org.optaweb.employeerostering.shared.tenant.Tenant;
import org.optaweb.employeerostering.shared.tenant.TenantRestService;

public class TenantRestServiceImpl extends AbstractRestServiceImpl implements TenantRestService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public List<Tenant> getTenantList() {
        return entityManager.createNamedQuery("Tenant.findAll", Tenant.class)
                .getResultList();
    }

    @Override
    @Transactional
    public Tenant getTenant(Integer id) {
        Tenant tenant = entityManager.find(Tenant.class, id);
        return tenant;
    }

    @Override
    @Transactional
    public Tenant addTenant(RosterState intialRosterState) {
        entityManager.persist(intialRosterState.getTenant());
        intialRosterState.setTenantId(intialRosterState.getTenant().getId());
        RosterParametrization rosterParametriation = new RosterParametrization();
        rosterParametriation.setTenantId(intialRosterState.getTenant().getId());
        entityManager.persist(intialRosterState);
        entityManager.persist(rosterParametriation);
        return intialRosterState.getTenant();
    }

    @Override
    @Transactional
    public Boolean removeTenant(Integer tenantId) {
        // Dependency order: Shift, EmployeeAvailability, ShiftTemplate,
        // Employee, Spot, Skill,
        // RosterParametrization, RosterState
        Tenant tenant = getTenant(tenantId);
        if (tenant == null) {
            throw new IllegalArgumentException("There is no tenant with id (" + tenantId + ").");
        }
        deleteAllEntitiesBelongingToTenantOfType(tenantId, Shift.class, "Shift.findAll");
        deleteAllEntitiesBelongingToTenantOfType(tenantId, EmployeeAvailability.class, "EmployeeAvailability.findAll");
        deleteAllEntitiesBelongingToTenantOfType(tenantId, ShiftTemplate.class, "ShiftTemplate.findAll");
        deleteAllEntitiesBelongingToTenantOfType(tenantId, Employee.class, "Employee.findAll");
        deleteAllEntitiesBelongingToTenantOfType(tenantId, Spot.class, "Spot.findAll");
        deleteAllEntitiesBelongingToTenantOfType(tenantId, Skill.class, "Skill.findAll");
        deleteAllEntitiesBelongingToTenantOfType(tenantId, RosterParametrization.class, "RosterParametrization.find");
        deleteAllEntitiesBelongingToTenantOfType(tenantId, RosterState.class, "RosterState.find");
        entityManager.remove(tenant);
        return true;
    }

    private <T extends AbstractPersistable> void deleteAllEntitiesBelongingToTenantOfType(Integer tenantId, Class<T> type, String queryName) {
        List<T> entitiesBelongingToTenant = entityManager.createNamedQuery(queryName, type).setParameter("tenantId", tenantId)
                .getResultList();
        for (T entity : entitiesBelongingToTenant) {
            entityManager.remove(entity);
        }
    }

    @Override
    @Transactional
    public RosterParametrization updateRosterParametrization(RosterParametrization rosterParametrization) {
        return entityManager.merge(rosterParametrization);
    }

    @Override
    public RosterParametrization getRosterParametrization(Integer tenantId) {
        return entityManager.createNamedQuery("RosterParametrization.find", RosterParametrization.class)
                .setParameter("tenantId", tenantId)
                .getSingleResult();
    }

    @Override
    public List<ZoneId> getSupportedTimezones() {
        return ZoneId.getAvailableZoneIds().stream()
                .sorted().map(zoneId -> ZoneId.of(zoneId))
                .collect(Collectors.toList());
    }
}
