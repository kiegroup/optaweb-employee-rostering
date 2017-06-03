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

package org.optaplanner.openshift.employeerostering.server.skill;

import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.optaplanner.openshift.employeerostering.server.common.AbstractRestServiceImpl;
import org.optaplanner.openshift.employeerostering.server.roster.RosterDao;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestService;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class SkillRestServiceImpl extends AbstractRestServiceImpl implements SkillRestService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public List<Skill> getSkillList(Integer tenantId) {
        return entityManager.createNamedQuery("Skill.findAll", Skill.class)
                .setParameter("tenantId", tenantId)
                .getResultList();
    }

    @Override
    @Transactional
    public Skill getSkill(Integer tenantId, Long id) {
        Skill skill = entityManager.find(Skill.class, id);
        validateTenantIdParameter(tenantId, skill);
        return skill;
    }

    @Override
    @Transactional
    public Long addSkill(Integer tenantId, Skill skill) {
        validateTenantIdParameter(tenantId, skill);
        entityManager.persist(skill);
        return skill.getId();
    }

    @Override
    @Transactional
    public Boolean removeSkill(Integer tenantId, Long id) {
        Skill skill = entityManager.find(Skill.class, id);
        if (skill == null) {
            return false;
        }
        validateTenantIdParameter(tenantId, skill);
        entityManager.remove(skill);
        return true;
    }

}
