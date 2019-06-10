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

package org.optaweb.employeerostering.domain;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SkillController {

    @GetMapping("/tenant/{tenantId}/skill")
    public String getSkillList(@PathVariable Integer tenantId) {
        return "Get skill list";
    }

    @GetMapping("/tenant/{tenantId}/skill/{id}")
    public String getSkill(@PathVariable Integer tenantId, @PathVariable Long id) {
        return "Get a skill";
    }

    @DeleteMapping("/tenant/{tenantId}/skill/{id}")
    public String deleteSkill(@PathVariable Integer tenantId, @PathVariable Long id) {
        return "Delete a skill";
    }

    @PostMapping("/tenant/{tenantId}/skill/add")
    public String createSkill(@PathVariable Integer tenantId, @RequestBody Skill skill) {
        return "Create a skill";
    }

    @PutMapping("/tenant/{tenantId}/skill/update")
    public String updateSkill(@PathVariable Integer tenantId, @RequestBody Skill skill) {
        return "Update a skill";
    }
}
