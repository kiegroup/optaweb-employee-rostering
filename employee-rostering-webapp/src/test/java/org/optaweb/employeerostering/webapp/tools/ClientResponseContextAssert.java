/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.webapp.tools;

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
