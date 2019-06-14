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

import java.util.List;

import org.optaweb.employeerostering.domain.Skill;
import org.optaweb.employeerostering.service.skill.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List> getSkillList(@PathVariable Integer tenantId) {
        return new ResponseEntity<List>(skillService.getSkillList(tenantId), HttpStatus.OK);
    }

    @GetMapping("/tenant/{tenantId}/skill/{id}")
    public ResponseEntity<Skill> getSkill(@PathVariable Integer tenantId, @PathVariable Long id) {
        return new ResponseEntity<Skill>(skillService.getSkill(tenantId, id), HttpStatus.OK);
    }

    @DeleteMapping("/tenant/{tenantId}/skill/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Integer tenantId, @PathVariable Long id) {
        skillService.deleteSkill(tenantId, id);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @PostMapping("/tenant/{tenantId}/skill/add")
    public ResponseEntity<Void> createSkill(@PathVariable Integer tenantId, @RequestBody Skill skill) {
        skillService.createSkill(tenantId, skill);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @PutMapping("/tenant/{tenantId}/skill/update")
    public ResponseEntity<Void> updateSkill(@PathVariable Integer tenantId, @RequestBody Skill skill) {
        skillService.updateSkill(tenantId, skill);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }
}