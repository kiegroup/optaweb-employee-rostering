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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class RosterRestControllerTest {

    @Autowired
    private TestRestTemplate rosterRestTemplate;

    private String rosterPathURI = "http://localhost:8080/rest/tenant/{tenantId}/roster/";

    private ResponseEntity<RosterState> getRosterStateTest(Integer id) {
        return rosterRestTemplate.getForEntity(rosterPathURI + id, RosterState.class);
    }

    @Test
    public void getRosterStateTest() {
        // TODO: Implement getRosterTest when Tenant CRUD methods are implemented - the 'tenantId' column in RosterState
        // links to primary key (tenantId) in Tenant
    }

    // TODO: Add AvailabilityRosterView tests when Tenant CRUD methods are implemented
}
