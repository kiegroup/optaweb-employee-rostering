package org.optaweb.employeerostering.contract;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.view.ContractView;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@QuarkusTest
public class ContractRestControllerTest extends AbstractEntityRequireTenantRestServiceTest {

    private final String contractPathURI = "/rest/tenant/{tenantId}/contract/";

    private Response getContracts(Integer tenantId) {
        return RestAssured.given()
                .basePath(contractPathURI)
                .pathParam("tenantId", tenantId)
                .get();
    }

    private Response getContract(Integer tenantId, Long id) {
        return RestAssured.given()
                .basePath(contractPathURI + id)
                .pathParam("tenantId", tenantId)
                .get();
    }

    private Response deleteContract(Integer tenantId, Long id) {
        return RestAssured.given()
                .basePath(contractPathURI + id)
                .pathParam("tenantId", tenantId)
                .delete();
    }

    private Response addContract(Integer tenantId, ContractView contractView) {
        return RestAssured.given()
                .basePath(contractPathURI + "add")
                .pathParam("tenantId", tenantId)
                .body(contractView)
                .post();
    }

    private Response updateContract(Integer tenantId, ContractView contractView) {
        return RestAssured.given()
                .basePath(contractPathURI + "update")
                .pathParam("tenantId", tenantId)
                .body(contractView)
                .post();
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
    public void contractCrudTest() {
        Integer maximumMinutesPerDay = 50;
        Integer maximumMinutesPerWeek = 250;
        Integer maximumMinutesPerMonth = 1000;
        Integer maximumMinutesPerYear = 12000;

        ContractView contractView = new ContractView(TENANT_ID, "contract", maximumMinutesPerDay, maximumMinutesPerWeek,
                maximumMinutesPerMonth, maximumMinutesPerYear);
        Response postResponse = addContract(TENANT_ID, contractView);
        assertThat(postResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        Contract postedContract = postResponse.as(Contract.class);

        Response response = getContract(TENANT_ID, postedContract.getId());
        assertThat(response.getStatusCode()).isEqualTo(Status.OK.getStatusCode());

        Contract getContract = response.as(Contract.class);
        assertThat(getContract).usingRecursiveComparison().ignoringFields("groovyResponse").isEqualTo(postedContract);

        ContractView updatedContractView = new ContractView(TENANT_ID, "updatedContract", maximumMinutesPerDay,
                maximumMinutesPerWeek, maximumMinutesPerMonth, maximumMinutesPerYear);
        updatedContractView.setId(postedContract.getId());
        Response putResponse = updateContract(TENANT_ID, updatedContractView);
        assertThat(putResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());

        Contract putContract = putResponse.as(Contract.class);
        response = getContract(TENANT_ID, putContract.getId());
        assertThat(putResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(putResponse.getBody()).usingRecursiveComparison().ignoringFields("groovyResponse")
                .isEqualTo(response.getBody());

        deleteContract(TENANT_ID, putContract.getId());

        Response getListResponse = getContracts(TENANT_ID);
        assertThat(getListResponse.getStatusCode()).isEqualTo(Status.OK.getStatusCode());
        assertThat(getListResponse.body().jsonPath().getList(".", Contract.class)).isEmpty();
    }
}
