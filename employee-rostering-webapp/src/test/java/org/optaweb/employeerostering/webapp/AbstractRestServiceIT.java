/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.webapp;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneOffset;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.optaweb.employeerostering.restclient.ServiceClientFactory;
import org.optaweb.employeerostering.server.common.jaxrs.OptaWebObjectMapperResolver;
import org.optaweb.employeerostering.shared.roster.RosterState;
import org.optaweb.employeerostering.shared.tenant.Tenant;
import org.optaweb.employeerostering.shared.tenant.TenantRestService;
import org.optaweb.employeerostering.webapp.tools.ClientResponseContextAssert;
import org.optaweb.employeerostering.webapp.tools.RecordingClientResponseFilter;
import org.optaweb.employeerostering.webapp.tools.TestConfig;

public class AbstractRestServiceIT {

    private static final String BASE_TEST_URL = TestConfig.getApplicationUrl();

    // Consequence of reusing JPA entities on client - cannot rely on equals()
    protected static final String[] IGNORED_FIELDS = {"id", "version"};

    protected final ServiceClientFactory serviceClientFactory;
    protected final RecordingClientResponseFilter recordingClientResponseFilter;
    protected TenantRestService tenantRestService;
    protected Integer TENANT_ID;

    protected AbstractRestServiceIT() {
        URL baseTestUrl;
        try {
            baseTestUrl = new URL(BASE_TEST_URL);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Base URL (" + BASE_TEST_URL + ") is invalid.", e);
        }

        recordingClientResponseFilter = new RecordingClientResponseFilter();
        ResteasyClient resteasyClient = new ResteasyClientBuilder().register(recordingClientResponseFilter).build();
        resteasyClient.register(OptaWebObjectMapperResolver.class);
        serviceClientFactory = new ServiceClientFactory(baseTestUrl, resteasyClient);
        tenantRestService = serviceClientFactory.createTenantRestServiceClient();
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

    @Before
    public void createTestTenant() {
        RosterState rosterState = new RosterState(null, 7, LocalDate.of(2000, 1, 1), 7, 24, 0, 7, LocalDate.of(1999, 12, 24),
                                                  ZoneOffset.UTC);
        rosterState.setTenant(new Tenant("TestTenant"));
        Tenant tenant = tenantRestService.addTenant(rosterState);
        TENANT_ID = tenant.getId();
    }

    @After
    public void deleteTestTenant() {
        tenantRestService.removeTenant(TENANT_ID);
    }
}
