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
public class SkillRestControllerIT {

    @Autowired
    private TestRestTemplate skillRestTemplate;

    private String skillPathURI = "http://localhost:8080/rest/tenant/{tenantId}/skill/";

    @Test
    public void getSkillListTest() {
        Integer tenantId = 1;
        Integer tenantId2 = 2;
        String name = "name";
        String name2 = "name2";

        Skill skill = new Skill(tenantId, name);
        Skill skill2 = new Skill(tenantId, name2);
        Skill skill3 = new Skill(tenantId2, name);

        ResponseEntity<Skill> postResponse = skillRestTemplate.postForEntity(skillPathURI + "add", skill, Skill.class
                , tenantId);
        ResponseEntity<Skill> postResponse2 = skillRestTemplate.postForEntity(skillPathURI + "add", skill2, Skill.class
                , tenantId);
        ResponseEntity<Skill> postResponse3 = skillRestTemplate.postForEntity(skillPathURI + "add", skill3, Skill.class
                , tenantId2);

        ResponseEntity<List<Skill>> response = skillRestTemplate.exchange(skillPathURI, HttpMethod.GET, null,
                                                                          new ParameterizedTypeReference
                                                                                  <List<Skill>>() {}, tenantId);
        ResponseEntity<List<Skill>> response2 = skillRestTemplate.exchange(skillPathURI, HttpMethod.GET, null,
                                                                           new ParameterizedTypeReference
                                                                                   <List<Skill>>() {}, tenantId2);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(postResponse.getBody());
        assertThat(response.getBody()).contains(postResponse2.getBody());

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody()).contains(postResponse3.getBody());

        skillRestTemplate.delete(skillPathURI + postResponse.getBody().getId(), tenantId);
        skillRestTemplate.delete(skillPathURI + postResponse2.getBody().getId(), tenantId);
        skillRestTemplate.delete(skillPathURI + postResponse3.getBody().getId(), tenantId2);
    }

    @Test
    public void getSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        ResponseEntity<Skill> postResponse = skillRestTemplate.postForEntity(skillPathURI + "add", skill, Skill.class
                , tenantId);

        ResponseEntity<Skill> response = skillRestTemplate.getForEntity(skillPathURI + postResponse.getBody().getId(),
                                                                        Skill.class, tenantId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(postResponse.getBody());

        skillRestTemplate.delete(skillPathURI + postResponse.getBody().getId(), tenantId);
    }

    @Test
    public void deleteSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        ResponseEntity<Skill> postResponse = skillRestTemplate.postForEntity(skillPathURI + "add", skill, Skill.class
                , tenantId);

        skillRestTemplate.delete(skillPathURI + postResponse.getBody().getId(), tenantId);

        ResponseEntity<List<Skill>> response = skillRestTemplate.exchange(skillPathURI, HttpMethod.GET, null,
                                                                          new ParameterizedTypeReference
                                                                                  <List<Skill>>() {}, tenantId);

        assertThat(response.getBody()).isEmpty();
    }

    @Test
    public void createSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        ResponseEntity<Skill> postResponse = skillRestTemplate.postForEntity(skillPathURI + "add", skill, Skill.class
                , tenantId);

        ResponseEntity<Skill> response = skillRestTemplate.getForEntity(skillPathURI + postResponse.getBody().getId(),
                                                                        Skill.class, tenantId);

        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(postResponse.getBody()).isEqualTo(response.getBody());

        skillRestTemplate.delete(skillPathURI + postResponse.getBody().getId(), tenantId);
    }

    @Test
    public void updateSkillTest() {
        Integer tenantId = 1;
        String name = "name";

        Skill skill = new Skill(tenantId, name);

        ResponseEntity<Skill> postResponse = skillRestTemplate.postForEntity(skillPathURI + "add", skill, Skill.class
                , tenantId);

        Skill skill2 = new Skill(tenantId, "name2");
        skill2.setId(postResponse.getBody().getId());
        HttpEntity<Skill> request = new HttpEntity<>(skill2);

        ResponseEntity<Skill> putResponse = skillRestTemplate.exchange(skillPathURI + "update", HttpMethod.PUT,
                                                                       request, Skill.class, tenantId);

        ResponseEntity<Skill> response = skillRestTemplate.getForEntity(skillPathURI + putResponse.getBody().getId(),
                                                                        Skill.class, tenantId);

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(putResponse.getBody()).isEqualTo(response.getBody());

        skillRestTemplate.delete(skillPathURI + putResponse.getBody().getId(), tenantId);
    }
}
