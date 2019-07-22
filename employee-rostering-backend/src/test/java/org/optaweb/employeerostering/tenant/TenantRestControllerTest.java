/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.tenant;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TenantRestControllerTest {

    @Autowired
    private TestRestTemplate tenantRestTemplate;

    private String tenantPathURI = "http://localhost:8080/rest/tenant/";

    private ResponseEntity<List<Tenant>> getTenants() {
        return tenantRestTemplate.exchange(tenantPathURI, HttpMethod.GET, null,
                                          new ParameterizedTypeReference<List<Tenant>>() {});
    }

    private ResponseEntity<Tenant> getTenant(Integer id) {
        return tenantRestTemplate.getForEntity(tenantPathURI + id, Tenant.class);
    }

    @Test
    public void getTenantListTest() {
        Tenant mockTenant = new Tenant("mockTenant");
        mockTenant.setId(1);
        mockTenant.setVersion(0L);

        ResponseEntity<List<Tenant>> response = getTenants();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(mockTenant);
    }

    @Test
    public void getTenantTest() {
        Tenant mockTenant = new Tenant("mockTenant");
        mockTenant.setId(1);
        mockTenant.setVersion(0L);

        ResponseEntity<Tenant> response = getTenant(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockTenant);
    }
}
