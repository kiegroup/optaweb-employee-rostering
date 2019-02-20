/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.webapp.spot;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.optaweb.employeerostering.shared.skill.Skill;
import org.optaweb.employeerostering.shared.skill.SkillRestService;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.spot.SpotRestService;
import org.optaweb.employeerostering.webapp.AbstractEntityRequireTenantRestServiceIT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SpotRestServiceIT extends AbstractEntityRequireTenantRestServiceIT {

    private SpotRestService spotRestService;
    private SkillRestService skillRestService;

    public SpotRestServiceIT() {
        spotRestService = serviceClientFactory.createSpotRestServiceClient();
        skillRestService = serviceClientFactory.createSkillRestServiceClient();
    }

    @Before
    public void setup() {
        createTestTenant();
    }

    @After
    public void cleanup() {
        deleteTestTenant();
    }

    private Skill createSkill(String name) {
        Skill skill = new Skill(TENANT_ID, name);
        Skill out = skillRestService.addSkill(TENANT_ID, skill);
        assertClientResponseOk();
        return out;
    }

    @Test
    public void testDeleteNonExistingSpot() {
        final long nonExistingSpotId = 123456L;
        boolean result = spotRestService.removeSpot(TENANT_ID, nonExistingSpotId);
        assertThat(result).isFalse();
        assertClientResponseOk();
    }

    @Test
    public void testUpdateNonExistingSpot() {
        final long nonExistingSpotId = 123456L;
        Spot nonExistingSpot = new Spot(TENANT_ID, "Non-existing spot", Collections.emptySet());
        nonExistingSpot.setId(nonExistingSpotId);
        Spot updatedSpot = spotRestService.updateSpot(TENANT_ID, nonExistingSpot);

        assertClientResponseOk();
        assertThat(updatedSpot.getName()).isEqualTo(nonExistingSpot.getName());
        assertThat(updatedSpot.getRequiredSkillSet()).isEqualTo(nonExistingSpot.getRequiredSkillSet());
        assertThat(updatedSpot.getId()).isNotNull().isNotEqualTo(nonExistingSpot.getId());
    }

    @Test
    public void testGetOfNonExistingSpot() {
        final long nonExistingSpotId = 123456L;
        assertThatExceptionOfType(javax.ws.rs.NotFoundException.class)
                .isThrownBy(() -> spotRestService.getSpot(TENANT_ID, nonExistingSpotId));
        assertClientResponseError(Response.Status.NOT_FOUND);
    }

    @Test
    public void testCrudSpot() {
        Skill skillA = createSkill("A");
        Skill skillB = createSkill("B");
        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Spot testAddSpot = new Spot(TENANT_ID, "Test Spot", testSkillSet);
        spotRestService.addSpot(TENANT_ID, testAddSpot);
        assertClientResponseOk();

        List<Spot> spots = spotRestService.getSpotList(TENANT_ID);
        assertClientResponseOk();
        assertThat(spots).usingElementComparatorIgnoringFields(IGNORED_FIELDS).containsExactly(testAddSpot);

        Spot testUpdateSpot = spots.get(0);
        testUpdateSpot.setName("ZZZ");

        Skill skillC = createSkill("C");
        testSkillSet.remove(skillA);
        testSkillSet.add(skillC);

        testUpdateSpot.setRequiredSkillSet(testSkillSet);
        spotRestService.updateSpot(TENANT_ID, testUpdateSpot);

        Spot retrievedSpot = spotRestService.getSpot(TENANT_ID, testUpdateSpot.getId());
        assertClientResponseOk();
        assertThat(retrievedSpot).isNotNull().isEqualToIgnoringGivenFields(testUpdateSpot, "version");

        boolean result = spotRestService.removeSpot(TENANT_ID, retrievedSpot.getId());
        assertThat(result).isTrue();
        assertClientResponseOk();

        spots = spotRestService.getSpotList(TENANT_ID);
        assertThat(spots).isEmpty();
    }
}
