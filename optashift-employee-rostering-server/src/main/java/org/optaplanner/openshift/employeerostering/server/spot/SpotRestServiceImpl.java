/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.server.spot;

import java.util.List;
import javax.inject.Inject;

import org.optaplanner.openshift.employeerostering.server.roster.RosterDao;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestService;

public class SpotRestServiceImpl implements SpotRestService {

    @Inject
    private RosterDao rosterDao;

    @Override
    public List<Spot> getSpotList(Long tenantId) {
        List<Spot> spotList = rosterDao.getRoster(tenantId).getSpotList();
        return spotList;
    }

    @Override
    public Spot getSpot(Long tenantId, Long id) {
        List<Spot> spotList = rosterDao.getRoster(tenantId).getSpotList();
        return spotList.stream()
                .filter(spot -> spot.getId().equals(id))
                .findFirst().orElse(null);
    }

    @Override
    public Long addSpot(Long tenantId, Spot spot) {
        List<Spot> spotList = rosterDao.getRoster(tenantId).getSpotList();
        if (spot.getId() != null) {
            throw new IllegalArgumentException("The spot (" + spot
                    + ") to add already has an id (" + spot.getId() + ").");
        }
        long spotId = spotList.stream().mapToLong(AbstractPersistable::getId).max().orElse(0L) + 1L;
        spot.setId(spotId);
        spotList.add(spot);
        return spotId;
    }

    @Override
    public Boolean removeSpot(Long tenantId, Long id) {
        List<Spot> spotList = rosterDao.getRoster(tenantId).getSpotList();
        return spotList.removeIf(s -> s.getId().equals(id));
    }

}
