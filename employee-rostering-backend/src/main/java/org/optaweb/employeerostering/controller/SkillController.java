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

package org.optaweb.employeerostering.controller;

import org.optaweb.employeerostering.domain.Skill;
import org.optaweb.employeerostering.service.skill.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SkillController {

    @Autowired
    private SkillService skillService;

    @GetMapping("/tenant/{tenantId}/skill")
    public String getSkillList(@PathVariable Integer tenantId) {
        return skillService.getSkillList(tenantId);
    }

    @GetMapping("/tenant/{tenantId}/skill/{id}")
    public String getSkill(@PathVariable Integer tenantId, @PathVariable Long id) {
        return skillService.getSkill(tenantId, id);
    }

    @DeleteMapping("/tenant/{tenantId}/skill/{id}")
    public String deleteSkill(@PathVariable Integer tenantId, @PathVariable Long id) {
        skillService.deleteSkill(tenantId, id);
        return "Delete a skill";
    }

    @PostMapping("/tenant/{tenantId}/skill/add")
    public String createSkill(@PathVariable Integer tenantId, @RequestBody Skill skill) {
        skillService.createSkill(tenantId, skill);
        return "Create a skill";
    }

    @PutMapping("/tenant/{tenantId}/skill/update")
    public String updateSkill(@PathVariable Integer tenantId, @RequestBody Skill skill) {
        skillService.updateSkill(tenantId, skill);
        return "Update a skill";
    }
}
