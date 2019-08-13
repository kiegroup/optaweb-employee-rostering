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

package org.optaweb.employeerostering.roster;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RosterRestControllerTest extends AbstractEntityRequireTenantRestServiceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String rosterPathURI = "http://localhost:8080/rest/tenant/{tenantId}/roster/";

    private ResponseEntity<RosterState> getRosterState(Integer id) {
        return restTemplate.getForEntity(rosterPathURI + id, RosterState.class, TENANT_ID);
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
    public void getRosterStateTest() {
        ResponseEntity<RosterState> rosterStateResponseEntity = getRosterState(TENANT_ID);

        assertThat(rosterStateResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rosterStateResponseEntity.getBody().getPublishNotice()).isEqualTo(7);
        assertThat(rosterStateResponseEntity.getBody().getFirstDraftDate().toString()).isEqualTo("2000-01-01");
        assertThat(rosterStateResponseEntity.getBody().getPublishLength()).isEqualTo(7);
        assertThat(rosterStateResponseEntity.getBody().getDraftLength()).isEqualTo(24);
        assertThat(rosterStateResponseEntity.getBody().getUnplannedRotationOffset()).isEqualTo(0);
        assertThat(rosterStateResponseEntity.getBody().getRotationLength()).isEqualTo(7);
        assertThat(rosterStateResponseEntity.getBody().getLastHistoricDate().toString()).isEqualTo("1999-12-24");
        assertThat(rosterStateResponseEntity.getBody().getTimeZone().toString()).isEqualTo("Z");
        assertThat(rosterStateResponseEntity.getBody().getTenantId()).isEqualTo(TENANT_ID);
        assertThat(rosterStateResponseEntity.getBody().getTenant().getName()).isEqualTo("TestTenant");
        assertThat(rosterStateResponseEntity.getBody().getTenant().getId()).isEqualTo(TENANT_ID);
    }

    // TODO: Add AvailabilityRosterView tests when Tenant CRUD methods are implemented

    // TODO: Add ShiftRosterView tests when Tenant when Tenant CRUD methods are implemented
}
