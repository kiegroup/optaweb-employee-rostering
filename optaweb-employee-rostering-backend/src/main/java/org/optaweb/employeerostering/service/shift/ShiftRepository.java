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

package org.optaweb.employeerostering.service.shift;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.spot.Spot;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class ShiftRepository implements PanacheRepository<Shift> {

    // FIXME: When https://github.com/quarkusio/quarkus/issues/15088 is fixed,
    //        add employee.name as a last parameter to sort
    public List<Shift> findAllByTenantId(Integer tenantId) {
        return find("tenantId", Sort.ascending("startDateTime", "spot.name"),
                tenantId).list();
    }

    // FIXME: When https://github.com/quarkusio/quarkus/issues/15088 is fixed,
    //        add employee.name as a last parameter to sort
    public List<Shift> findAllByTenantIdBetweenDates(Integer tenantId,
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime) {
        return find("tenantId = ?1 and endDateTime >= ?2 and startDateTime < ?3",
                Sort.ascending("startDateTime", "spot.name"),
                tenantId, startDateTime, endDateTime).list();
    }

    public void deleteForTenant(Integer tenantId) {
        delete("tenantId", tenantId);
    }

    // FIXME: When https://github.com/quarkusio/quarkus/issues/15088 is fixed,
    //        add employee.name as a last parameter to sort
    public List<Shift> filterWithSpots(Integer tenantId, Set<Spot> spotSet,
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime) {
        return find("tenantId = ?1 and spot in ?2 and endDateTime >= ?3 and startDateTime < ?4",
                Sort.ascending("startDateTime", "spot.name"),
                tenantId, spotSet, startDateTime, endDateTime).list();
    }

    // FIXME: When https://github.com/quarkusio/quarkus/issues/15088 is fixed,
    //        add employee.name as a last parameter to sort
    public List<Shift> filterWithEmployees(Integer tenantId,
            Set<Employee> employeeSet,
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime) {
        return find("tenantId = ?1 and employee in ?2 and endDateTime >= ?3 and startDateTime < ?4",
                Sort.ascending("startDateTime", "spot.name"),
                tenantId, employeeSet, startDateTime, endDateTime).list();
    }
}
