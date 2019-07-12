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

package org.optaweb.employeerostering.service;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.optaweb.employeerostering.domain.Spot;
import org.optaweb.employeerostering.persistence.SpotRepository;
import org.springframework.stereotype.Service;

@Service
public class SpotService extends AbstractRestService {

    private final SpotRepository spotRepository;

    public SpotService(SpotRepository spotRepository) {
        this.spotRepository = spotRepository;
    }

    @Transactional
    public List<Spot> getSpotList(Integer tenantId) {
        return spotRepository.findAllByTenantId(tenantId);
    }

    @Transactional
    public Spot getSpot(Integer tenantId, Long id) {
        Optional<Spot> spotOptional = spotRepository.findById(id);

        if (!spotOptional.isPresent()) {
            throw new EntityNotFoundException("No Spot entity found with ID (" + id + ").");
        }

        validateTenantIdParameter(tenantId, spotOptional.get());
        return spotOptional.get();
    }

    @Transactional
    public Boolean deleteSpot(Integer tenantId, Long id) {
        Optional<Spot> spotOptional = spotRepository.findById(id);

        if (!spotOptional.isPresent()) {
            throw new EntityNotFoundException("No Spot entity found with ID (" + id + ").");
        }

        validateTenantIdParameter(tenantId, spotOptional.get());
        spotRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Spot createSpot(Integer tenantId, Spot spot) {
        validateTenantIdParameter(tenantId, spot);

        return spotRepository.save(spot);
    }

    @Transactional
    public Spot updateSpot(Integer tenantId, Spot spot) {
        validateTenantIdParameter(tenantId, spot);

        Optional<Spot> spotOptional = spotRepository.findById(spot.getId());

        if (!spotOptional.isPresent()) {
            throw new EntityNotFoundException("Spot entity with ID (" + spot.getId() + ") not found.");
        } else if (!spotOptional.get().getTenantId().equals(spot.getTenantId())) {
            throw new IllegalStateException("Spot entity with tenantId (" + spotOptional.get().getTenantId()
                                                    + ") cannot change tenants.");
        }

        Spot databaseSpot = spotOptional.get();
        databaseSpot.setName(spot.getName());
        databaseSpot.setRequiredSkillSet(spot.getRequiredSkillSet());
        return spotRepository.save(databaseSpot);
    }
}
