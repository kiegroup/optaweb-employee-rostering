package org.optaplanner.openshift.employeerostering.webapp.tools;

import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.assertj.core.api.AbstractAssert;

/**
 * Assert for a response from a server received by jax-rs client.
 **/
public final class ClientResponseContextAssert extends
                                               AbstractAssert<ClientResponseContextAssert, ClientResponseContext> {

    public static ClientResponseContextAssert assertThat(final ClientResponseContext clientResponseContext) {
        return new ClientResponseContextAssert(clientResponseContext);
    }

    public ClientResponseContextAssert(final ClientResponseContext clientResponseContext) {
        super(clientResponseContext, ClientResponseContextAssert.class);
    }

    public ClientResponseContextAssert hasStatus(final int statusCode) {
        isNotNull();

        if (actual.getStatus() != statusCode) {
            failWithMessage("Expected status code to be <%s> but was <%s>", statusCode, actual.getStatus());
        }

        return myself;
    }

    public ClientResponseContextAssert hasStatus(final Response.Status status) {
        return hasStatus(status.getStatusCode());
    }

    public ClientResponseContextAssert hasMediaType(final String expectedMediaType) {
        isNotNull();

        final MediaType actualMediaType = actual.getMediaType();
        if (expectedMediaType == null && actualMediaType != null) {
            failWithMessage("Expected content type to be null but was <%s>", actualMediaType.toString());
        }

        if (!MediaType.valueOf(expectedMediaType).isCompatible(actual.getMediaType())) {
            failWithMessage("Expected content type to be <%s> but was <%s>", expectedMediaType,
                            actualMediaType.toString());
        }
        return myself;
    }
}
