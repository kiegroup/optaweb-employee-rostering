package org.optaweb.employeerostering.generator;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.rotation.view.TimeBucketView;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.service.roster.RosterGenerator;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@QuarkusTest
public class RosterGeneratorTest extends AbstractEntityRequireTenantRestServiceTest {

    @Inject
    RosterGenerator rosterGenerator;

    private Integer tenantId;

    private final String skillPathURI = "/rest/tenant/{tenantId}/skill/";
    private final String spotPathURI = "/rest/tenant/{tenantId}/spot/";
    private final String contractPathURI = "/rest/tenant/{tenantId}/contract/";
    private final String employeePathURI = "/rest/tenant/{tenantId}/employee/";
    private final String rotationPathURI = "/rest/tenant/{tenantId}/rotation/";
    private final String tenantPathURI = "/rest/tenant/";
    private final String shiftPathURI = "/rest/tenant/{tenantId}/shift/";

    private Response getSkills(Integer tenantId) {
        return RestAssured.get(skillPathURI, tenantId);
    }

    private Response getSpots(Integer tenantId) {
        return RestAssured.get(spotPathURI, tenantId);
    }

    private Response getContracts(Integer tenantId) {
        return RestAssured.get(contractPathURI, tenantId);
    }

    private Response getEmployees(Integer tenantId) {
        return RestAssured.get(employeePathURI, tenantId);
    }

    private Response getTimeBuckets(Integer tenantId) {
        return RestAssured.get(rotationPathURI, tenantId);
    }

    private Response getShifts(Integer tenantId) {
        return RestAssured.get(shiftPathURI, tenantId);
    }

    private Response getTenants() {
        return RestAssured.get(tenantPathURI);
    }

    @BeforeEach
    public void setup() {
        tenantId = rosterGenerator.generateRoster(2, 7).getTenantId();
    }

    @AfterEach
    public void cleanup() {
        deleteTestTenant();
    }

    @Test
    public void generateSkillListTest() {
        Response response = getSkills(tenantId);

        assertThat(response.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(response.getBody().jsonPath().getList("$", Skill.class)).size().isGreaterThan(0);
    }

    @Test
    public void generateSpotListTest() {
        Response response = getSpots(tenantId);

        assertThat(response.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(response.getBody().jsonPath().getList("$", Spot.class)).size().isGreaterThan(0);
    }

    @Test
    public void generateContractListTest() {
        Response response = getContracts(tenantId);

        assertThat(response.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(response.getBody().jsonPath().getList("$", Contract.class)).size().isGreaterThan(0);
    }

    @Test
    public void generateEmployeeListTest() {
        Response response = getEmployees(tenantId);

        assertThat(response.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(response.getBody().jsonPath().getList("$", Employee.class)).size().isGreaterThan(0);
    }

    @Test
    public void generateShiftTemplateListTest() {
        Response response = getTimeBuckets(tenantId);

        assertThat(response.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(response.getBody().jsonPath().getList("$", TimeBucketView.class)).size().isGreaterThan(0);
    }

    @Test
    public void generateShiftListTest() {
        Response response = getShifts(tenantId);

        assertThat(response.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(response.getBody().jsonPath().getList("$")).size().isGreaterThan(0);
    }

    @Test
    public void generateTenantListTest() {
        Response response = getTenants();

        assertThat(response.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(response.getBody().jsonPath().getList("$", Tenant.class)).size().isGreaterThan(0);
    }
}
