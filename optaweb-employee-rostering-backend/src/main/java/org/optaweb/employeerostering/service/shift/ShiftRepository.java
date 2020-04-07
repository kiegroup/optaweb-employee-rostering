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

import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

    @Query("select distinct sa from Shift sa" +
            " left join fetch sa.spot s" +
            " left join fetch sa.rotationEmployee re" +
            " left join fetch sa.originalEmployee oe" +
            " left join fetch sa.employee e" +
            " where sa.tenantId = :tenantId" +
            " order by sa.startDateTime, s.name, e.name")
    List<Shift> findAllByTenantId(@Param("tenantId") Integer tenantId);

    @Query("select distinct sa from Shift sa" +
            " left join fetch sa.spot s" +
            " left join fetch sa.rotationEmployee re" +
            " left join fetch sa.originalEmployee oe" +
            " left join fetch sa.employee e" +
            " where sa.tenantId = :tenantId" +
            " and sa.endDateTime >= :startDateTime" +
            " and sa.startDateTime < :endDateTime" +
            " order by sa.startDateTime, s.name, e.name")
    List<Shift> findAllByTenantIdBetweenDates(@Param("tenantId") Integer tenantId,
                                              @Param("startDateTime") OffsetDateTime startDateTime,
                                              @Param("endDateTime") OffsetDateTime endDateTime);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Shift s" +
            " where s.tenantId = :tenantId")
    void deleteForTenant(@Param("tenantId") Integer tenantId);

    @Query("select distinct sa from Shift sa" +
            " left join fetch sa.spot s" +
            " left join fetch sa.rotationEmployee re" +
            " left join fetch sa.originalEmployee oe" +
            " left join fetch sa.employee e" +
            " where sa.tenantId = :tenantId" +
            " and sa.spot IN :spotSet" +
            " and sa.endDateTime >= :startDateTime" +
            " and sa.startDateTime < :endDateTime" +
            " order by sa.startDateTime, s.name, e.name")
    List<Shift> filterWithSpots(@Param("tenantId") Integer tenantId, @Param("spotSet") Set<Spot> spotSet,
                                @Param("startDateTime") OffsetDateTime startDateTime,
                                @Param("endDateTime") OffsetDateTime endDateTime);

    @Query("select distinct sa from Shift sa" +
            " left join fetch sa.spot s" +
            " left join fetch sa.rotationEmployee re" +
            " left join fetch sa.originalEmployee oe" +
            " left join fetch sa.employee e" +
            " where sa.tenantId = :tenantId" +
            " and sa.employee IN :employeeSet" +
            " and sa.endDateTime >= :startDateTime" +
            " and sa.startDateTime < :endDateTime" +
            " order by sa.startDateTime, s.name, e.name")
    List<Shift> filterWithEmployees(@Param("tenantId") Integer tenantId,
                                    @Param("employeeSet") Set<Employee> employeeSet,
                                    @Param("startDateTime") OffsetDateTime startDateTime,
                                    @Param("endDateTime") OffsetDateTime endDateTime);
}
