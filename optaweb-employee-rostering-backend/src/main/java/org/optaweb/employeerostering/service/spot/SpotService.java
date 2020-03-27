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

package org.optaweb.employeerostering.service.spot;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityNotFoundException;

import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;
import org.optaweb.employeerostering.service.common.AbstractRestService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpotService extends AbstractRestService {

    private final SpotRepository spotRepository;

    public SpotService(SpotRepository spotRepository) {
        this.spotRepository = spotRepository;
    }

    public Spot convertFromView(Integer tenantId, SpotView spotView) {
        validateTenantIdParameter(tenantId, spotView);
        Spot spot = new Spot(tenantId, spotView.getName(), spotView.getRequiredSkillSet(), spotView.isCovidWard());
        spot.setId(spotView.getId());
        spot.setVersion(spotView.getVersion());
        return spot;
    }

    @Transactional
    public List<Spot> getSpotList(Integer tenantId) {
        return spotRepository.findAllByTenantId(tenantId, PageRequest.of(0, Integer.MAX_VALUE));
    }

    @Transactional
    public Spot getSpot(Integer tenantId, Long id) {
        Spot spot = spotRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No Spot entity found with ID (" + id + ")."));

        validateTenantIdParameter(tenantId, spot);
        return spot;
    }

    @Transactional
    public Boolean deleteSpot(Integer tenantId, Long id) {
        Optional<Spot> spotOptional = spotRepository.findById(id);

        if (!spotOptional.isPresent()) {
            return false;
        }

        validateTenantIdParameter(tenantId, spotOptional.get());
        spotRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Spot createSpot(Integer tenantId, SpotView spotView) {
        Spot spot = convertFromView(tenantId, spotView);
        return spotRepository.save(spot);
    }

    @Transactional
    public Spot updateSpot(Integer tenantId, SpotView spotView) {
        Spot newSpot = convertFromView(tenantId, spotView);
        Spot oldSpot = spotRepository
                .findById(newSpot.getId())
                .orElseThrow(() -> new EntityNotFoundException("Spot entity with ID (" + newSpot.getId() +
                                                                       ") not found."));

        if (!oldSpot.getTenantId().equals(newSpot.getTenantId())) {
            throw new IllegalStateException("Spot entity with tenantId (" + oldSpot.getTenantId()
                                                    + ") cannot change tenants.");
        }

        oldSpot.setName(newSpot.getName());
        oldSpot.setRequiredSkillSet(newSpot.getRequiredSkillSet());
        oldSpot.setCovidWard(newSpot.isCovidWard());
        return spotRepository.save(oldSpot);
    }
}
