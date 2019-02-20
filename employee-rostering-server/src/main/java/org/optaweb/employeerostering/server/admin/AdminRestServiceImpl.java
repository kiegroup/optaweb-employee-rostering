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

package org.optaweb.employeerostering.server.admin;

import java.time.ZoneId;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.transaction.Transactional;

import org.optaweb.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaweb.employeerostering.server.roster.RosterGenerator;
import org.optaweb.employeerostering.shared.admin.AdminRestService;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeAvailability;
import org.optaweb.employeerostering.shared.roster.RosterState;
import org.optaweb.employeerostering.shared.rotation.ShiftTemplate;
import org.optaweb.employeerostering.shared.shift.Shift;
import org.optaweb.employeerostering.shared.skill.Skill;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.tenant.RosterParametrization;
import org.optaweb.employeerostering.shared.tenant.Tenant;

public class AdminRestServiceImpl extends AbstractRestServiceImpl implements AdminRestService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private RosterGenerator rosterGenerator;

    @Override
    @Transactional
    public void resetApplication(ZoneId zoneId) {
        if (zoneId == null){
            zoneId = SystemPropertiesRetriever.determineZoneId();
        }
        // IMPORTANT: Delete entries that has Many-to-One relations first,
        // otherwise we break referential integrity
        deleteAllEntities(Shift.class, EmployeeAvailability.class, ShiftTemplate.class,
                Employee.class, Spot.class, Skill.class,
                RosterParametrization.class, RosterState.class, Tenant.class);
        rosterGenerator.setUpGeneratedData(zoneId);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void deleteAllEntities(Class<?>... entityTypes) {
        for (Class entityType : entityTypes) {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();
            CriteriaDelete query = builder.createCriteriaDelete(entityType);
            query.from(entityType);
            entityManager.createQuery(query).executeUpdate();
        }
    }

}
