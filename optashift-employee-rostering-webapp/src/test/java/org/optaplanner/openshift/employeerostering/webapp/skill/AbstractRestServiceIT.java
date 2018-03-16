package org.optaplanner.openshift.employeerostering.webapp.skill;

import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.Before;
import org.optaplanner.openshift.employeerostering.restclient.ServiceClientFactory;
import org.optaplanner.openshift.employeerostering.webapp.tools.ClientResponseContextAssert;
import org.optaplanner.openshift.employeerostering.webapp.tools.RecordingClientResponseFilter;
import org.optaplanner.openshift.employeerostering.webapp.tools.TestConfig;

public class AbstractRestServiceIT {

    private static final String BASE_TEST_URL = TestConfig.getApplicationUrl();

    // Consequence of reusing JPA entities on client - cannot rely on equals()
    protected static final String[] IGNORED_FIELDS = {"id", "version"};

    protected final ServiceClientFactory serviceClientFactory;
    protected final RecordingClientResponseFilter recordingClientResponseFilter;

    protected AbstractRestServiceIT() {
        URL baseTestUrl;
        try {
            baseTestUrl = new URL(BASE_TEST_URL);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Base URL (" + BASE_TEST_URL + ") is invalid.", e);
        }

        recordingClientResponseFilter = new RecordingClientResponseFilter();
        ResteasyClient resteasyClient = new ResteasyClientBuilder().register(recordingClientResponseFilter).build();
        serviceClientFactory = new ServiceClientFactory(baseTestUrl, resteasyClient);
    }

    protected void assertClientResponseOk() {
        assertClientResponse(Status.OK, MediaType.APPLICATION_JSON);
    }

    protected void assertClientResponseError(final Status expectedStatus) {
        assertClientResponse(expectedStatus, MediaType.TEXT_PLAIN);
    }

    protected void assertClientResponse(final Status expectedStatus, final String expectedMediaType) {
        ClientResponseContextAssert.assertThat(recordingClientResponseFilter.next())
                .hasStatus(expectedStatus)
                .hasMediaType(expectedMediaType);
    }

    @Before
    public void clearClientResponseFilter() {
        recordingClientResponseFilter.clear();
    }
}
