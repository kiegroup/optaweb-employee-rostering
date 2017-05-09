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
import javax.inject.Inject;

import org.optaplanner.openshift.employeerostering.server.roster.RosterDao;
import org.optaplanner.openshift.employeerostering.shared.domain.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestService;

public class SkillRestServiceImpl implements SkillRestService {

    @Inject
    private RosterDao rosterDao;

    @Override
    public List<Skill> getSkillList(Long tenantId) {
        List<Skill> skillList = rosterDao.getRoster(tenantId).getSkillList();
        return skillList;
    }

    @Override
    public Skill getSkill(Long tenantId, Long id) {
        List<Skill> skillList = rosterDao.getRoster(tenantId).getSkillList();
        return skillList.stream()
                .filter(skill -> skill.getId().equals(id))
                .findFirst().orElse(null);
    }

    @Override
    public Long addSkill(Long tenantId, Skill skill) {
        List<Skill> skillList = rosterDao.getRoster(tenantId).getSkillList();
        if (skill.getId() != null) {
            throw new IllegalArgumentException("The skill (" + skill
                    + ") to add already has an id (" + skill.getId() + ").");
        }
        long skillId = skillList.stream().mapToLong(AbstractPersistable::getId).max().orElse(0L) + 1L;
        skill.setId(skillId);
        skillList.add(skill);
        return skillId;
    }

    @Override
    public Boolean removeSkill(Long tenantId, Long id) {
        List<Skill> skillList = rosterDao.getRoster(tenantId).getSkillList();
        return skillList.removeIf(s -> s.getId().equals(id));
    }

}
