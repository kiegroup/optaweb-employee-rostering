package org.optaweb.employeerostering.employee;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.view.ContractView;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.domain.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.domain.employee.view.EmployeeView;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.skill.view.SkillView;
import org.optaweb.employeerostering.service.contract.ContractService;
import org.optaweb.employeerostering.service.employee.EmployeeService;
import org.optaweb.employeerostering.service.skill.SkillService;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusTest
public class EmployeeServiceTest extends AbstractEntityRequireTenantRestServiceTest {

    @Inject
    EmployeeService employeeService;

    @Inject
    SkillService skillService;

    @Inject
    ContractService contractService;

    private Skill createSkill(Integer tenantId, String name) {
        SkillView skillView = new SkillView(tenantId, name);
        return skillService.createSkill(tenantId, skillView);
    }

    private Contract createContract(Integer tenantId, String name) {
        ContractView contractView = new ContractView(tenantId, name);
        return contractService.createContract(tenantId, contractView);
    }

    @BeforeEach
    public void setup() {
        createTestTenant();
    }

    @AfterEach
    public void cleanup() {
        deleteTestTenant();
    }

    // ************************************************************************
    // Employee
    // ************************************************************************

    @Test
    public void getEmployeeListTest() {
        RestAssured.get("/rest/tenant/{tenantId}/employee/", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    public void getEmployeeTest() {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        List<Skill> skillList = RestAssured.get("/rest/tenant/{tenantId}/employee/" + employee.getId(), TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("name", equalTo("employee"))
                .body("contract.id", equalTo(contract.getId().intValue()))
                .extract().jsonPath().getList("skillProficiencySet", Skill.class);
        assertThat(skillList).containsExactlyInAnyOrderElementsOf(testSkillSet);
    }

    @Test
    public void getNonExistentEmployeeTest() {
        String exceptionMessage = "No Employee entity found with ID (0).";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        RestAssured.get("/rest/tenant/{tenantId}/employee/{id}", TENANT_ID, 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void getNonMatchingEmployeeTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable (employee)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        RestAssured.get("/rest/tenant/{tenantId}/employee/{id}", 0, employee.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void deleteEmployeeTest() {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        boolean result = RestAssured.delete("/rest/tenant/{tenantId}/employee/{id}", TENANT_ID, employee.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(Boolean.class);
        assertThat(result).isTrue();
    }

    @Test
    public void deleteNonExistentEmployeeTest() {
        boolean result = RestAssured.delete("/rest/tenant/{tenantId}/employee/{id}", TENANT_ID, 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(Boolean.class);
        assertThat(result).isFalse();
    }

    @Test
    public void deleteNonMatchingEmployeeTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable (employee)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        RestAssured.delete("/rest/tenant/{tenantId}/employee/{id}", 0, employee.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void createEmployeeTest() {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet);

        List<Skill> skillList = RestAssured.given()
                .body(employeeView)
                .post("/rest/tenant/{tenantId}/employee/add", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("name", equalTo("employee"))
                .body("contract.id", equalTo(contract.getId().intValue()))
                .extract().jsonPath().getList("skillProficiencySet", Skill.class);
        assertThat(skillList).containsExactlyInAnyOrderElementsOf(testSkillSet);
    }

    @Test
    public void createNonMatchingEmployeeTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable (employee)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(0, "A");
        Skill skillB = createSkill(0, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet);

        RestAssured.given()
                .body(employeeView)
                .post("/rest/tenant/{tenantId}/employee/add", 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void createNonMatchingSkillProficiencyTest() {
        String exceptionMessage = "The tenantId (" + TENANT_ID + ") does not match the skillProficiency (A)'s " +
                "tenantId (0).";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(0, "A");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet);

        RestAssured.given()
                .body(employeeView)
                .post("/rest/tenant/{tenantId}/employee/add", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void updateEmployeeTest() {
        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        EmployeeView updatedEmployee = new EmployeeView(TENANT_ID, "updatedEmployee", contract, testSkillSet);
        updatedEmployee.setId(employee.getId());

        List<Skill> skillList = RestAssured.given()
                .body(updatedEmployee)
                .post("/rest/tenant/{tenantId}/employee/update", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("name", equalTo("updatedEmployee"))
                .body("contract.id", equalTo(contract.getId().intValue()))
                .extract().jsonPath().getList("skillProficiencySet", Skill.class);
        assertThat(skillList).containsExactlyInAnyOrderElementsOf(testSkillSet);
    }

    @Test
    public void updateNonMatchingEmployeeTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable (updatedEmployee)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, testSkillSet);
        employeeService.createEmployee(TENANT_ID, employeeView);

        EmployeeView updatedEmployee = new EmployeeView(TENANT_ID, "updatedEmployee", contract, testSkillSet);

        RestAssured.given()
                .body(updatedEmployee)
                .post("/rest/tenant/{tenantId}/employee/update", 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void updateNonExistentEmployeeTest() {
        String exceptionMessage = "Employee entity with ID (0) not found.";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        Contract contract = new Contract();

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        employeeView.setId(0L);

        RestAssured.given()
                .body(employeeView)
                .post("/rest/tenant/{tenantId}/employee/update", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void updateChangeTenantIdEmployeeTest() {
        String exceptionMessage = "Employee entity with tenantId (" + TENANT_ID + ") cannot change tenants.";
        String exceptionClass = "java.lang.IllegalStateException";

        Skill skillA = createSkill(TENANT_ID, "A");
        Skill skillB = createSkill(TENANT_ID, "B");

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Contract contractA = createContract(TENANT_ID, "A");
        Contract contractB = createContract(0, "B");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contractA, testSkillSet);
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        EmployeeView updatedEmployee = new EmployeeView(0, "updatedEmployee", contractB, Collections.emptySet());
        updatedEmployee.setId(employee.getId());

        RestAssured.given()
                .body(updatedEmployee)
                .post("/rest/tenant/{tenantId}/employee/update", 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    // ************************************************************************
    // EmployeeAvailability
    // ************************************************************************

    @Test
    public void getEmployeeAvailabilityTest() {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime,
                EmployeeAvailabilityState.UNAVAILABLE);
        EmployeeAvailabilityView persistedEmployeeAvailabilityView =
                employeeService.createEmployeeAvailability(TENANT_ID, employeeAvailabilityView);

        RestAssured.get("/rest/tenant/{tenantId}/employee/availability/{id}", TENANT_ID,
                persistedEmployeeAvailabilityView.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("employeeId", equalTo(employee.getId().intValue()))
                .body("startDateTime", equalTo("1999-12-31T23:59:00"))
                .body("endDateTime", equalTo("2000-01-01T00:00:00"))
                .body("state", equalTo("UNAVAILABLE"));
    }

    @Test
    public void getNonExistentEmployeeAvailabilityTest() {
        String exceptionMessage = "No EmployeeAvailability entity found with ID (0).";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        RestAssured.get("/rest/tenant/{tenantId}/employee/availability/{id}", TENANT_ID, 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void getNonMatchingEmployeeAvailabilityTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable " +
                "(employee:1999-12-31T23:59Z-2000-01-01T00:00Z)'s tenantId (" + TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime,
                EmployeeAvailabilityState.UNAVAILABLE);
        EmployeeAvailabilityView persistedEmployeeAvailabilityView =
                employeeService.createEmployeeAvailability(TENANT_ID, employeeAvailabilityView);

        RestAssured.get("/rest/tenant/{tenantId}/employee/availability/{id}", 0, persistedEmployeeAvailabilityView.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void deleteEmployeeAvailabilityTest() {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime,
                EmployeeAvailabilityState.UNAVAILABLE);
        EmployeeAvailabilityView persistedEmployeeAvailabilityView =
                employeeService.createEmployeeAvailability(TENANT_ID, employeeAvailabilityView);

        boolean result = RestAssured.delete("/rest/tenant/{tenantId}/employee/availability/{id}", TENANT_ID,
                persistedEmployeeAvailabilityView.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(Boolean.class);
        assertThat(result).isTrue();
    }

    @Test
    public void deleteNonExistentEmployeeAvailabilityTest() {
        boolean result = RestAssured.delete("/rest/tenant/{tenantId}/employee/availability/{id}", TENANT_ID,
                0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(Boolean.class);
        assertThat(result).isFalse();
    }

    @Test
    public void deleteNonMatchingEmployeeAvailabilityTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable " +
                "(employee:1999-12-31T23:59Z-2000-01-01T00:00Z)'s tenantId (" + TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime,
                EmployeeAvailabilityState.UNAVAILABLE);
        EmployeeAvailabilityView persistedEmployeeAvailabilityView =
                employeeService.createEmployeeAvailability(TENANT_ID, employeeAvailabilityView);

        RestAssured.delete("/rest/tenant/{tenantId}/employee/availability/{id}", 0,
                persistedEmployeeAvailabilityView.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void createEmployeeAvailabilityTest() {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime,
                EmployeeAvailabilityState.UNAVAILABLE);

        RestAssured.given()
                .body(employeeAvailabilityView)
                .post("/rest/tenant/{tenantId}/employee/availability/add", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("employeeId", equalTo(employee.getId().intValue()))
                .body("startDateTime", equalTo("1999-12-31T23:59:00"))
                .body("endDateTime", equalTo("2000-01-01T00:00:00"))
                .body("state", equalTo("UNAVAILABLE"));
    }

    @Test
    public void createNonMatchingEmployeeAvailabilityTest() {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime,
                EmployeeAvailabilityState.UNAVAILABLE);
        String employeeAvailabilityName =
                employeeAvailabilityView.getEmployeeId() + ":" + startDateTime + "-" + endDateTime;

        String exceptionMessage = "The tenantId (0) does not match the persistable (" + employeeAvailabilityName +
                ")'s tenantId (" + TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        RestAssured.given()
                .body(employeeAvailabilityView)
                .post("/rest/tenant/{tenantId}/employee/availability/add", 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void updateEmployeeAvailabilityTest() {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime,
                EmployeeAvailabilityState.UNAVAILABLE);
        EmployeeAvailabilityView persistedEmployeeAvailabilityView =
                employeeService.createEmployeeAvailability(TENANT_ID, employeeAvailabilityView);

        EmployeeAvailabilityView updatedEmployeeAvailability = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime,
                EmployeeAvailabilityState.DESIRED);
        updatedEmployeeAvailability.setId(persistedEmployeeAvailabilityView.getId());

        RestAssured.given()
                .body(updatedEmployeeAvailability)
                .put("/rest/tenant/{tenantId}/employee/availability/update", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("employeeId", equalTo(employee.getId().intValue()))
                .body("startDateTime", equalTo("1999-12-31T23:59:00"))
                .body("endDateTime", equalTo("2000-01-01T00:00:00"))
                .body("state", equalTo("DESIRED"));
    }

    @Test
    public void updateNonMatchingEmployeeAvailabilityTest() {
        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime,
                EmployeeAvailabilityState.UNAVAILABLE);
        EmployeeAvailabilityView persistedEmployeeAvailabilityView =
                employeeService.createEmployeeAvailability(TENANT_ID, employeeAvailabilityView);

        EmployeeAvailabilityView updatedEmployeeAvailability = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime,
                EmployeeAvailabilityState.DESIRED);
        updatedEmployeeAvailability.setId(persistedEmployeeAvailabilityView.getId());

        String employeeAvailabilityName =
                updatedEmployeeAvailability.getEmployeeId() + ":" + startDateTime + "-" + endDateTime;

        String exceptionMessage = "The tenantId (0) does not match the persistable (" + employeeAvailabilityName +
                ")'s tenantId (" + TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        RestAssured.given()
                .body(updatedEmployeeAvailability)
                .put("/rest/tenant/{tenantId}/employee/availability/update", 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void updateNonExistentEmployeeAvailabilityTest() {
        String exceptionMessage = "EmployeeAvailability entity with ID (0) not found.";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        Contract contract = createContract(TENANT_ID, "contract");

        EmployeeView employeeView = new EmployeeView(TENANT_ID, "employee", contract, Collections.emptySet());
        Employee employee = employeeService.createEmployee(TENANT_ID, employeeView);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                startDateTime, endDateTime,
                EmployeeAvailabilityState.DESIRED);
        employeeAvailabilityView.setId(0L);

        RestAssured.given()
                .body(employeeAvailabilityView)
                .put("/rest/tenant/{tenantId}/employee/availability/update", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    private Set<Skill> getSkillSet(String... names) {
        AtomicLong id = new AtomicLong(0L);
        return Arrays.stream(names)
                .map(name -> {
                    Skill out = new Skill(TENANT_ID, name);
                    out.setId(id.incrementAndGet());
                    return out;
                })
                .collect(Collectors.toSet());
    }

    @Test
    public void employeeListImportTest() throws IOException {
        Contract defaultContract = contractService.getOrCreateDefaultContract(TENANT_ID);

        List<Employee> expectedEmployeeList = Arrays.asList(
                new Employee(TENANT_ID, "Amy Cole", defaultContract, getSkillSet("Nurse")),
                new Employee(TENANT_ID, "Beth Fox", defaultContract,
                        getSkillSet("Respiratory Specialist", "Nurse")),
                new Employee(TENANT_ID, "Chad Green", defaultContract, getSkillSet("Nurse")),
                new Employee(TENANT_ID, "Dan Jones", defaultContract, getSkillSet("Nurse")),
                new Employee(TENANT_ID, "Elsa King", defaultContract, getSkillSet("Nurse")),
                new Employee(TENANT_ID, "Flo Li", defaultContract,
                        getSkillSet("Nurse", "Emergency Person")),
                new Employee(TENANT_ID, "Gus Poe", defaultContract, getSkillSet("Nurse")),
                new Employee(TENANT_ID, "Hugo Rye", defaultContract, Collections.emptySet()),
                new Employee(TENANT_ID, "Ivy Smith", defaultContract, getSkillSet("Nurse")),
                new Employee(TENANT_ID, "Amy Fox", defaultContract, getSkillSet("Foo Faa fu")),
                new Employee(TENANT_ID, "Beth Green", defaultContract,
                        getSkillSet("Foo Faa fu")),
                new Employee(TENANT_ID, "Chad Jones", defaultContract, getSkillSet("Nurse")),
                new Employee(TENANT_ID, "Dan King", defaultContract, Collections.emptySet()),
                new Employee(TENANT_ID, "Elsa Li", defaultContract, getSkillSet("Nurse")),
                new Employee(TENANT_ID, "Flo Poe", defaultContract, getSkillSet("Doctor")),
                new Employee(TENANT_ID, "Gus Rye", defaultContract, getSkillSet("Nurse")),
                new Employee(TENANT_ID, "Hugo Smith", defaultContract, getSkillSet("Nurse")),
                new Employee(TENANT_ID, "Ivy Watt", defaultContract, getSkillSet("Nur se")),
                new Employee(TENANT_ID, "Jay Cole", defaultContract, getSkillSet("Nurse")),
                new Employee(TENANT_ID, "Amy Green", defaultContract, getSkillSet("Nurse")),
                new Employee(TENANT_ID, "Beth Jones", defaultContract,
                        getSkillSet("Respiratory Specialist")),
                new Employee(TENANT_ID, "Chad King", defaultContract, getSkillSet("Nurse")),
                new Employee(TENANT_ID, "Dan Li", defaultContract, getSkillSet("Doctor")),
                new Employee(TENANT_ID, "My Name", defaultContract, getSkillSet("Nur se")));

        Function<List<Employee>, Consumer<Employee>> assertListMatch = employeeList -> expected -> {
            Optional<Employee> maybeActual = employeeList.stream()
                    .filter(e -> e.getName().equals(expected.getName())).findAny();
            if (maybeActual.isPresent()) {
                Employee actual = maybeActual.get();
                assertThat(actual.getContract()).withFailMessage("Wrong contract for " + expected.getName())
                        .isEqualTo(expected.getContract());
                assertThat(actual.getTenantId()).withFailMessage("Wrong tenant id for " + expected.getName())
                        .isEqualTo(expected.getTenantId());
                assertThat(actual.getSkillProficiencySet()).withFailMessage("Wrong number of skills for " + expected.getName())
                        .hasSize(expected.getSkillProficiencySet().size());

                assertThat(actual.getSkillProficiencySet()).allMatch(skill -> expected.getSkillProficiencySet().stream()
                        .anyMatch(expectedSkill -> skill.getName().equals(expectedSkill.getName())));
            } else {
                fail("Expected an employee with name (" + expected.getName() + "), but no such employee was found.");
            }
        };

        final List<Employee> excelEmployeeList = employeeService
                .importEmployeesFromExcel(TENANT_ID, getClass().getResourceAsStream("/EmployeeList.xlsx"));
        expectedEmployeeList.forEach(assertListMatch.apply(excelEmployeeList));
        final List<Employee> allEmployeeList = employeeService.getEmployeeList(TENANT_ID);
        expectedEmployeeList.forEach(assertListMatch.apply(allEmployeeList));
    }
}
