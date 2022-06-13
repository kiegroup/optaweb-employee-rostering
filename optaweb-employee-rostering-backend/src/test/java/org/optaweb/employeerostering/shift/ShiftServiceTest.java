package org.optaweb.employeerostering.shift;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.time.LocalDateTime;
import java.util.Collections;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.view.ContractView;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.view.EmployeeView;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;
import org.optaweb.employeerostering.service.contract.ContractService;
import org.optaweb.employeerostering.service.employee.EmployeeService;
import org.optaweb.employeerostering.service.shift.ShiftService;
import org.optaweb.employeerostering.service.spot.SpotService;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusTest
public class ShiftServiceTest extends AbstractEntityRequireTenantRestServiceTest {

    @Inject
    ShiftService shiftService;

    @Inject
    SpotService spotService;

    @Inject
    ContractService contractService;

    @Inject
    EmployeeService employeeService;

    private Spot createSpot(Integer tenantId, String name) {
        SpotView spotView = new SpotView(tenantId, name, Collections.emptySet());
        return spotService.createSpot(tenantId, spotView);
    }

    private Contract createContract(Integer tenantId, String name) {
        ContractView contractView = new ContractView(tenantId, name);
        return contractService.createContract(tenantId, contractView);
    }

    private Employee createEmployee(Integer tenantId, String name, Contract contract) {
        EmployeeView employeeView = new EmployeeView(tenantId, name, contract, Collections.emptySet());
        return employeeService.createEmployee(tenantId, employeeView);
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
    public void getShiftListTest() {
        RestAssured.get("/rest/tenant/{tenantId}/shift/", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    public void getShiftTest() {
        Spot spot = createSpot(TENANT_ID, "spot");
        Contract contract = createContract(TENANT_ID, "contract");
        Employee rotationEmployee = createEmployee(TENANT_ID, "rotationEmployee", contract);

        LocalDateTime startDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(8);
        ShiftView shiftView = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime, rotationEmployee);
        ShiftView persistedShift = shiftService.createShift(TENANT_ID, shiftView);

        RestAssured.get("/rest/tenant/{tenantId}/shift/{id}", TENANT_ID, persistedShift.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("rotationEmployeeId", equalTo(persistedShift.getRotationEmployeeId().intValue()))
                .body("spotId", equalTo(persistedShift.getSpotId().intValue()))
                .body("startDateTime", equalTo("2000-01-01T00:00:00"))
                .body("endDateTime", equalTo("2000-01-01T08:00:00"));
    }

    @Test
    public void getNonExistentShiftTest() {
        String exceptionMessage = "No Shift entity found with ID (0).";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        RestAssured.get("/rest/tenant/{tenantId}/shift/{id}", TENANT_ID, 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void deleteShiftTest() {
        Spot spot = createSpot(TENANT_ID, "spot");
        Contract contract = createContract(TENANT_ID, "contract");
        Employee rotationEmployee = createEmployee(TENANT_ID, "rotationEmployee", contract);

        LocalDateTime startDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(8);
        ShiftView shiftView = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime, rotationEmployee);
        ShiftView persistedShift = shiftService.createShift(TENANT_ID, shiftView);

        boolean result = RestAssured.delete("/rest/tenant/{tenantId}/shift/{id}", TENANT_ID, persistedShift.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(Boolean.class);
        assertThat(result).isTrue();
    }

    @Test
    public void deleteNonExistentShiftTest() {
        boolean result = RestAssured.delete("/rest/tenant/{tenantId}/shift/{id}", TENANT_ID, 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(Boolean.class);
        assertThat(result).isFalse();
    }

    @Test
    public void createShiftTest() {
        Spot spot = createSpot(TENANT_ID, "spot");
        Contract contract = createContract(TENANT_ID, "contract");
        Employee rotationEmployee = createEmployee(TENANT_ID, "rotationEmployee", contract);

        LocalDateTime startDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(8);
        ShiftView shiftView = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime, rotationEmployee);

        RestAssured.given()
                .body(shiftView)
                .post("/rest/tenant/{tenantId}/shift/add", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("rotationEmployeeId", equalTo(shiftView.getRotationEmployeeId().intValue()))
                .body("spotId", equalTo(shiftView.getSpotId().intValue()))
                .body("startDateTime", equalTo("2000-01-01T00:00:00"))
                .body("endDateTime", equalTo("2000-01-01T08:00:00"));
    }

    @Test
    public void createInvalidShiftTest() {
        Spot spot = createSpot(TENANT_ID, "spot");
        Contract contract = createContract(TENANT_ID, "contract");
        Employee rotationEmployee = createEmployee(TENANT_ID, "rotationEmployee", contract);

        LocalDateTime startDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime;
        ShiftView shiftView = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime, rotationEmployee);

        String exceptionMessage = "Shift's end date time is not at least 30 minutes" +
                " after shift's start date time";
        String i18nKey = "ServerSideException.entityConstraintViolation";

        RestAssured.given()
                .body(shiftView)
                .post("/rest/tenant/{tenantId}/shift/add", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .body("messageParameters[1]", equalTo(exceptionMessage))
                .body("i18nKey", equalTo(i18nKey));
    }

    @Test
    public void updateShiftTest() {
        Spot spot = createSpot(TENANT_ID, "spot");
        Contract contract = createContract(TENANT_ID, "contract");
        Employee rotationEmployee = createEmployee(TENANT_ID, "rotationEmployee", contract);

        LocalDateTime startDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(8);
        ShiftView shiftView = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime, rotationEmployee);
        ShiftView persistedShift = shiftService.createShift(TENANT_ID, shiftView);

        startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59, 59, 0);
        endDateTime = startDateTime.plusHours(10);
        ShiftView updatedShift = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime, rotationEmployee);
        updatedShift.setId(persistedShift.getId());

        RestAssured.given()
                .body(updatedShift)
                .put("/rest/tenant/{tenantId}/shift/update", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("rotationEmployeeId", equalTo(updatedShift.getRotationEmployeeId().intValue()))
                .body("spotId", equalTo(updatedShift.getSpotId().intValue()))
                .body("startDateTime", equalTo("1999-12-31T23:59:59"))
                .body("endDateTime", equalTo("2000-01-01T09:59:59"));
    }

    @Test
    public void updateNonExistentShiftTest() {
        String exceptionMessage = "Shift entity with ID (0) not found.";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        Spot spot = createSpot(TENANT_ID, "spot");
        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59, 59, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(10);
        ShiftView updatedShift = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime);
        updatedShift.setId(0L);

        RestAssured.given()
                .body(updatedShift)
                .put("/rest/tenant/{tenantId}/shift/update", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }
}
