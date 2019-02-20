/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.server.spot;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.optaweb.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.spot.SpotRestService;

public class SpotRestServiceImpl extends AbstractRestServiceImpl
        implements
        SpotRestService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public List<Spot> getSpotList(Integer tenantId) {
        return entityManager.createNamedQuery("Spot.findAll", Spot.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
    }

    @Override
    @Transactional
    public Spot getSpot(Integer tenantId, Long id) {
        Spot spot = entityManager.find(Spot.class, id);
        if (spot == null) {
            throw new EntityNotFoundException("No Spot entity found with ID (" + id + ").");
        }
        validateTenantIdParameter(tenantId, spot);
        return spot;
    }

    @Override
    @Transactional
    public Spot addSpot(Integer tenantId, Spot spot) {
        validateTenantIdParameter(tenantId, spot);
        entityManager.persist(spot);
        return spot;
    }

    @Override
    @Transactional
    public Spot updateSpot(Integer tenantId, Spot spot) {
        validateTenantIdParameter(tenantId, spot);
        spot = entityManager.merge(spot);
        return spot;
    }

    @Override
    @Transactional
    public Boolean removeSpot(Integer tenantId, Long id) {
        Spot spot = entityManager.find(Spot.class, id);
        if (spot == null) {
            return false;
        }
        validateTenantIdParameter(tenantId, spot);
        entityManager.remove(spot);
        return true;
    }
}
