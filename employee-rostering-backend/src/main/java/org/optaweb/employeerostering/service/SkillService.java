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

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.optaweb.employeerostering.domain.Skill;
import org.optaweb.employeerostering.persistence.SkillRepository;
import org.springframework.stereotype.Service;

@Service
public class SkillService extends AbstractRestService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Transactional
    public List<Skill> getSkillList(Integer tenantId) {
        return skillRepository.findAllByTenantId(tenantId);
    }

    @Transactional
    public Skill getSkill(Integer tenantId, Long id) {

        Optional<Skill> skillOptional = skillRepository.findById(id);

        if (!skillOptional.isPresent()) {
            throw new EntityNotFoundException("No Skill entity found with ID (" + id + ").");
        }

        validateTenantIdParameter(tenantId, skillOptional.get());
        return skillRepository.findById(id).get();
    }

    @Transactional
    public Boolean deleteSkill(Integer tenantId, Long id) {

        Optional<Skill> skillOptional = skillRepository.findById(id);

        if (!skillOptional.isPresent()) {
            throw new EntityNotFoundException("No Skill entity found with ID (" + id + ").");
        }

        validateTenantIdParameter(tenantId, skillOptional.get());
        skillRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Skill createSkill(Integer tenantId, Skill skill) {

        validateTenantIdParameter(tenantId, skill);

        //If name is null
        if (skill.getName() == null) {
            throw new IllegalStateException("Skill entity name with tenantId (" + tenantId + ") cannot be null.");
        }
        //Skill with ID already exists
        else if (skillRepository.findById(skill.getId()).isPresent()) {
            throw new EntityExistsException("Skill entity with ID (" + skill.getId() + ") already exists.");
        }

        return skillRepository.save(skill);
    }

    @Transactional
    public Skill updateSkill(Integer tenantId, Skill skill) {

        validateTenantIdParameter(tenantId, skill);

        Optional<Skill> skillOptional = skillRepository.findById(skill.getId());

        //If name is null
        if (skill.getName() == null) {
            throw new IllegalStateException("Skill entity name with tenantId (" + tenantId + ") cannot be null.");
        }
        //Skill not found
        else if (!skillOptional.isPresent()) {
            throw new EntityNotFoundException("Skill entity with ID (" + skill.getId() + ") not found.");
        }
        //Skill changes tenantId
        else if (!skillOptional.get().getTenantId().equals(skill.getTenantId())) {
            throw new IllegalStateException("Skill entity with tenantId (" + skillOptional.get().getTenantId()
                    + ") cannot change tenants.");
        }

        skillRepository.deleteById(skill.getId());
        return skillRepository.save(skill);
    }
}
