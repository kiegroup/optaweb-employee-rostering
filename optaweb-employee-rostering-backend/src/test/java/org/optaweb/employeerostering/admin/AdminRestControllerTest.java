package org.optaweb.employeerostering.admin;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;

@QuarkusTest
public class AdminRestControllerTest {

    private final String adminPathURI = "/rest/admin/";

    private Response resetApplication() {
        return RestAssured.post(adminPathURI + "reset");
    }

    @Test
    public void resetApplicationTest() {
        Response resetResponse = resetApplication();
        assertThat(resetResponse.getStatusCode()).isEqualTo(Status.NO_CONTENT.getStatusCode());
    }
}
