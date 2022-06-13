package org.optaweb.employeerostering.contract;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.equalTo;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.view.ContractView;
import org.optaweb.employeerostering.service.contract.ContractService;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@QuarkusTest
public class ContractServiceTest extends AbstractEntityRequireTenantRestServiceTest {

    @Inject
    ContractService contractService;

    @BeforeEach
    public void setup() {
        createTestTenant();
    }

    @AfterEach
    public void cleanup() {
        deleteTestTenant();
    }

    @Test
    public void getContractListTest() {
        RestAssured.get("/rest/tenant/{tenantId}/contract/", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    public void getContractTest() {
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        ContractView contractView = new ContractView(TENANT_ID, "contract", maximumMinutesPerDay, maximumMinutesPerWeek,
                maximumMinutesPerMonth, maximumMinutesPerYear);
        Contract contract = contractService.createContract(TENANT_ID, contractView);

        RestAssured.get("/rest/tenant/{tenantId}/contract/{id}", TENANT_ID, contract.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("name", equalTo("contract"))
                .body("maximumMinutesPerDay", equalTo(maximumMinutesPerDay))
                .body("maximumMinutesPerWeek", equalTo(maximumMinutesPerWeek))
                .body("maximumMinutesPerMonth", equalTo(maximumMinutesPerMonth))
                .body("maximumMinutesPerYear", equalTo(maximumMinutesPerYear));
    }

    @Test
    public void getNonExistentContractTest() {
        String exceptionMessage = "No Contract entity found with ID (0).";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        RestAssured.get("/rest/tenant/{tenantId}/contract/{id}", TENANT_ID, 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void getNonMatchingContractTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable (contract)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        ContractView contractView = new ContractView(TENANT_ID, "contract");
        Contract contract = contractService.createContract(TENANT_ID, contractView);

        RestAssured.get("/rest/tenant/{tenantId}/contract/{id}", 0, contract.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void deleteContractTest() {
        ContractView contractView = new ContractView(TENANT_ID, "contract");
        Contract contract = contractService.createContract(TENANT_ID, contractView);

        boolean result = RestAssured.delete("/rest/tenant/{tenantId}/contract/{id}", TENANT_ID, contract.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(Boolean.class);
        assertThat(result).isTrue();
    }

    @Test
    public void deleteNonExistentContractTest() {
        boolean result = RestAssured.delete("/rest/tenant/{tenantId}/contract/{id}", TENANT_ID, 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().as(Boolean.class);
        assertThat(result).isFalse();
    }

    @Test
    public void deleteNonMatchingContractTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable (contract)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        ContractView contractView = new ContractView(TENANT_ID, "contract");
        Contract contract = contractService.createContract(TENANT_ID, contractView);

        RestAssured.delete("/rest/tenant/{tenantId}/contract/{id}", 0, contract.getId())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void createContractTest() {
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        ContractView contractView = new ContractView(TENANT_ID, "contract", maximumMinutesPerDay, maximumMinutesPerWeek,
                maximumMinutesPerMonth, maximumMinutesPerYear);

        RestAssured.given()
                .body(contractView)
                .post("/rest/tenant/{tenantId}/contract/add", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("name", equalTo("contract"))
                .body("maximumMinutesPerDay", equalTo(maximumMinutesPerDay))
                .body("maximumMinutesPerWeek", equalTo(maximumMinutesPerWeek))
                .body("maximumMinutesPerMonth", equalTo(maximumMinutesPerMonth))
                .body("maximumMinutesPerYear", equalTo(maximumMinutesPerYear));
    }

    @Test
    public void createNonMatchingContractTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable (contract)'s tenantId ("
                + TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        ContractView contractView = new ContractView(TENANT_ID, "contract");

        RestAssured.given()
                .body(contractView)
                .post("/rest/tenant/{tenantId}/contract/add", 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void updateContractTest() {
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        ContractView contractView = new ContractView(TENANT_ID, "contract");
        Contract contract = contractService.createContract(TENANT_ID, contractView);

        ContractView updatedContractView = new ContractView(TENANT_ID, "updatedContract", maximumMinutesPerDay,
                maximumMinutesPerWeek,
                maximumMinutesPerMonth, maximumMinutesPerYear);
        updatedContractView.setId(contract.getId());

        RestAssured.given()
                .body(updatedContractView)
                .post("/rest/tenant/{tenantId}/contract/update", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.OK.getStatusCode())
                .body("tenantId", equalTo(TENANT_ID))
                .body("name", equalTo("updatedContract"))
                .body("maximumMinutesPerDay", equalTo(maximumMinutesPerDay))
                .body("maximumMinutesPerWeek", equalTo(maximumMinutesPerWeek))
                .body("maximumMinutesPerMonth", equalTo(maximumMinutesPerMonth))
                .body("maximumMinutesPerYear", equalTo(maximumMinutesPerYear));
    }

    @Test
    public void updateNonMatchingContractTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable (updatedContract)'s tenantId (" +
                TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        ContractView contractView = new ContractView(TENANT_ID, "contract");
        Contract contract = contractService.createContract(TENANT_ID, contractView);

        ContractView updatedContractView = new ContractView(TENANT_ID, "updatedContract");
        updatedContractView.setId(contract.getId());

        RestAssured.given()
                .body(updatedContractView)
                .post("/rest/tenant/{tenantId}/contract/update", 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void updateNonExistentContractTest() {
        String exceptionMessage = "Contract entity with ID (0) not found.";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        ContractView contractView = new ContractView(TENANT_ID, "contract");
        contractView.setId(0L);

        RestAssured.given()
                .body(contractView)
                .post("/rest/tenant/{tenantId}/contract/update", TENANT_ID)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void updateChangeTenantIdContractTest() {
        String exceptionMessage = "The tenantId (0) does not match the persistable (contract)'s tenantId (" + TENANT_ID + ").";
        String exceptionClass = "java.lang.IllegalStateException";

        ContractView contractView = new ContractView(TENANT_ID, "contract");
        Contract contract = contractService.createContract(TENANT_ID, contractView);

        ContractView updatedContract = new ContractView(0, "updatedContract");
        updatedContract.setId(contract.getId());

        RestAssured.given()
                .body(contractView)
                .post("/rest/tenant/{tenantId}/contract/update", 0)
                .then()
                .contentType(ContentType.JSON)
                .statusCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .body("exceptionMessage", equalTo(exceptionMessage))
                .body("exceptionClass", equalTo(exceptionClass));
    }

    @Test
    public void getOrCreateDefaultContractNotExistsTest() {
        Contract contract = contractService.getOrCreateDefaultContract(TENANT_ID);
        assertThat(contract.getName()).isEqualTo("Default Contract");
        assertThat(contract.getMaximumMinutesPerDay()).isNull();
        assertThat(contract.getMaximumMinutesPerWeek()).isNull();
        assertThat(contract.getMaximumMinutesPerMonth()).isNull();
        assertThat(contract.getMaximumMinutesPerYear()).isNull();
    }

    @Test
    public void getOrCreateDefaultContractExistsTest() {
        ContractView contractView = new ContractView();
        contractView.setTenantId(TENANT_ID);
        contractView.setName("Default Contract");
        contractView.setMaximumMinutesPerDay(10);

        Contract contract = contractService.createContract(TENANT_ID, contractView);
        Contract defaultContract = contractService.getOrCreateDefaultContract(TENANT_ID);

        assertThat(defaultContract).isEqualTo(contract);
    }
}
