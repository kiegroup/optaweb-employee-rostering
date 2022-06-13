package org.optaweb.employeerostering.employee;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.domain.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.domain.skill.Skill;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@QuarkusTest
public class EmployeeRestControllerTest extends AbstractEntityRequireTenantRestServiceTest {

    private final String employeePathURI = "/rest/tenant/{tenantId}/employee/";
    private final String contractPathURI = "/rest/tenant/{tenantId}/contract/";
    private final String skillPathURI = "/rest/tenant/{tenantId}/skill/";
    private final String employeeAvailabilityPathURI =
            "/rest/tenant/{tenantId}/employee/availability/";

    private Response getEmployees(Integer tenantId) {
        return RestAssured.get(employeePathURI, tenantId);
    }

    private Response getEmployee(Integer tenantId, Long id) {
        return RestAssured.get(employeePathURI + id, tenantId);
    }

    private void deleteEmployee(Integer tenantId, Long id) {
        RestAssured.delete(employeePathURI + id, tenantId);
    }

    private Response addEmployee(Integer tenantId, Employee employee) {
        return RestAssured.given()
                .body(employee)
                .post(employeePathURI + "add", tenantId);
    }

    private Response updateEmployee(Integer tenantId, Employee employee) {
        return RestAssured.given()
                .body(employee)
                .post(employeePathURI + "update", tenantId);
    }

    private Response addSkill(Integer tenantId, Skill skill) {
        return RestAssured.given()
                .body(skill)
                .post(skillPathURI + "add", tenantId);
    }

    private Response addContract(Integer tenantId, Contract contract) {
        return RestAssured.given()
                .body(contract)
                .post(contractPathURI + "add", tenantId);
    }

    private Response getEmployeeAvailability(Integer tenantId, Long id) {
        return RestAssured.get(employeeAvailabilityPathURI + id, tenantId);
    }

    private void deleteEmployeeAvailability(Integer tenantId, Long id) {
        RestAssured.delete(employeeAvailabilityPathURI + id, tenantId);
    }

    private Response addEmployeeAvailability(Integer tenantId,
            EmployeeAvailabilityView employeeAvailabilityView) {
        return RestAssured.given()
                .body(employeeAvailabilityView)
                .post(employeeAvailabilityPathURI + "add", tenantId);
    }

    private Response updateEmployeeAvailability(Integer tenantId,
            EmployeeAvailabilityView employeeAvailabilityView) {
        return RestAssured.given()
                .body(employeeAvailabilityView)
                .put(employeeAvailabilityPathURI + "update", tenantId);
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
    public void employeeCrudTest() {
        Response skillResponseA = addSkill(TENANT_ID, new Skill(TENANT_ID, "A"));
        Response skillResponseB = addSkill(TENANT_ID, new Skill(TENANT_ID, "B"));

        Skill skillA = skillResponseA.as(Skill.class);
        Skill skillB = skillResponseB.as(Skill.class);

        Set<Skill> testSkillSet = new HashSet<>();
        testSkillSet.add(skillA);
        testSkillSet.add(skillB);

        Response contractResponseEntity = addContract(TENANT_ID, new Contract(TENANT_ID, "A"));
        Contract contractA = contractResponseEntity.as(Contract.class);

        Employee employee = new Employee(TENANT_ID, "employee", contractA, testSkillSet);
        Response postResponse = addEmployee(TENANT_ID, employee);
        assertThat(postResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());

        Response response = getEmployee(TENANT_ID, postResponse.as(Employee.class).getId());
        assertThat(response.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(response.getBody()).usingRecursiveComparison().ignoringFields("groovyResponse")
                .isEqualTo(postResponse.getBody());

        Employee updatedEmployee = new Employee(TENANT_ID, "updatedEmployee", contractA,
                testSkillSet);
        updatedEmployee.setId(postResponse.as(Employee.class).getId());
        Response putResponse = updateEmployee(TENANT_ID, updatedEmployee);
        assertThat(putResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());

        response = getEmployee(TENANT_ID, putResponse.as(Employee.class).getId());
        assertThat(putResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(putResponse.as(Employee.class)).usingRecursiveComparison().ignoringFields("groovyResponse")
                .isEqualTo(response.as(Employee.class));

        deleteEmployee(TENANT_ID, putResponse.as(Employee.class).getId());

        Response getListResponse = getEmployees(TENANT_ID);
        assertThat(getListResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(getListResponse.jsonPath().getList("$", Employee.class)).isEmpty();
    }

    // ************************************************************************
    // EmployeeAvailability
    // ************************************************************************

    @Test
    public void employeeAvailabilityCrudTest() {
        Response contractResponseEntity = addContract(TENANT_ID, new Contract(TENANT_ID, "contract"));
        Contract contract = contractResponseEntity.as(Contract.class);

        Response employeeResponseEntity = addEmployee(TENANT_ID,
                new Employee(TENANT_ID, "employee",
                        contract,
                        Collections.emptySet()));
        Employee employee = employeeResponseEntity.as(Employee.class);

        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59);
        LocalDateTime endDateTime = LocalDateTime.of(2000, 1, 1, 0, 0);
        Response postResponse =
                addEmployeeAvailability(TENANT_ID, new EmployeeAvailabilityView(TENANT_ID, employee,
                        startDateTime,
                        endDateTime,
                        EmployeeAvailabilityState.UNAVAILABLE));
        assertThat(postResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());

        Response getResponse = getEmployeeAvailability(TENANT_ID,
                postResponse.as(EmployeeAvailabilityView.class).getId());
        assertThat(getResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(getResponse.getBody()).usingRecursiveComparison().ignoringFields("groovyResponse")
                .isEqualTo(postResponse.getBody());

        EmployeeAvailabilityView newEmployeeAvailabilityView =
                new EmployeeAvailabilityView(TENANT_ID, employee,
                        startDateTime, endDateTime,
                        EmployeeAvailabilityState.DESIRED);
        newEmployeeAvailabilityView.setId(postResponse.as(EmployeeAvailabilityView.class).getId());
        Response putResponse = updateEmployeeAvailability(TENANT_ID, newEmployeeAvailabilityView);
        assertThat(putResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());

        getResponse = getEmployeeAvailability(TENANT_ID, putResponse.as(EmployeeAvailabilityView.class).getId());
        assertThat(putResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(getResponse.getBody()).usingRecursiveComparison().ignoringFields("groovyResponse")
                .isEqualTo(putResponse.getBody());

        deleteEmployeeAvailability(TENANT_ID, putResponse.as(EmployeeAvailabilityView.class).getId());
    }
}
