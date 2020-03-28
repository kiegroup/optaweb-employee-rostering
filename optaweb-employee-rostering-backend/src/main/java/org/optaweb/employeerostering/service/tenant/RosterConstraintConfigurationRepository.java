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

package org.optaweb.employeerostering.service.tenant;

import java.util.Optional;

import org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RosterConstraintConfigurationRepository extends JpaRepository<RosterConstraintConfiguration, Long> {

    @Query("select distinct rc from RosterConstraintConfiguration rc " +
            "where rc.tenantId = :tenantId")
    Optional<RosterConstraintConfiguration> findByTenantId(@Param("tenantId") Integer tenantId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from RosterConstraintConfiguration rc where rc.tenantId = :tenantId")
    void deleteForTenant(@Param("tenantId") Integer tenantId);
}
