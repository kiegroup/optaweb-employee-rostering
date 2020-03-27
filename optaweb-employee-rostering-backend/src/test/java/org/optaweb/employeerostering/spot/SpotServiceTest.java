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

package org.optaweb.employeerostering.spot;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.skill.view.SkillView;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;
import org.optaweb.employeerostering.service.skill.SkillService;
import org.optaweb.employeerostering.service.spot.SpotService;
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
public class SpotServiceTest extends AbstractEntityRequireTenantRestServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(SpotServiceTest.class);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private SpotService spotService;

    @Autowired
    private SkillService skillService;

    private Skill createSkill(Integer tenantId, String name) {
        SkillView skillView = new SkillView(tenantId, name);
        return skillService.createSkill(tenantId, skillView);
    }

    @Before
    public void setup() {
        createTestTenant();
    }

    @After
    public void cleanup() {
        deleteTestTenant();
    }

    @Test
    public void getSpotListTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/spot/", TENANT_ID)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void getSpotTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet, false);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/spot/{id}", TENANT_ID, spot.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("spot"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.requiredSkillSet").isNotEmpty());
    }

    @Test
    public void getNonExistentSpotTest() throws Exception {
        String exceptionMessage = "No Spot entity found with ID (0).";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/spot/{id}", TENANT_ID, 0)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void getNonMatchingSpotTest() throws Exception {
        String exceptionMessage = "The tenantId (0) does not match the persistable (spot)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet, false);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/spot/{id}", 0, spot.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void deleteSpotTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet, false);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/spot/{id}", TENANT_ID, spot.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("true"));
    }

    @Test
    public void deleteNonExistentSpotTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/spot/{id}", TENANT_ID, 0)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("false"));
    }

    @Test
    public void deleteNonMatchingSpotTest() throws Exception {
        String exceptionMessage = "The tenantId (0) does not match the persistable (spot)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet, false);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/spot/{id}", 0, spot.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void createSpotTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet, false);
        String body = (new ObjectMapper()).writeValueAsString(spotView);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/spot/add", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("spot"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.requiredSkillSet").isNotEmpty());
    }

    @Test
    public void createNonMatchingSpotTest() throws Exception {
        String exceptionMessage = "The tenantId (0) does not match the persistable (spot)'s tenantId ("
                + TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet, false);
        String body = (new ObjectMapper()).writeValueAsString(spotView);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/spot/add", 0)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void updateSpotTest() throws Exception {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet, false);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        SpotView updatedSpot = new SpotView(TENANT_ID, "updatedSpot", testSkillSet, false);
        updatedSpot.setId(spot.getId());
        String body = (new ObjectMapper()).writeValueAsString(updatedSpot);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/spot/update", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("updatedSpot"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.requiredSkillSet").isNotEmpty());
    }

    @Test
    public void updateNonMatchingSpotTest() throws Exception {
        String exceptionMessage = "The tenantId (0) does not match the persistable (updatedSpot)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet, false);
        spotService.createSpot(TENANT_ID, spotView);

        SpotView updatedSpot = new SpotView(TENANT_ID, "updatedSpot", testSkillSet, false);
        String body = (new ObjectMapper()).writeValueAsString(updatedSpot);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/spot/update", 0)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void updateNonExistentSpotTest() throws Exception {
        String exceptionMessage = "Spot entity with ID (0) not found.";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        SpotView spotView = new SpotView(TENANT_ID, "spot", Collections.emptySet(), false);
        spotView.setId(0L);
        String body = (new ObjectMapper()).writeValueAsString(spotView);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/spot/update", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void updateChangeTenantIdSpotTest() throws Exception {
        String exceptionMessage = "Spot entity with tenantId (" + TENANT_ID + ") cannot change tenants.";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        SpotView spotView = new SpotView(TENANT_ID, "spot", testSkillSet, false);
        Spot spot = spotService.createSpot(TENANT_ID, spotView);

        SpotView updatedSpot = new SpotView(0, "updatedSpot", testSkillSet, false);
        updatedSpot.setId(spot.getId());
        String body = (new ObjectMapper()).writeValueAsString(updatedSpot);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/spot/update", 0)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }
}
