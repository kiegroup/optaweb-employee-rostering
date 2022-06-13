package org.optaweb.employeerostering.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.roster.view.RosterStateView;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.domain.tenant.view.RosterConstraintConfigurationView;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@QuarkusTest
public class TenantRestControllerTest extends AbstractEntityRequireTenantRestServiceTest {

    private final String tenantPathURI = "/rest/tenant/";

    private Response getTenant(Integer id) {
        return RestAssured.get(tenantPathURI + id);
    }

    private Response addTenant(RosterStateView initialRosterStateView) {
        return RestAssured.given()
                .body(initialRosterStateView)
                .post(tenantPathURI + "add");
    }

    private void deleteTenant(Integer id) {
        RestAssured.delete(tenantPathURI + "remove/" + id);
    }

    private Response getRosterConstraintParametrization(Integer tenantId) {
        return RestAssured.get(tenantPathURI + tenantId + "/config/constraint");
    }

    private Response updateRosterConstraintParametrization(
            Integer tenantId, RosterConstraintConfigurationView rosterConstraintConfigurationView) {
        return RestAssured.given()
                .body(rosterConstraintConfigurationView)
                .post(tenantPathURI + tenantId + "/config/constraint/update");
    }

    private Response getSupportedTimezones() {
        return RestAssured.get(tenantPathURI + "supported/timezones");
    }

    @BeforeEach
    public void setup() {
        createTestTenant();
    }

    @AfterEach
    public void cleanup() {
        deleteTestTenant();
    }

    @Test
    public void tenantCrudTest() {
        RosterStateView rosterStateView = new RosterStateView(0, 0, LocalDate.of(2000, 1, 1), 0, 0, 0, 2,
                LocalDate.of(2000, 1, 2), ZoneId.of("America/Toronto"));
        rosterStateView.setTenant(new Tenant("tenant"));
        Response postResponse = addTenant(rosterStateView);
        assertThat(postResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());

        Response getResponse = getTenant(postResponse.as(Tenant.class).getId());
        assertThat(getResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(getResponse.as(Tenant.class)).usingRecursiveComparison().ignoringFields("groovyResponse")
                .isEqualTo(postResponse.as(Tenant.class));

        deleteTenant(postResponse.as(Tenant.class).getId());
    }

    @Test
    public void rosterConstraintConfigurationCrudTest() {
        Response getResponse = getRosterConstraintParametrization(TENANT_ID);
        assertThat(getResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(getResponse.getBody()).isNotNull();

        Response updateResponse =
                updateRosterConstraintParametrization(TENANT_ID, new RosterConstraintConfigurationView(
                        TENANT_ID, DayOfWeek.TUESDAY));
        assertThat(updateResponse.statusCode()).isEqualTo(Status.OK.getStatusCode());
        RosterConstraintConfigurationView updateBody = updateResponse.as(RosterConstraintConfigurationView.class);
        assertThat(updateBody.getWeekStartDay()).isEqualTo(DayOfWeek.TUESDAY);
        assertThat(updateBody.getRequiredSkill()).isEqualTo(HardMediumSoftLongScore.ofHard(100));
        assertThat(updateBody.getUnavailableTimeSlot()).isEqualTo(HardMediumSoftLongScore.ofHard(50));
        assertThat(updateBody.getNoMoreThan2ConsecutiveShifts())
                .isEqualTo(HardMediumSoftLongScore.ofHard(10));
        assertThat(updateBody.getBreakBetweenNonConsecutiveShiftsAtLeast10Hours())
                .isEqualTo(HardMediumSoftLongScore.ofHard(1));
        assertThat(updateBody.getContractMaximumDailyMinutes())
                .isEqualTo(HardMediumSoftLongScore.ofHard(1));
        assertThat(updateBody.getContractMaximumWeeklyMinutes())
                .isEqualTo(HardMediumSoftLongScore.ofHard(1));
        assertThat(updateBody.getContractMaximumMonthlyMinutes())
                .isEqualTo(HardMediumSoftLongScore.ofHard(1));
        assertThat(updateBody.getContractMaximumYearlyMinutes())
                .isEqualTo(HardMediumSoftLongScore.ofHard(1));
        assertThat(updateBody.getAssignEveryShift()).isEqualTo(HardMediumSoftLongScore.ofMedium(1));
        assertThat(updateBody.getUndesiredTimeSlot()).isEqualTo(HardMediumSoftLongScore.ofSoft(1));
        assertThat(updateBody.getDesiredTimeSlot()).isEqualTo(HardMediumSoftLongScore.ofSoft(1));
        assertThat(updateBody.getNotRotationEmployee()).isEqualTo(HardMediumSoftLongScore.ofSoft(1));
    }

    @Test
    public void getSupportedTimezonesTest() {
        Response getResponse = getSupportedTimezones();
        assertThat(getResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        List<String> timezoneList = getResponse.jsonPath().getList("$", String.class);
        assertThat(timezoneList).contains("America/Toronto");
        assertThat(timezoneList).contains("Europe/Berlin");
        assertThat(timezoneList).contains("Zulu");
    }
}
