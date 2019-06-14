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

package org.optaweb.employeerostering.service.skill;

import java.util.List;

import org.optaweb.employeerostering.domain.Skill;
import org.optaweb.employeerostering.persistence.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SkillService {

    @Autowired
    private SkillRepository skillRepository;

    public List<Skill> getSkillList(Integer tenantId) {
        return skillRepository.findAll();
    }

    public Skill getSkill(Integer tenantId, Long id) {
        if (skillRepository.findById(id).isPresent())
            return skillRepository.findById(id).get();

        return new Skill();
    }

    public void deleteSkill(Integer tenantId, Long id) {
        //TODO: delete skill
    }

    public void createSkill(Integer tenantId, Skill skill) {
        //TODO: create skill
    }

    public void updateSkill(Integer tenantId, Skill skill) {
        //TODO: update skill
    }
}
