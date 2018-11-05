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

package org.optaweb.employeerostering.restclient;

import java.net.URL;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.optaweb.employeerostering.shared.contract.ContractRestService;
import org.optaweb.employeerostering.shared.employee.EmployeeRestService;
import org.optaweb.employeerostering.shared.roster.RosterRestService;
import org.optaweb.employeerostering.shared.rotation.RotationRestService;
import org.optaweb.employeerostering.shared.shift.ShiftRestService;
import org.optaweb.employeerostering.shared.skill.SkillRestService;
import org.optaweb.employeerostering.shared.spot.SpotRestService;
import org.optaweb.employeerostering.shared.tenant.TenantRestService;

/**
 * Creates client proxies to remote REST API endpoints.
 **/
public final class ServiceClientFactory {

    private static final String REST_API_CONTEXT = "rest";

    private URL baseUrl;
    private ResteasyClient resteasyClient;
    private ResteasyWebTarget target;

    /**
     * @param baseUrl URL of the Employee Shift Rostering application
     **/
    public ServiceClientFactory(URL baseUrl) {
        this(baseUrl, new ResteasyClientBuilder().build());
    }

    /**
     * @param baseUrl URL of the Employee Shift Rostering application
     * @param resteasyClient Resteasy client to be used for restclient connections
     **/
    public ServiceClientFactory(URL baseUrl, ResteasyClient resteasyClient) {
        this.baseUrl = baseUrl;
        this.resteasyClient = resteasyClient;
        String baseUrlString = baseUrl.toExternalForm();
        String contextSuffix = baseUrlString.endsWith("/") ? REST_API_CONTEXT : "/" + REST_API_CONTEXT;
        this.target = resteasyClient.target(baseUrlString + contextSuffix);
    }

    public SkillRestService createSkillRestServiceClient() {
        return target.proxy(SkillRestService.class);
    }

    public EmployeeRestService createEmployeeRestServiceClient() {
        return target.proxy(EmployeeRestService.class);
    }

    public RosterRestService createRosterRestServiceClient() {
        return target.proxy(RosterRestService.class);
    }

    public ShiftRestService createShiftRestServiceClient() {
        return target.proxy(ShiftRestService.class);
    }

    public ContractRestService createContractRestServiceClient() {
        return target.proxy(ContractRestService.class);
    }

    public SpotRestService createSpotRestServiceClient() {
        return target.proxy(SpotRestService.class);
    }

    public RotationRestService createRotationRestServiceClient() {
        return target.proxy(RotationRestService.class);
    }

    public TenantRestService createTenantRestServiceClient() {
        return target.proxy(TenantRestService.class);
    }
}
