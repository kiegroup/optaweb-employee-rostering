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

package org.optaweb.employeerostering.shift;

import java.time.LocalDateTime;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.view.ContractView;
import org.optaweb.employeerostering.domain.employee.CovidRiskType;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.view.EmployeeView;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;
import org.optaweb.employeerostering.service.contract.ContractService;
import org.optaweb.employeerostering.service.employee.EmployeeService;
import org.optaweb.employeerostering.service.shift.ShiftService;
import org.optaweb.employeerostering.service.spot.SpotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@Transactional
public class ShiftServiceTest extends AbstractEntityRequireTenantRestServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ShiftServiceTest.class);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private SpotService spotService;

    @Autowired
    private ContractService contractService;

    @Autowired
    private EmployeeService employeeService;

    private Spot createSpot(Integer tenantId, String name) {
        SpotView spotView = new SpotView(tenantId, name, Collections.emptySet(), false);
        return spotService.createSpot(tenantId, spotView);
    }

    private Contract createContract(Integer tenantId, String name) {
        ContractView contractView = new ContractView(tenantId, name);
        return contractService.createContract(tenantId, contractView);
    }

    private Employee createEmployee(Integer tenantId, String name, Contract contract) {
        EmployeeView employeeView = new EmployeeView(tenantId, name, contract, Collections.emptySet(),
                                                     CovidRiskType.INOCULATED);
        return employeeService.createEmployee(tenantId, employeeView);
    }

    @Before
    public void setup() {
        createTestTenant();
    }

    @After
    public void cleanup() {
        deleteTestTenant();
    }

    @Test
    public void getShiftListTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/shift/", TENANT_ID)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk());
    }

    @Test
    public void getShiftTest() throws Exception {
        Spot spot = createSpot(TENANT_ID, "spot");
        Contract contract = createContract(TENANT_ID, "contract");
        Employee rotationEmployee = createEmployee(TENANT_ID, "rotationEmployee", contract);

        LocalDateTime startDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(8);
        ShiftView shiftView = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime, rotationEmployee);
        ShiftView persistedShift = shiftService.createShift(TENANT_ID, shiftView);

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/shift/{id}", TENANT_ID, persistedShift.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.rotationEmployeeId").value(
                        persistedShift.getRotationEmployeeId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.spotId").value(persistedShift.getSpotId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.startDateTime").value(
                        "2000-01-01T00:00:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.endDateTime").value(
                        "2000-01-01T08:00:00"));
    }

    @Test
    public void getNonExistentShiftTest() throws Exception {
        String exceptionMessage = "No Shift entity found with ID (0).";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        mvc.perform(MockMvcRequestBuilders
                            .get("/rest/tenant/{tenantId}/shift/{id}", TENANT_ID, 0)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }

    @Test
    public void deleteShiftTest() throws Exception {
        Spot spot = createSpot(TENANT_ID, "spot");
        Contract contract = createContract(TENANT_ID, "contract");
        Employee rotationEmployee = createEmployee(TENANT_ID, "rotationEmployee", contract);

        LocalDateTime startDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(8);
        ShiftView shiftView = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime, rotationEmployee);
        ShiftView persistedShift = shiftService.createShift(TENANT_ID, shiftView);

        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/shift/{id}", TENANT_ID, persistedShift.getId())
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("true"));
    }

    @Test
    public void deleteNonExistentShiftTest() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                            .delete("/rest/tenant/{tenantId}/shift/{id}", TENANT_ID, 0)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().string("false"));
    }

    @Test
    public void createShiftTest() throws Exception {
        Spot spot = createSpot(TENANT_ID, "spot");
        Contract contract = createContract(TENANT_ID, "contract");
        Employee rotationEmployee = createEmployee(TENANT_ID, "rotationEmployee", contract);

        LocalDateTime startDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(8);
        ShiftView shiftView = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime, rotationEmployee);
        String body = (new ObjectMapper()).writeValueAsString(shiftView);

        mvc.perform(MockMvcRequestBuilders
                            .post("/rest/tenant/{tenantId}/shift/add", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.rotationEmployeeId").value(
                        shiftView.getRotationEmployeeId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.spotId").value(shiftView.getSpotId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.startDateTime").value(
                        "2000-01-01T00:00:00"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.endDateTime").value(
                        "2000-01-01T08:00:00"));
    }

    @Test
    public void updateShiftTest() throws Exception {
        Spot spot = createSpot(TENANT_ID, "spot");
        Contract contract = createContract(TENANT_ID, "contract");
        Employee rotationEmployee = createEmployee(TENANT_ID, "rotationEmployee", contract);

        LocalDateTime startDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(8);
        ShiftView shiftView = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime, rotationEmployee);
        ShiftView persistedShift = shiftService.createShift(TENANT_ID, shiftView);

        startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59, 59, 0);
        endDateTime = startDateTime.plusHours(10);
        ShiftView updatedShift = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime, rotationEmployee);
        updatedShift.setId(persistedShift.getId());
        String body = (new ObjectMapper()).writeValueAsString(updatedShift);

        mvc.perform(MockMvcRequestBuilders
                            .put("/rest/tenant/{tenantId}/shift/update", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body)
                            .accept(MediaType.APPLICATION_JSON))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.startDateTime").value(
                        "1999-12-31T23:59:59"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.endDateTime").value(
                        "2000-01-01T09:59:59"));
    }

    @Test
    public void updateNonExistentShiftTest() throws Exception {
        String exceptionMessage = "Shift entity with ID (0) not found.";
        String exceptionClass = "javax.persistence.EntityNotFoundException";

        Spot spot = createSpot(TENANT_ID, "spot");
        LocalDateTime startDateTime = LocalDateTime.of(1999, 12, 31, 23, 59, 59, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(10);
        ShiftView updatedShift = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime);
        updatedShift.setId(0L);
        String body = (new ObjectMapper()).writeValueAsString(updatedShift);

        mvc.perform(MockMvcRequestBuilders
                            .put("/rest/tenant/{tenantId}/shift/update", TENANT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                .andDo(mvcResult -> logger.info(mvcResult.toString()))
                .andExpect(status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionMessage").value(exceptionMessage))
                .andExpect(MockMvcResultMatchers.jsonPath("$.exceptionClass").value(exceptionClass));
    }
}
