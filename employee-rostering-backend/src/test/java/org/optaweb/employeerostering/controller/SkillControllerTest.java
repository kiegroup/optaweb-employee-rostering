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

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.domain.Skill;
import org.optaweb.employeerostering.service.SkillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class SkillControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(SkillControllerTest.class);

    @Autowired
    private SkillService skillService;

    @Test
    public void getSkillListTest() {
        Integer tenantId = 1;
        Integer tenantId2 = 2;
        String name = "name";
        String name2 = "name2";

        Skill skill = new Skill(tenantId, name);
        Skill skill2 = new Skill(tenantId, name2);
        Skill skill3 = new Skill(tenantId2, name);

        skillService.createSkill(tenantId, skill);
        skillService.createSkill(tenantId, skill2);
        skillService.createSkill(tenantId2, skill3);

        List<Skill> skillList = skillService.getSkillList(tenantId);
        List<Skill> skillList2 = skillService.getSkillList(tenantId2);

        assertEquals(skillList.get(0), skill);
        assertEquals(skillList.get(1), skill2);
        assertEquals(skillList2.get(0), skill3);
    }

    @Test
    public void getSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        skillService.createSkill(tenantId, skill);

        Skill returnSkill = skillService.getSkill(tenantId, skill.getId());

        assertEquals(returnSkill.getTenantId(), (Integer) 1);
        assertEquals(returnSkill.getName(), "name");
    }

    @Test
    public void getNonExistentSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        skillService.createSkill(tenantId, skill);

        try {
            skillService.getSkill(1, -1L);
        }
        catch (EntityNotFoundException e) {
            String expectedMessage = "No Skill entity found with ID (-1).";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void getNonMatchingSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        skillService.createSkill(tenantId, skill);

        try {
            skillService.getSkill(2, skill.getId());
        }
        catch (IllegalStateException e) {
            String expectedMessage = "The tenantId (2) does not match the persistable (name)'s tenantId (1).";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void deleteSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        skillService.createSkill(tenantId, skill);

        assertEquals(skillService.deleteSkill(tenantId, skill.getId()), true);
    }

    @Test
    public void deleteNonExistentSkillTest() {
        try {
            skillService.deleteSkill(1, -1L);
        }
        catch (EntityNotFoundException e) {
            String expectedMessage = "No Skill entity found with ID (-1).";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void deleteNonMatchingSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        skillService.createSkill(tenantId, skill);

        try {
            skillService.deleteSkill(2, skill.getId());
        }
        catch (IllegalStateException e) {
            String expectedMessage = "The tenantId (2) does not match the persistable (name)'s tenantId (1).";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void createSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        Skill returnSkill = skillService.createSkill(tenantId, skill);

        assertEquals(returnSkill.getTenantId(), (Integer) 1);
        assertEquals(returnSkill.getName(), "name");
    }

    @Test
    public void createNonMatchingSkillTest(){
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        try {
            skillService.createSkill(2, skill);
        }
        catch (IllegalStateException e) {
            String expectedMessage = "The tenantId (2) does not match the persistable (name)'s tenantId (1).";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void updateSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        skillService.createSkill(tenantId, skill);

        Skill skill2 = new Skill(tenantId, "name2");
        skill2.setId(skill.getId());

        Skill returnSkill = skillService.updateSkill(tenantId, skill2);

        assertEquals(returnSkill.getTenantId(), (Integer) 1);
        assertEquals(returnSkill.getName(), "name2");
    }

    @Test
    public void updateNonMatchingSkillTest(){
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        skillService.createSkill(tenantId, skill);

        Skill skill2 = new Skill(tenantId, "name2");

        try {
            skillService.updateSkill(2, skill2);
        }
        catch (IllegalStateException e) {
            String expectedMessage = "The tenantId (2) does not match the persistable (name2)'s tenantId (1).";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void updateNonExistentSkillTest() {
        Skill skill = new Skill(1, "name");
        skill.setId(-1L);

        try {
            skillService.updateSkill(1, skill);
        }
        catch (EntityNotFoundException e) {
            String expectedMessage = "Skill entity with ID (-1) not found.";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void updateChangeTenantIdSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        skillService.createSkill(tenantId, skill);

        Skill skill2 = new Skill(2, name);
        skill2.setId(skill.getId());

        try {
            skillService.updateSkill(2, skill2);
        }
        catch (IllegalStateException e) {
            String expectedMessage = "Skill entity with tenantId (1) cannot change tenants.";
            assertEquals(expectedMessage, e.getMessage());
        }
    }
}
