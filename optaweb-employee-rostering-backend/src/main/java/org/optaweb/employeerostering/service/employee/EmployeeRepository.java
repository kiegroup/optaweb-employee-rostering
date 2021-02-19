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
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.optaweb.employeerostering.domain.employee.Employee;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class EmployeeRepository implements PanacheRepository<Employee> {

    public List<Employee> findAllByTenantId(Integer tenantId) {
        return find("tenantId", Sort.ascending("name"), tenantId).list();
    }

    public Optional<Employee> findEmployeeByName(Integer tenantId, String name) {
        return find("tenantId = ?1 and name = ?2",
                Sort.ascending("name"),
                tenantId, name).singleResultOptional();
    }

    public void deleteForTenant(Integer tenantId) {
        delete("tenantId", tenantId);
    }
}
