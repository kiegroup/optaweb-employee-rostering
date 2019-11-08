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

package org.optaweb.employeerostering.tenant;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.roster.view.RosterStateView;
import org.optaweb.employeerostering.domain.tenant.RosterParametrization;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.domain.tenant.view.RosterParametrizationView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureTestDatabase
public class TenantRestControllerTest extends AbstractEntityRequireTenantRestServiceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String tenantPathURI = "http://localhost:8080/rest/tenant/";

    private ResponseEntity<Tenant> getTenant(Integer id) {
        return restTemplate.getForEntity(tenantPathURI + id, Tenant.class);
    }

    private ResponseEntity<Tenant> addTenant(RosterStateView initialRosterStateView) {
        return restTemplate.postForEntity(tenantPathURI + "add", initialRosterStateView, Tenant.class);
    }

    private void deleteTenant(Integer id) {
        restTemplate.postForEntity(tenantPathURI + "remove/" + id, null, Void.class);
    }

    private ResponseEntity<RosterParametrization> getRosterParametrization(Integer tenantId) {
        return restTemplate.getForEntity(tenantPathURI + tenantId + "/parametrization", RosterParametrization.class);
    }

    private ResponseEntity<RosterParametrization> updateRosterParametrization(RosterParametrizationView
                                                                                      rosterParametrizationView) {
        return restTemplate.postForEntity(tenantPathURI + "parametrization/update", rosterParametrizationView,
                                          RosterParametrization.class);
    }

    private ResponseEntity<List> getSupportedTimezones() {
        return restTemplate.getForEntity(tenantPathURI + "supported/timezones", List.class);
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
    public void tenantCrudTest() {
        RosterStateView rosterStateView = new RosterStateView(0, 0, LocalDate.of(2000, 01, 01), 0, 0, 0, 2,
                                                              LocalDate.of(2000, 01, 02), ZoneId.of("America/Toronto"));
        rosterStateView.setTenant(new Tenant("tenant"));
        ResponseEntity<Tenant> postResponse = addTenant(rosterStateView);
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<Tenant> getResponse = getTenant(postResponse.getBody().getId());
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isEqualToComparingFieldByFieldRecursively(postResponse.getBody());

        deleteTenant(postResponse.getBody().getId());
    }

    @Test
    public void rosterParametrizationCrudTest() {
        ResponseEntity<RosterParametrization> getResponse = getRosterParametrization(TENANT_ID);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();

        ResponseEntity<RosterParametrization> updateResponse =
                updateRosterParametrization(new RosterParametrizationView(TENANT_ID, 0, 0, 0, DayOfWeek.TUESDAY));
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody().getDesiredTimeSlotWeight()).isEqualTo(0);
        assertThat(updateResponse.getBody().getRotationEmployeeMatchWeight()).isEqualTo(0);
        assertThat(updateResponse.getBody().getUndesiredTimeSlotWeight()).isEqualTo(0);
        assertThat(updateResponse.getBody().getWeekStartDay()).isEqualTo(DayOfWeek.TUESDAY);
    }

    @Test
    public void getSupportedTimezonesTest() {
        ResponseEntity<List> getResponse = getSupportedTimezones();
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).contains("America/Toronto");
        assertThat(getResponse.getBody()).contains("Europe/Berlin");
        assertThat(getResponse.getBody()).contains("Zulu");
    }
}
