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

package org.optaweb.employeerostering.rotation;

import java.time.LocalTime;
import java.util.Collections;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.rotation.TimeBucket;
import org.optaweb.employeerostering.domain.rotation.view.TimeBucketView;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;
import org.optaweb.employeerostering.service.rotation.RotationService;
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
public class RotationServiceTest extends AbstractEntityRequireTenantRestServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(RotationServiceTest.class);
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MockMvc mvc;

    @Autowired
    private RotationService rotationService;

    @Autowired
    private SpotService spotService;

    private Spot createSpot(Integer tenantId, String name, Set<Skill> requiredSkillSet) {
        SpotView spotView = new SpotView(tenantId, name, requiredSkillSet);
        return spotService.createSpot(tenantId, spotView);
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
    public void getTimeBucketListTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/rotation/", TENANT_ID)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void getTimeBucketTest() throws Exception {
        Spot spot = createSpot(TENANT_ID, "spot", Collections.emptySet());

        TimeBucketView timeBucketView = new TimeBucketView(new TimeBucket(TENANT_ID, spot, LocalTime.of(9, 0),
                                                                          LocalTime.of(17, 0),
                                                                          Collections.emptySet(),
                                                                          Collections.emptySet(),
                                                                          Collections.emptyList()));
        TimeBucketView persistedTimeBucket = rotationService.createTimeBucket(TENANT_ID, timeBucketView);

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/rotation/{id}", TENANT_ID, persistedTimeBucket.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.spotId").value(spot.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.startTime").value("09:00:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.endTime").value("17:00:00"));
    }

    @Test
    public void getNonExistentTimeBucketTest() throws Exception {
        String exceptionMessage = "No TimeBucket entity found with ID (0).";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/rotation/{id}", TENANT_ID, 0)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void deleteTimeBucketTest() throws Exception {
        Spot spot = createSpot(TENANT_ID, "spot", Collections.emptySet());

        TimeBucketView timeBucketView = new TimeBucketView(new TimeBucket(TENANT_ID, spot, LocalTime.of(9, 0),
                                                                          LocalTime.of(17, 0),
                                                                          Collections.emptySet(),
                                                                          Collections.emptySet(),
                                                                          Collections.emptyList()));
        TimeBucketView persistedTimeBucket = rotationService.createTimeBucket(TENANT_ID, timeBucketView);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/rotation/{id}", TENANT_ID, persistedTimeBucket.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("true"));
    }

    @Test
    public void deleteNonMatchingTimeBucketTest() throws Exception {
        Spot spot = createSpot(TENANT_ID, "spot", Collections.emptySet());

        TimeBucketView timeBucketView = new TimeBucketView(new TimeBucket(TENANT_ID, spot, LocalTime.of(9, 0),
                                                                          LocalTime.of(17, 0),
                                                                          Collections.emptySet(),
                                                                          Collections.emptySet(),
                                                                          Collections.emptyList()));
        TimeBucketView persistedTimeBucket = rotationService.createTimeBucket(TENANT_ID, timeBucketView);

        String timeBucketName = "[TimeBucket-" + persistedTimeBucket.getId() + "]";

        String exceptionMessage = "The tenantId (0) does not match the persistable (" + timeBucketName +
                ")'s tenantId (" + TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/rotation/{id}", 0, persistedTimeBucket.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void createTimeBucketTest() throws Exception {
        Spot spot = createSpot(TENANT_ID, "spot", Collections.emptySet());

        TimeBucketView timeBucketView = new TimeBucketView(new TimeBucket(TENANT_ID, spot, LocalTime.of(9, 0),
                                                                          LocalTime.of(17, 0),
                                                                          Collections.emptySet(),
                                                                          Collections.emptySet(),
                                                                          Collections.emptyList()));
        String body = objectMapper.writeValueAsString(timeBucketView);
        
        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/rotation/add", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.spotId").value(spot.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.startTime").value("09:00:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.endTime").value("17:00:00"));
    }

    @Test
    public void createNonMatchingTimeBucketTest() throws Exception {
        Spot spot = createSpot(TENANT_ID, "spot", Collections.emptySet());

        TimeBucketView timeBucketView = new TimeBucketView(new TimeBucket(0, spot, LocalTime.of(9, 0),
                                                                          LocalTime.of(17, 0),
                                                                          Collections.emptySet(),
                                                                          Collections.emptySet(),
                                                                          Collections.emptyList()));
        String body = objectMapper.writeValueAsString(timeBucketView);

        String timeBucketName = "[TimeBucket-null]";

        String exceptionMessage = "The tenantId (" + TENANT_ID + ") does not match the persistable (" +
                timeBucketName + ")'s tenantId (0).";
        String exceptionClass = "java.lang.IllegalStateException";

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/rotation/add", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void updateTimeBucketTest() throws Exception {
        Spot spotA = createSpot(TENANT_ID, "A", Collections.emptySet());
        Spot spotB = createSpot(TENANT_ID, "B", Collections.emptySet());

        TimeBucketView timeBucketView = new TimeBucketView(new TimeBucket(TENANT_ID, spotA, LocalTime.of(9, 0),
                                                                          LocalTime.of(17, 0),
                                                                          Collections.emptySet(),
                                                                          Collections.emptySet(),
                                                                          Collections.emptyList()));
        
        TimeBucketView persistedTimeBucket = rotationService.createTimeBucket(TENANT_ID, timeBucketView);

        TimeBucketView updatedTimeBucket = new TimeBucketView(new TimeBucket(TENANT_ID, spotB, LocalTime.of(9, 0),
                                                                             LocalTime.of(17, 0),
                                                                             Collections.emptySet(),
                                                                             Collections.emptySet(),
                                                                             Collections.emptyList()));
        updatedTimeBucket.setId(persistedTimeBucket.getId());
        String body = objectMapper.writeValueAsString(updatedTimeBucket);

        mvc.perform(MockMvcRequestBuilders
                            .put("/rest/tenant/{tenantId}/rotation/update", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(TENANT_ID))
                .andExpect(MockMvcResultMatchers.jsonPath("$.spotId").value(spotB.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.startTime").value("09:00:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.endTime").value("17:00:00"));
    }

    @Test
    public void updateNonExistentTimeBucketTest() throws Exception {
        String exceptionMessage = "TimeBucket entity with ID (0) not found.";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        Spot spot = createSpot(TENANT_ID, "spot", Collections.emptySet());

        TimeBucketView timeBucketView = new TimeBucketView(new TimeBucket(TENANT_ID, spot, LocalTime.of(9, 0),
                                                                          LocalTime.of(17, 0),
                                                                          Collections.emptySet(),
                                                                          Collections.emptySet(),
                                                                          Collections.emptyList()));
        timeBucketView.setId(0L);
        String body = objectMapper.writeValueAsString(timeBucketView);

        mvc.perform(MockMvcRequestBuilders
                            .put("/rest/tenant/{tenantId}/rotation/update", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }
}
