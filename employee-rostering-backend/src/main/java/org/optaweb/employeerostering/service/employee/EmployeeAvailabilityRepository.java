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
import java.util.List;
import java.util.Set;

import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeAvailabilityRepository extends JpaRepository<EmployeeAvailability, Long> {

    @Query("select distinct ea from EmployeeAvailability ea" +
            " left join fetch ea.employee e" +
            " where ea.tenantId = :tenantId" +
            " order by e.name, ea.startDateTime")
    List<EmployeeAvailability> findAllByTenantId(@Param("tenantId") Integer tenantId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from EmployeeAvailability ea where ea.tenantId = :tenantId")
    void deleteForTenant(@Param("tenantId") Integer tenantId);

    @Query("select distinct ea from EmployeeAvailability ea" +
            " left join fetch ea.employee e" +
            " where ea.tenantId = :tenantId" +
            " and ea.employee IN :employeeSet" +
            " and ea.endDateTime >= :startDateTime" +
            " and ea.startDateTime < :endDateTime" +
            " order by e.name, ea.startDateTime")
    List<EmployeeAvailability> filterWithEmployee(@Param("tenantId") Integer tenantId,
                                                  @Param("employeeSet") Set<Employee> employeeSet,
                                                  @Param("startDateTime") OffsetDateTime startDateTime,
                                                  @Param("endDateTime") OffsetDateTime endDateTime);
}
