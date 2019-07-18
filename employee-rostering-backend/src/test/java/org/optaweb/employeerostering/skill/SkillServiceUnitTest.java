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

import javax.transaction.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.service.SkillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.util.NestedServletException;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class SkillServiceUnitTest {

    private static final Logger logger = LoggerFactory.getLogger(SkillServiceUnitTest.class);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SkillService skillService;

    @Test
    public void getSkillListTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/skill", 1)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void getSkillTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        skillService.createSkill(tenantId, skill);

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/skill/{id}", tenantId, skill.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(tenantId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(name));
    }

    @Test
    public void getNonExistentSkillTest() {
        Integer tenantId = 1;

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .get("/rest/tenant/{tenantId}/skill/{id}", tenantId, -1L)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                                     "Exception: No Skill entity found with ID (-1).");
    }

    @Test
    public void getNonMatchingSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        skillService.createSkill(tenantId, skill);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .get("/rest/tenant/{tenantId}/skill/{id}", 2,
                                                           skill.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: The " +
                                     "tenantId (2) does not match the persistable (name)'s tenantId (1).");
    }

    @Test
    public void deleteSkillTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        skillService.createSkill(tenantId, skill);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/skill/{id}", tenantId, skill.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteNonExistentSkillTest() {
        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .delete("/rest/tenant/{tenantId}/skill/{id}", 1, -1L)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                                     "Exception: No Skill entity found with ID (-1).");
    }

    @Test
    public void deleteNonMatchingSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        skillService.createSkill(tenantId, skill);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .delete("/rest/tenant/{tenantId}/skill/{id}", 2, skill.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (2) does not match the persistable (name)'s tenantId (1).");
    }

    @Test
    public void createSkillTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);
        String body = (new ObjectMapper()).writeValueAsString(skill);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/skill/add", tenantId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(tenantId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(name));
    }

    @Test
    public void createNonMatchingSkillTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);
        String body = (new ObjectMapper()).writeValueAsString(skill);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .post("/rest/tenant/{tenantId}/skill/add", 2)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (2) does not match the persistable (name)'s tenantId (1).");
    }

    @Test
    public void updateSkillTest() throws Exception {

        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        skillService.createSkill(tenantId, skill);

        Skill skill2 = new Skill(tenantId, "name2");
        skill2.setId(skill.getId());
        String body = (new ObjectMapper()).writeValueAsString(skill2);

        mvc.perform(MockMvcRequestBuilders
                            .put("/rest/tenant/{tenantId}/skill/update", tenantId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(tenantId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("name2"));
    }

    @Test
    public void updateNonMatchingSkillTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        skillService.createSkill(tenantId, skill);

        Skill skill2 = new Skill(tenantId, "name2");
        String body = (new ObjectMapper()).writeValueAsString(skill2);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/skill/update", 2)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (2) does not match the persistable (name2)'s tenantId (1).");
    }

    @Test
    public void updateNonExistentSkillTest() throws Exception {
        Skill skill = new Skill(1, "name");
        skill.setId(-1L);
        String body = (new ObjectMapper()).writeValueAsString(skill);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/skill/update", 1)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                                     "Exception: Skill entity with ID (-1) not found.");
    }

    @Test
    public void updateChangeTenantIdSkillTest() throws Exception {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        skillService.createSkill(tenantId, skill);

        Skill skill2 = new Skill(2, name);
        skill2.setId(skill.getId());
        String body = (new ObjectMapper()).writeValueAsString(skill2);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/skill/update", 2)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalState" +
                                     "Exception: Skill entity with tenantId (1) cannot change tenants.");
    }
}
