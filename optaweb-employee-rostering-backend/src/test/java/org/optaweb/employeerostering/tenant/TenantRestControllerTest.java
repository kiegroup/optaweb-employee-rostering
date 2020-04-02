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
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.roster.view.RosterStateView;
import org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.domain.tenant.view.RosterConstraintConfigurationView;
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

    private ResponseEntity<RosterConstraintConfiguration> getRosterConstraintParametrization(Integer tenantId) {
        return restTemplate.getForEntity(tenantPathURI + tenantId + "/config/constraint",
                                         RosterConstraintConfiguration.class);
    }

    private ResponseEntity<RosterConstraintConfiguration> updateRosterConstraintParametrization(
            Integer tenantId, RosterConstraintConfigurationView rosterConstraintConfigurationView) {
        return restTemplate.postForEntity(tenantPathURI + tenantId + "/config/constraint/update",
                                          rosterConstraintConfigurationView, RosterConstraintConfiguration.class);
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
    public void rosterConstraintConfigurationCrudTest() {
        ResponseEntity<RosterConstraintConfiguration> getResponse = getRosterConstraintParametrization(TENANT_ID);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();

        ResponseEntity<RosterConstraintConfiguration> updateResponse =
                updateRosterConstraintParametrization(TENANT_ID, new RosterConstraintConfigurationView(
                        TENANT_ID, DayOfWeek.TUESDAY));
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody().getWeekStartDay()).isEqualTo(DayOfWeek.TUESDAY);
        assertThat(updateResponse.getBody().getRequiredSkill()).isEqualTo(HardMediumSoftLongScore.ofHard(100));
        assertThat(updateResponse.getBody().getUnavailableTimeSlot()).isEqualTo(HardMediumSoftLongScore.ofHard(50));
        assertThat(updateResponse.getBody().getNoMoreThan2ConsecutiveShifts())
                .isEqualTo(HardMediumSoftLongScore.ofHard(10));
        assertThat(updateResponse.getBody().getBreakBetweenNonConsecutiveShiftsAtLeast10Hours())
                .isEqualTo(HardMediumSoftLongScore.ofHard(1));
        assertThat(updateResponse.getBody().getContractMaximumDailyMinutes())
                .isEqualTo(HardMediumSoftLongScore.ofHard(1));
        assertThat(updateResponse.getBody().getContractMaximumWeeklyMinutes())
                .isEqualTo(HardMediumSoftLongScore.ofHard(1));
        assertThat(updateResponse.getBody().getContractMaximumMonthlyMinutes())
                .isEqualTo(HardMediumSoftLongScore.ofHard(1));
        assertThat(updateResponse.getBody().getContractMaximumYearlyMinutes())
                .isEqualTo(HardMediumSoftLongScore.ofHard(1));
        assertThat(updateResponse.getBody().getAssignEveryShift()).isEqualTo(HardMediumSoftLongScore.ofMedium(1));
        assertThat(updateResponse.getBody().getUndesiredTimeSlot()).isEqualTo(HardMediumSoftLongScore.ofSoft(1));
        assertThat(updateResponse.getBody().getDesiredTimeSlot()).isEqualTo(HardMediumSoftLongScore.ofSoft(1));
        assertThat(updateResponse.getBody().getNotRotationEmployee()).isEqualTo(HardMediumSoftLongScore.ofSoft(1));
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
