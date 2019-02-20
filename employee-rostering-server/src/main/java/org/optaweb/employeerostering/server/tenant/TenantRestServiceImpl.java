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

package org.optaweb.employeerostering.server.tenant;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.optaweb.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaweb.employeerostering.shared.roster.RosterState;
import org.optaweb.employeerostering.shared.tenant.RosterParametrization;
import org.optaweb.employeerostering.shared.tenant.Tenant;
import org.optaweb.employeerostering.shared.tenant.TenantRestService;

public class TenantRestServiceImpl extends AbstractRestServiceImpl
        implements
        TenantRestService {

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
        entityManager.createNamedQuery("Shift.deleteForTenant").setParameter("tenantId", tenantId).executeUpdate();
        entityManager.createNamedQuery("EmployeeAvailability.deleteForTenant").setParameter("tenantId", tenantId).executeUpdate();
        entityManager.createNamedQuery("ShiftTemplate.deleteForTenant").setParameter("tenantId", tenantId).executeUpdate();
        entityManager.createNamedQuery("Employee.deleteForTenant").setParameter("tenantId", tenantId).executeUpdate();
        entityManager.createNamedQuery("Spot.deleteForTenant").setParameter("tenantId", tenantId).executeUpdate();
        entityManager.createNamedQuery("Skill.deleteForTenant").setParameter("tenantId", tenantId).executeUpdate();
        entityManager.createNamedQuery("RosterParametrization.deleteForTenant").setParameter("tenantId", tenantId).executeUpdate();
        entityManager.createNamedQuery("RosterState.deleteForTenant").setParameter("tenantId", tenantId).executeUpdate();
        entityManager.remove(tenant);
        return true;
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
