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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.domain.Skill;
import org.optaweb.employeerostering.domain.Spot;
import org.optaweb.employeerostering.service.SkillService;
import org.optaweb.employeerostering.service.SpotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class SpotControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(SpotControllerTest.class);

    @Autowired
    private SpotService spotService;

    @Autowired
    private SkillService skillService;

    private Skill createSkill(Integer tenantId, String name) {
        Skill skill = new Skill(tenantId, name);
        Skill out = skillService.createSkill(tenantId, skill);
        return out;
    }

    @Test
    public void getSpotListTest() {
        Integer tenantId = 1;
        Integer tenantId2 = 2;
        String name = "name";
        String name2 = "name2";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, testSkillSet);
        Spot spot2 = new Spot(tenantId, name2, testSkillSet);
        Spot spot3 = new Spot(tenantId2, name, testSkillSet);

        spotService.createSpot(tenantId, spot);
        spotService.createSpot(tenantId, spot2);
        spotService.createSpot(tenantId2, spot3);

        List<Spot> spotList = spotService.getSpotList(tenantId);
        List<Spot> spotList2 = spotService.getSpotList(tenantId2);

        assertEquals(spotList.get(0), spot);
        assertEquals(spotList.get(1), spot2);
        assertEquals(spotList2.get(0), spot3);
    }

    @Test
    public void getSpotTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, testSkillSet);

        spotService.createSpot(tenantId, spot);

        Spot returnSpot = spotService.getSpot(tenantId, spot.getId());

        assertEquals(returnSpot.getTenantId(), (Integer) 1);
        assertEquals(returnSpot.getName(), "name");
        assertEquals(returnSpot.getRequiredSkillSet(), spot.getRequiredSkillSet());
    }

    @Test
    public void getNonExistentSpotTest() {
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> spotService.getSpot(1, -1L))
                .withMessage("No Spot entity found with ID (-1).");
    }

    @Test
    public void getNonMatchingSpotTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, testSkillSet);

        spotService.createSpot(tenantId, spot);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> spotService.getSpot(2, spot.getId()))
                .withMessage("The tenantId (2) does not match the persistable (name)'s tenantId (1).");
    }

    @Test
    public void deleteSpotTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, testSkillSet);

        spotService.createSpot(tenantId, spot);

        assertEquals(spotService.deleteSpot(tenantId, spot.getId()), true);
    }

    @Test
    public void deleteNonExistentSpotTest() {
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> spotService.deleteSpot(1, -1L))
                .withMessage("No Spot entity found with ID (-1).");
    }

    @Test
    public void deleteNonMatchingSpotTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, testSkillSet);

        spotService.createSpot(tenantId, spot);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> spotService.deleteSpot(2, spot.getId()))
                .withMessage("The tenantId (2) does not match the persistable (name)'s tenantId (1).");
    }

    @Test
    public void createSpotTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, testSkillSet);

        Spot returnSpot = spotService.createSpot(tenantId, spot);

        assertEquals(returnSpot.getTenantId(), (Integer) 1);
        assertEquals(returnSpot.getName(), "name");
        assertEquals(returnSpot.getRequiredSkillSet(), spot.getRequiredSkillSet());
    }

    @Test
    public void createNonMatchingSpotTest(){
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, testSkillSet);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> spotService.createSpot(2, spot))
                .withMessage("The tenantId (2) does not match the persistable (name)'s tenantId (1).");
    }

    @Test
    public void updateSpotTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, Collections.emptySet());

        spotService.createSpot(tenantId, spot);

        Spot spot2 = new Spot(tenantId, "name2", testSkillSet);
        spot2.setId(spot.getId());

        Spot returnSpot = spotService.updateSpot(tenantId, spot2);

        assertEquals(returnSpot.getTenantId(), (Integer) 1);
        assertEquals(returnSpot.getName(), "name2");
        assertEquals(returnSpot.getRequiredSkillSet(), spot2.getRequiredSkillSet());
    }

    @Test
    public void updateNonMatchingSpotTest(){
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, Collections.emptySet());

        spotService.createSpot(tenantId, spot);

        Spot spot2 = new Spot(tenantId, "name2", testSkillSet);

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> spotService.updateSpot(2, spot2))
                .withMessage("The tenantId (2) does not match the persistable (name2)'s tenantId (1).");
    }

    @Test
    public void updateNonExistentSpotTest() {
        Spot spot = new Spot(1, "name", Collections.emptySet());
        spot.setId(-1L);

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> spotService.updateSpot(1, spot))
                .withMessage("Spot entity with ID (-1) not found.");
    }

    @Test
    public void updateChangeTenantIdSpotTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skillA = createSkill(tenantId, "A");
        Skill skillB = createSkill(tenantId, "B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot spot = new Spot(tenantId, name, Collections.emptySet());

        spotService.createSpot(tenantId, spot);

        Spot spot2 = new Spot(2, name, testSkillSet);
        spot2.setId(spot.getId());

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> spotService.updateSpot(2, spot2))
                .withMessage("Spot entity with tenantId (1) cannot change tenants.");
    }
}
