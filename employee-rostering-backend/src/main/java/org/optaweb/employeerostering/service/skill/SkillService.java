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

import org.optaweb.employeerostering.domain.Skill;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

public interface SkillService {

    @GetMapping("/tenant/{tenantId}/skill")
    public String getSkillList(Integer tenantId);

    @GetMapping("/tenant/{tenantId}/skill/{id}")
    public String getSkill(Integer tenantId, Long id);

    @DeleteMapping("/tenant/{tenantId}/skill/{id}")
    public void deleteSkill(Integer tenantId, Long id);

    @PostMapping("/tenant/{tenantId}/skill/add")
    public void createSkill(Integer tenantId, Skill skill);

    @PutMapping("/tenant/{tenantId}/skill/update")
    public void updateSkill(Integer tenantId, Skill skill);
}
