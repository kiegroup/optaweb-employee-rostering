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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.domain.Skill;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SkillControllerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(SkillControllerIntegrationTest.class);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SkillService skillService;

    @Test
    public void getSkillListTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .get("/tenant/{tenantId}/skill", 0)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void getSkillTest() throws Exception {

        int tenantId = 1;
        String name = "name1";

        Skill skill = new Skill(tenantId, name);
        skill.setId(1L);

        skillService.createSkill(tenantId, skill);

        mvc.perform(MockMvcRequestBuilders
                            .get("/tenant/{tenantId}/skill/{id}", tenantId, skill.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId", is(tenantId)))
                .andExpect(jsonPath("$.name", is(name)));
    }

    @Test
    public void deleteSkillTest() throws Exception {

        int tenantId = 2;
        String name = "name2";

        Skill skill = new Skill(tenantId, name);
        skill.setId(2L);

        skillService.createSkill(tenantId, skill);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/tenant/{tenantId}/skill/{id}", tenantId, skill.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void createSkillTest() throws Exception {

        int tenantId = 3;
        String name = "name3";

        Skill skill = new Skill(tenantId, name);
        skill.setId(5L);

        String body = (new ObjectMapper()).writeValueAsString(skill);
        mvc.perform(MockMvcRequestBuilders
                            .post("/tenant/{tenantId}/skill/add", tenantId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId", is(tenantId)))
                .andExpect(jsonPath("$.name", is(name)));
    }

    @Test
    public void updateSkillTest() throws Exception {

        int tenantId = 4;
        String name = "name4";

        Skill skill = new Skill(tenantId, name);
        skill.setId(4L);

        skillService.createSkill(tenantId, skill);

        String body = (new ObjectMapper()).writeValueAsString(skill);
        mvc.perform(MockMvcRequestBuilders
                            .put("/tenant/{tenantId}/skill/update", tenantId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId", is(tenantId)))
                .andExpect(jsonPath("$.name", is(name)));
    }
}
