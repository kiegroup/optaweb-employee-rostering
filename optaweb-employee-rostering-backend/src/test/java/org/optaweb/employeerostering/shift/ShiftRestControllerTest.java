package org.optaweb.employeerostering.shift;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Collections;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@QuarkusTest
public class ShiftRestControllerTest extends AbstractEntityRequireTenantRestServiceTest {

    private final String shiftPathURI = "/rest/tenant/{tenantId}/shift/";
    private final String employeePathURI = "/rest/tenant/{tenantId}/employee/";
    private final String contractPathURI = "/rest/tenant/{tenantId}/contract/";
    private final String spotPathURI = "/rest/tenant/{tenantId}/spot/";

    private Response getShifts(Integer tenantId) {
        return RestAssured.get(shiftPathURI, tenantId);
    }

    private Response getShift(Integer tenantId, Long id) {
        return RestAssured.get(shiftPathURI + id, tenantId);
    }

    private void deleteShift(Integer tenantId, Long id) {
        RestAssured.delete(shiftPathURI + id, tenantId);
    }

    private Response addShift(Integer tenantId, ShiftView shiftView) {
        return RestAssured.given()
                .body(shiftView)
                .post(shiftPathURI + "add", tenantId);
    }

    private Response updateShift(Integer tenantId, ShiftView shiftView) {
        return RestAssured.given()
                .body(shiftView)
                .put(shiftPathURI + "update", tenantId);
    }

    private Response addEmployee(Integer tenantId, Employee employee) {
        return RestAssured.given()
                .body(employee)
                .post(employeePathURI + "add", tenantId);
    }

    private Response addContract(Integer tenantId, Contract contract) {
        return RestAssured.given()
                .body(contract)
                .post(contractPathURI + "add", tenantId);
    }

    private Response addSpot(Integer tenantId, SpotView spotView) {
        return RestAssured.given()
                .body(spotView)
                .post(spotPathURI + "add", tenantId);
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
    public void shiftCrudTest() {
        Response spotResponseEntity = addSpot(TENANT_ID, new SpotView(TENANT_ID, "spot",
                Collections.emptySet()));
        Spot spot = spotResponseEntity.as(Spot.class);

        Response contractResponseEntity = addContract(TENANT_ID, new Contract(TENANT_ID, "contract"));
        Contract contract = contractResponseEntity.as(Contract.class);

        Response rotationEmployeeResponseEntity = addEmployee(TENANT_ID,
                new Employee(TENANT_ID,
                        "rotationEmployee", contract,
                        Collections.emptySet()));
        Employee rotationEmployee = rotationEmployeeResponseEntity.as(Employee.class);

        LocalDateTime startDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(8);
        ShiftView shiftView = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime, rotationEmployee);
        Response postResponse = addShift(TENANT_ID, shiftView);
        assertThat(postResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());

        Response getResponse = getShift(TENANT_ID, postResponse.as(ShiftView.class).getId());
        assertThat(getResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(getResponse.getBody()).usingRecursiveComparison().ignoringFields("groovyResponse")
                .isEqualTo(postResponse.getBody());

        ShiftView updatedShiftView = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime);
        updatedShiftView.setId(postResponse.as(ShiftView.class).getId());
        Response putResponse = updateShift(TENANT_ID, updatedShiftView);
        assertThat(putResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());

        getResponse = getShift(TENANT_ID, putResponse.as(ShiftView.class).getId());
        assertThat(putResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(putResponse.getBody()).usingRecursiveComparison().ignoringFields("groovyResponse")
                .isEqualTo(getResponse.getBody());

        deleteShift(TENANT_ID, putResponse.as(ShiftView.class).getId());

        Response getShiftListResponse = getShifts(TENANT_ID);
        assertThat(getShiftListResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(getShiftListResponse.jsonPath().getList("$", ShiftView.class)).isEmpty();
    }
}
