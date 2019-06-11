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

//SpringBootTest loads entire application, WebMvcTest only loads controller and its dependencies

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SkillControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void getSkillListTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .get("tenant/{tenantId}/skill", 0)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Get skill list"));
    }

    @Test
    public void getSkillTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .get("/tenant/{tenantId}/skill/{id}", 0)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Get a skill"));
    }

    @Test
    public void deleteSkillTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .delete("/tenant/{tenantId}/skill/{id}")
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Delete a skill"));
    }

    @Test
    public void createSkillTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .post("/tenant/{tenantId}/skill/add")
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Create a skill"));
    }

    @Test
    public void updateSkillTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .put("/tenant/{tenantId}/skill/update")
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Update a skill"));
    }
}
