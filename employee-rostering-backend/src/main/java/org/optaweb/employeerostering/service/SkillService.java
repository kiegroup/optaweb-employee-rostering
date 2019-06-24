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

import javax.persistence.EntityNotFoundException;

import org.optaweb.employeerostering.domain.Skill;
import org.optaweb.employeerostering.persistence.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
        Assert.notNull(skillRepository, "skillRepository must not be null.");
    }

    public List<Skill> getSkillList(Integer tenantId) {
        return skillRepository.findAll();
    }

    public Skill getSkill(Integer tenantId, Long id) {
        if (skillRepository.findById(id).isPresent()) {
            return skillRepository.findById(id).get();
        }

        throw new EntityNotFoundException("No Skill entity found with ID (" + id + ").");
    }

    public void deleteSkill(Integer tenantId, Long id) {
        if (skillRepository.findById(id).isPresent()) {
            skillRepository.deleteById(id);
        }
        else {
            throw new EntityNotFoundException("No Skill entity found with ID (" + id + ").");
        }
    }

    public Skill createSkill(Integer tenantId, Skill skill) {
        skillRepository.save(skill);
        return skill;
    }

    public Skill updateSkill(Integer tenantId, Skill skill) {

        if (skill.getId() == null) {
            throw new EntityNotFoundException("Skill id cannot be null.");
        }
        else if (!skillRepository.findById(skill.getId()).isPresent()) {
            throw new EntityNotFoundException("Skill entity not found.");
        }
        else {
            skillRepository.deleteById(skill.getId());
            skillRepository.save(skill);

            return skill;
        }
    }
}
