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

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.skill.view.SkillView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureTestDatabase
public class SkillRestControllerTest extends AbstractEntityRequireTenantRestServiceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String skillPathURI = "http://localhost:8080/rest/tenant/{tenantId}/skill/";

    private ResponseEntity<List<Skill>> getSkills(Integer tenantId) {
        return restTemplate.exchange(skillPathURI, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Skill>>() {
                }, tenantId);
    }

    private ResponseEntity<Skill> getSkill(Integer tenantId, Long id) {
        return restTemplate.getForEntity(skillPathURI + id, Skill.class, tenantId);
    }

    private void deleteSkill(Integer tenantId, Long id) {
        restTemplate.delete(skillPathURI + id, tenantId);
    }

    private ResponseEntity<Skill> addSkill(Integer tenantId, SkillView skillView) {
        return restTemplate.postForEntity(skillPathURI + "add", skillView, Skill.class, tenantId);
    }

    private ResponseEntity<Skill> updateSkill(Integer tenantId, SkillView skillView) {
        return restTemplate.postForEntity(skillPathURI + "update", skillView, Skill.class, tenantId);
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
    public void skillCrudTest() {
        SkillView skillView = new SkillView(TENANT_ID, "skill");
        ResponseEntity<Skill> postResponse = addSkill(TENANT_ID, skillView);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Skill> response = getSkill(TENANT_ID, postResponse.getBody().getId());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualToComparingFieldByFieldRecursively(postResponse.getBody());

        SkillView updatedSkill = new SkillView(TENANT_ID, "updatedSkill");
        updatedSkill.setId(postResponse.getBody().getId());
        ResponseEntity<Skill> putResponse = updateSkill(TENANT_ID, updatedSkill);
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = getSkill(TENANT_ID, putResponse.getBody().getId());
        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isEqualToComparingFieldByFieldRecursively(response.getBody());

        deleteSkill(TENANT_ID, postResponse.getBody().getId());

        ResponseEntity<List<Skill>> getListResponse = getSkills(TENANT_ID);
        assertThat(getListResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getListResponse.getBody()).isEmpty();
    }
}
