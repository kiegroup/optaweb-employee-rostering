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

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class EmployeeAvailabilityRepository implements PanacheRepository<EmployeeAvailability> {

    public List<EmployeeAvailability> findAllByTenantId(Integer tenantId) {
        return find("tenantId", Sort.ascending("employee.name", "startDateTime"), tenantId).list();
    }

    public void deleteForTenant(Integer tenantId) {
        delete("tenantId", tenantId);
    }

    public List<EmployeeAvailability> filterWithEmployee(Integer tenantId,
            Set<Employee> employeeSet,
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime) {
        // Panache doesn't like empty parameters
        if (employeeSet.isEmpty()) {
            return Collections.emptyList();
        }
        return find("tenantId = ?1 and employee in ?2 and endDateTime >= ?3 and startDateTime < ?4",
                Sort.ascending("employee.name", "startDateTime"),
                tenantId, employeeSet, startDateTime, endDateTime).list();
    }
}
