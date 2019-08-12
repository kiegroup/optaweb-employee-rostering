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

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.optaweb.employeerostering.domain.tenant.RosterParametrization;
import org.optaweb.employeerostering.domain.tenant.view.RosterParametrizationView;
import org.optaweb.employeerostering.service.common.AbstractRestService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService extends AbstractRestService {

    private final RosterParametrizationRepository rosterParametrizationRepository;

    public TenantService(RosterParametrizationRepository rosterParametrizationRepository) {
        this.rosterParametrizationRepository = rosterParametrizationRepository;
    }

    public RosterParametrization convertFromRosterParametrizationView(RosterParametrizationView
                                                                              rosterParametrizationView) {
        RosterParametrization rosterParametrization =
                new RosterParametrization(rosterParametrizationView.getTenantId(),
                                          rosterParametrizationView.getUndesiredTimeSlotWeight(),
                                          rosterParametrizationView.getDesiredTimeSlotWeight(),
                                          rosterParametrizationView.getRotationEmployeeMatchWeight(),
                                          rosterParametrizationView.getWeekStartDay());
        rosterParametrization.setId(rosterParametrizationView.getId());
        rosterParametrization.setVersion(rosterParametrizationView.getVersion());
        return rosterParametrization;
    }

    @Transactional
    public RosterParametrization getRosterParametrization(Integer tenantId) {
        Optional<RosterParametrization> rosterParametrizationOptional =
                rosterParametrizationRepository.findByTenantId(tenantId);

        if (!rosterParametrizationOptional.isPresent()) {
            throw new EntityNotFoundException("No RosterParametrization entity found with tenantId ("
                                                      + tenantId + ").");
        }

        return rosterParametrizationOptional.get();
    }

    @Transactional
    public RosterParametrization updateRosterParametrization(RosterParametrizationView rosterParametrizationView) {
        Optional<RosterParametrization> rosterParametrizationOptional =
                rosterParametrizationRepository.findByTenantId(rosterParametrizationView.getTenantId());

        if (!rosterParametrizationOptional.isPresent()) {
            throw new EntityNotFoundException("RosterParametrization entity with tenantId ("
                                                      + rosterParametrizationView.getTenantId() + ") not found.");
        } else if (!rosterParametrizationOptional.get().getTenantId().equals(rosterParametrizationView.getTenantId())) {
            throw new IllegalStateException("RosterParametrization entity with tenantId ("
                                                    + rosterParametrizationOptional.get().getTenantId()
                                                    + ") cannot change tenants.");
        }

        RosterParametrization databaseRosterParametrization = rosterParametrizationOptional.get();
        databaseRosterParametrization.setDesiredTimeSlotWeight(rosterParametrizationView.getDesiredTimeSlotWeight());
        databaseRosterParametrization.setRotationEmployeeMatchWeight(
                rosterParametrizationView.getRotationEmployeeMatchWeight());
        databaseRosterParametrization.setUndesiredTimeSlotWeight(
                rosterParametrizationView.getUndesiredTimeSlotWeight());
        databaseRosterParametrization.setWeekStartDay(rosterParametrizationView.getWeekStartDay());
        return rosterParametrizationRepository.save(databaseRosterParametrization);
    }

    public List<ZoneId> getSupportedTimezones() {
        return ZoneId.getAvailableZoneIds().stream()
                .sorted().map(zoneId -> ZoneId.of(zoneId))
                .collect(Collectors.toList());
    }
}
