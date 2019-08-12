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

import java.util.Set;

import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;
import org.optaweb.employeerostering.service.rotation.RotationService;
import org.optaweb.employeerostering.service.spot.SpotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

//@RunWith(SpringRunner.class)
//@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RotationServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(RotationServiceTest.class);

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

    // TODO: Add createTestTenant() and deleteTestTenant() setup methods to persist tenant and rosterState entities
    //  before running tests once Tenant CRUD methods are implemented

    /*
    @Test
    public void getShiftTemplateListTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/rotation/", 2)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void getShiftTemplateTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";

        Spot spot = createSpot(tenantId, name, Collections.emptySet());

        ShiftTemplateView shiftTemplateView = new ShiftTemplateView(tenantId, spot.getId(), null, null, null);
        rotationService.createShiftTemplate(tenantId, shiftTemplateView);

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/rotation/{id}", tenantId, shiftTemplateView.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(tenantId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.spotId").value(spot.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.rotationEmployeeId").value(null));
    }

    @Test
    public void getNonExistentShiftTemplateTest() {
        Integer tenantId = 2;

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .get("/rest/tenant/{tenantId}/rotation/{id}", tenantId, -1L)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                                     "Exception: No ShiftTemplateView entity found with ID (-1).");
    }

    @Test
    public void getNonMatchingShiftTemplateTest() {
        Integer tenantId = 2;
        String name = "name";

        Spot spot = createSpot(tenantId, name, Collections.emptySet());

        ShiftTemplateView shiftTemplateView = new ShiftTemplateView(tenantId, spot.getId(), null, null, null);
        rotationService.createShiftTemplate(tenantId, shiftTemplateView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .get("/rest/tenant/{tenantId}/rotation/{id}", 3,
                                                           shiftTemplateView.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: The " +
                                     "tenantId (3) does not match the persistable (name)'s tenantId (2).");
    }

    @Test
    public void deleteShiftTemplateTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";

        Spot spot = createSpot(tenantId, name, Collections.emptySet());

        ShiftTemplateView shiftTemplateView = new ShiftTemplateView(tenantId, spot.getId(), null, null, null);
        rotationService.createShiftTemplate(tenantId, shiftTemplateView);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/rotation/{id}", tenantId, shiftTemplateView.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andExpect(content().string("true"));
    }

    @Test
    public void deleteNonExistentShiftTemplateTest() {
        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .delete("/rest/tenant/{tenantId}/rotation/{id}", 2, -1L)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                                     "Exception: No ShiftTemplateView entity found with ID (-1).");
    }

    @Test
    public void deleteNonMatchingShiftTemplateTest() {
        Integer tenantId = 2;
        String name = "name";

        Spot spot = createSpot(tenantId, name, Collections.emptySet());

        ShiftTemplateView shiftTemplateView = new ShiftTemplateView(tenantId, spot.getId(), null, null, null);
        rotationService.createShiftTemplate(tenantId, shiftTemplateView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .delete("/rest/tenant/{tenantId}/rotation/{id}", 3,
                                                              shiftTemplateView.getId())))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (3) does not match the persistable (name)'s tenantId (2).");
    }

    @Test
    public void createShiftTemplateTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";

        Spot spot = createSpot(tenantId, name, Collections.emptySet());

        ShiftTemplateView shiftTemplateView = new ShiftTemplateView(tenantId, spot.getId(), null, null, null);
        String body = (new ObjectMapper()).writeValueAsString(shiftTemplateView);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/rotation/add", tenantId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(tenantId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.spotId").value(spot.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.rotationEmployeeId").value(null));
    }

    @Test
    public void createNonMatchingShiftTemplateTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";

        Spot spot = createSpot(tenantId, name, Collections.emptySet());

        ShiftTemplateView shiftTemplateView = new ShiftTemplateView(tenantId, spot.getId(), null, null, null);
        String body = (new ObjectMapper()).writeValueAsString(shiftTemplateView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .post("/rest/tenant/{tenantId}/rotation/add", 3)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (3) does not match the persistable (name)'s tenantId (2).");
    }

    @Test
    public void updateShiftTemplateTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";
        String name2 ="name2";

        Spot spot = createSpot(tenantId, name, Collections.emptySet());
        Spot spot2 = createSpot(tenantId, name2, Collections.emptySet());

        ShiftTemplateView shiftTemplateView = new ShiftTemplateView(tenantId, spot.getId(), null, null, null);
        rotationService.createShiftTemplate(tenantId, shiftTemplateView);

        ShiftTemplateView shiftTemplateView2 = new ShiftTemplateView(tenantId, spot2.getId(), null, null, null);
        String body = (new ObjectMapper()).writeValueAsString(shiftTemplateView2);

        mvc.perform(MockMvcRequestBuilders
                            .put("/rest/tenant/{tenantId}/rotation/update", tenantId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tenantId").value(tenantId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.spotId").value(spot2.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.rotationEmployeeId").value(null));
    }

    @Test
    public void updateNonMatchingShiftTemplateTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";
        String name2 = "name2";

        Spot spot = createSpot(tenantId, name, Collections.emptySet());
        Spot spot2 = createSpot(tenantId, name2, Collections.emptySet());

        ShiftTemplateView shiftTemplateView = new ShiftTemplateView(tenantId, spot.getId(), null, null, null);
        rotationService.createShiftTemplate(tenantId, shiftTemplateView);

        ShiftTemplateView shiftTemplateView2 = new ShiftTemplateView(tenantId, spot2.getId(), null, null, null);
        String body = (new ObjectMapper()).writeValueAsString(shiftTemplateView2);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/rotation/update", 3)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalStateException: " +
                                     "The tenantId (3) does not match the persistable (name2)'s tenantId (2).");
    }

    @Test
    public void updateNonExistentShiftTemplateTest() throws Exception {
        Integer tenantId = 2;
        String name = "name";

        Spot spot = createSpot(tenantId, name, Collections.emptySet());

        ShiftTemplateView shiftTemplateView = new ShiftTemplateView(tenantId, spot.getId(), null, null, null);
        shiftTemplateView.setId(-1L);
        String body = (new ObjectMapper()).writeValueAsString(shiftTemplateView);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/rotation/update", tenantId)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is javax.persistence.EntityNotFound" +
                                     "Exception: ShiftTemplateView entity with ID (-1) not found.");
    }

    @Test
    public void updateChangeTenantIdShiftTemplateTest() throws Exception {
        Integer tenantId = 2;
        Integer tenantId2 = 3;
        String name = "name";
        String name2 ="name2";

        Spot spot = createSpot(tenantId, name, Collections.emptySet());
        Spot spot2 = createSpot(tenantId, name2, Collections.emptySet());

        ShiftTemplateView shiftTemplateView = new ShiftTemplateView(tenantId, spot.getId(), null, null, null);
        rotationService.createShiftTemplate(tenantId, shiftTemplateView);

        ShiftTemplateView shiftTemplateView2 = new ShiftTemplateView(tenantId, spot2.getId(), null, null, null);
        String body = (new ObjectMapper()).writeValueAsString(shiftTemplateView2);

        assertThatExceptionOfType(NestedServletException.class)
                .isThrownBy(() -> mvc.perform(MockMvcRequestBuilders
                                                      .put("/rest/tenant/{tenantId}/rotation/update", tenantId2)
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .content(body)))
                .withMessage("Request processing failed; nested exception is java.lang.IllegalState" +
                                     "Exception: ShiftTemplateView entity with tenantId (2) cannot change tenants.");
    }*/
}
