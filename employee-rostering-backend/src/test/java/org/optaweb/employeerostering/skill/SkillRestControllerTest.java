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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.skill.view.SkillView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SkillRestControllerTest {

    @Autowired
    private TestRestTemplate skillRestTemplate;

    private String skillPathURI = "http://localhost:8080/rest/tenant/{tenantId}/skill/";

    private ResponseEntity<List<Skill>> getSkills(Integer tenantId) {
        return skillRestTemplate.exchange(skillPathURI, HttpMethod.GET, null,
                                          new ParameterizedTypeReference<List<Skill>>() {}, tenantId);
    }

    private ResponseEntity<Skill> getSkill(Integer tenantId, Long id) {
        return skillRestTemplate.getForEntity(skillPathURI + id, Skill.class, tenantId);
    }

    private void deleteSkill(Integer tenantId, Long id) {
        skillRestTemplate.delete(skillPathURI + id, tenantId);
    }

    private ResponseEntity<Skill> addSkill(Integer tenantId, SkillView skillView) {
        return skillRestTemplate.postForEntity(skillPathURI + "add", skillView, Skill.class, tenantId);
    }

    private ResponseEntity<Skill> updateSkill(Integer tenantId, HttpEntity<SkillView> request) {
        return skillRestTemplate.exchange(skillPathURI + "update", HttpMethod.PUT, request, Skill.class, tenantId);
    }

    @Test
    public void getSkillListTest() {
        Integer tenantId = 1;
        Integer tenantId2 = 2;
        String name = "name";
        String name2 = "name2";

        SkillView skillView = new SkillView(tenantId, name);
        SkillView skillView2 = new SkillView(tenantId, name2);
        SkillView skillView3 = new SkillView(tenantId2, name);

        ResponseEntity<Skill> postResponse = addSkill(tenantId, skillView);
        ResponseEntity<Skill> postResponse2 = addSkill(tenantId, skillView2);
        ResponseEntity<Skill> postResponse3 = addSkill(tenantId2, skillView3);

        ResponseEntity<List<Skill>> response = getSkills(tenantId);
        ResponseEntity<List<Skill>> response2 = getSkills(tenantId2);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(postResponse.getBody());
        assertThat(response.getBody()).contains(postResponse2.getBody());

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).contains(postResponse3.getBody());

        deleteSkill(tenantId, postResponse.getBody().getId());
        deleteSkill(tenantId, postResponse2.getBody().getId());
        deleteSkill(tenantId2, postResponse3.getBody().getId());
    }

    @Test
    public void getSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        SkillView skillView = new SkillView(tenantId, name);
        ResponseEntity<Skill> postResponse = addSkill(tenantId, skillView);

        ResponseEntity<Skill> response = getSkill(tenantId, postResponse.getBody().getId());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(postResponse.getBody());

        deleteSkill(tenantId, postResponse.getBody().getId());
    }

    @Test
    public void deleteSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        SkillView skillView = new SkillView(tenantId, name);
        ResponseEntity<Skill> postResponse = addSkill(tenantId, skillView);

        deleteSkill(tenantId, postResponse.getBody().getId());

        ResponseEntity<List<Skill>> response = getSkills(tenantId);

        assertThat(response.getBody()).isEmpty();
    }

    @Test
    public void createSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        SkillView skillView = new SkillView(tenantId, name);
        ResponseEntity<Skill> postResponse = addSkill(tenantId, skillView);

        ResponseEntity<Skill> response = getSkill(tenantId, postResponse.getBody().getId());

        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(postResponse.getBody()).isEqualTo(response.getBody());

        deleteSkill(tenantId, postResponse.getBody().getId());
    }

    @Test
    public void updateSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        SkillView skillView = new SkillView(tenantId, name);
        ResponseEntity<Skill> postResponse = addSkill(tenantId, skillView);

        SkillView skillView2 = new SkillView(tenantId, "name2");
        skillView2.setId(postResponse.getBody().getId());
        HttpEntity<SkillView> request = new HttpEntity<>(skillView2);

        ResponseEntity<Skill> putResponse = updateSkill(tenantId, request);

        ResponseEntity<Skill> response = getSkill(tenantId, putResponse.getBody().getId());

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isEqualTo(response.getBody());

        deleteSkill(tenantId, putResponse.getBody().getId());
    }
}
