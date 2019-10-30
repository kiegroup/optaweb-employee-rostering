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

package org.optaweb.employeerostering.skill;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.skill.view.SkillView;
import org.optaweb.employeerostering.service.skill.SkillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@Transactional
public class SkillServiceTest extends AbstractEntityRequireTenantRestServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(SkillServiceTest.class);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SkillService skillService;

    @Before
    public void setup() {
        createTestTenant();
    }

    @After
    public void cleanup() {
        deleteTestTenant();
    }

    @Test
    public void getSkillListTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/skill/", TENANT_ID)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void getSkillTest() throws Exception {
        SkillView skillView = new SkillView(TENANT_ID, "skill");
        Skill skill = skillService.createSkill(TENANT_ID, skillView);

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/skill/{id}", TENANT_ID, skill.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("skill"));
    }

    @Test
    public void getNonExistentSkillTest() throws Exception {
        String exceptionMessage = "No Skill entity found with ID (0).";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/skill/{id}", TENANT_ID, 0)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void getNonMatchingSkillTest() throws Exception {
        String exceptionMessage = "The tenantId (0) does not match the persistable (skill)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        SkillView skillView = new SkillView(TENANT_ID, "skill");
        Skill skill = skillService.createSkill(TENANT_ID, skillView);

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/skill/{id}", 0, skill.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void deleteSkillTest() throws Exception {
        SkillView skillView = new SkillView(TENANT_ID, "skill");
        Skill skill = skillService.createSkill(TENANT_ID, skillView);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/skill/{id}", TENANT_ID, skill.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("true"));
    }

    @Test
    public void deleteNonExistentSkillTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/skill/{id}", TENANT_ID, 0)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("false"));
    }

    @Test
    public void deleteNonMatchingSkillTest() throws Exception {
        String exceptionMessage = "The tenantId (0) does not match the persistable (skill)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        SkillView skillView = new SkillView(TENANT_ID, "skill");
        Skill skill = skillService.createSkill(TENANT_ID, skillView);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/skill/{id}", 0, skill.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void createSkillTest() throws Exception {
        SkillView skillView = new SkillView(TENANT_ID, "skill");
        String body = (new ObjectMapper()).writeValueAsString(skillView);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/skill/add", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("skill"));
    }

    @Test
    public void createNonMatchingSkillTest() throws Exception {
        String exceptionMessage = "The tenantId (0) does not match the persistable (skill)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        SkillView skillView = new SkillView(TENANT_ID, "skill");
        String body = (new ObjectMapper()).writeValueAsString(skillView);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/skill/add", 0)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void updateSkillTest() throws Exception {
        SkillView skillView = new SkillView(TENANT_ID, "skill");
        Skill skill = skillService.createSkill(TENANT_ID, skillView);

        SkillView updatedSkill = new SkillView(TENANT_ID, "updatedSkill");
        updatedSkill.setId(skill.getId());
        String body = (new ObjectMapper()).writeValueAsString(updatedSkill);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/skill/update", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("updatedSkill"));
    }

    @Test
    public void updateNonMatchingSkillTest() throws Exception {
        String exceptionMessage = "The tenantId (0) does not match the persistable (updatedSkill)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        SkillView skillView = new SkillView(TENANT_ID, "skill");
        Skill skill = skillService.createSkill(TENANT_ID, skillView);

        SkillView updatedSkill = new SkillView(TENANT_ID, "updatedSkill");
        String body = (new ObjectMapper()).writeValueAsString(updatedSkill);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/skill/update", 0)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void updateNonExistentSkillTest() throws Exception {
        String exceptionMessage = "Skill entity with ID (0) not found.";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        SkillView skillView = new SkillView(TENANT_ID, "skill");
        skillView.setId(0L);
        String body = (new ObjectMapper()).writeValueAsString(skillView);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/skill/update", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void updateChangeTenantIdSkillTest() throws Exception {
        String exceptionMessage = "Skill entity with tenantId (" + TENANT_ID + ") cannot change tenants.";
        String exceptionClass = "java.lang.IllegalStateException";

        SkillView skillView = new SkillView(TENANT_ID, "skill");
        Skill skill = skillService.createSkill(TENANT_ID, skillView);

        SkillView updatedSkill = new SkillView(0, "updatedSkill");
        updatedSkill.setId(skill.getId());
        String body = (new ObjectMapper()).writeValueAsString(updatedSkill);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/skill/update", 0)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }
}
