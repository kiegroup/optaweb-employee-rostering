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

package org.optaweb.employeerostering.roster;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.view.ContractView;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.domain.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.domain.roster.PublishResult;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.roster.view.AvailabilityRosterView;
import org.optaweb.employeerostering.domain.roster.view.RosterStateView;
import org.optaweb.employeerostering.domain.roster.view.ShiftRosterView;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;
import org.optaweb.employeerostering.domain.tenant.Tenant;
import org.optaweb.employeerostering.util.ShiftRosterXlsxFileIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureTestDatabase
public class RosterRestControllerTest extends AbstractEntityRequireTenantRestServiceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private final String rosterPathURI = "http://localhost:8080/rest/tenant/{tenantId}/roster/";
    private final String spotPathURI = "http://localhost:8080/rest/tenant/{tenantId}/spot/";
    private final String contractPathURI = "http://localhost:8080/rest/tenant/{tenantId}/contract/";
    private final String employeePathURI = "http://localhost:8080/rest/tenant/{tenantId}/employee/";
    private final String shiftPathURI = "http://localhost:8080/rest/tenant/{tenantId}/shift/";
    private final String employeeAvailabilityPathURI =
            "http://localhost:8080/rest/tenant/{tenantId}/employee/availability/";

    private List<Spot> spotList;
    private List<Employee> employeeList;
    private List<ShiftView> shiftViewList;
    private List<EmployeeAvailabilityView> employeeAvailabilityViewList;

    private ResponseEntity<RosterState> getRosterState(Integer id) {
        return restTemplate.getForEntity(rosterPathURI + id, RosterState.class, TENANT_ID);
    }

    private ResponseEntity<ShiftRosterView> getCurrentShiftRosterView(Integer pageNumber,
                                                                      Integer numberOfItemsPerPage) {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(rosterPathURI + "shiftRosterView/current")
                .queryParam("p", pageNumber)
                .queryParam("n", numberOfItemsPerPage)
                .build()
                .expand(Collections.singletonMap("tenantId", TENANT_ID));

        return restTemplate.getForEntity(uriComponents.toUriString(), ShiftRosterView.class);
    }

    private ResponseEntity<ShiftRosterView> getShiftRosterView(Integer pageNumber,
                                                               Integer numberOfItemsPerPage, String startDateString,
                                                               String endDateString) {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(rosterPathURI + "shiftRosterView")
                .queryParam("p", pageNumber)
                .queryParam("n", numberOfItemsPerPage)
                .queryParam("startDate", startDateString)
                .queryParam("endDate", endDateString)
                .build()
                .expand(Collections.singletonMap("tenantId", TENANT_ID));

        return restTemplate.getForEntity(uriComponents.toUriString(), ShiftRosterView.class);
    }

    private ResponseEntity<ShiftRosterView> getShiftRosterViewFor(String startDateString,
                                                                  String endDateString, List<Spot> spots) {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(rosterPathURI + "shiftRosterView/for")
                .queryParam("startDate", startDateString)
                .queryParam("endDate", endDateString)
                .build()
                .expand(Collections.singletonMap("tenantId", TENANT_ID));

        return restTemplate.postForEntity(uriComponents.toUriString(), spots, ShiftRosterView.class);
    }

    private ResponseEntity<AvailabilityRosterView> getCurrentAvailabilityRosterView(Integer pageNumber,
                                                                                    Integer numberOfItemsPerPage) {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(rosterPathURI + "availabilityRosterView" +
                                                                                 "/current")
                .queryParam("p", pageNumber)
                .queryParam("n", numberOfItemsPerPage)
                .build()
                .expand(Collections.singletonMap("tenantId", TENANT_ID));

        return restTemplate.getForEntity(uriComponents.toUriString(), AvailabilityRosterView.class);
    }

    private ResponseEntity<AvailabilityRosterView> getAvailabilityRosterView(Integer pageNumber,
                                                                             Integer numberOfItemsPerPage,
                                                                             String startDateString,
                                                                             String endDateString) {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(rosterPathURI + "availabilityRosterView")
                .queryParam("p", pageNumber)
                .queryParam("n", numberOfItemsPerPage)
                .queryParam("startDate", startDateString)
                .queryParam("endDate", endDateString)
                .build()
                .expand(Collections.singletonMap("tenantId", TENANT_ID));

        return restTemplate.getForEntity(uriComponents.toUriString(), AvailabilityRosterView.class);
    }

    private ResponseEntity<AvailabilityRosterView> getAvailabilityRosterViewFor(String startDateString,
                                                                                String endDateString,
                                                                                List<Employee> employees) {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(rosterPathURI + "availabilityRosterView/for")
                .queryParam("startDate", startDateString)
                .queryParam("endDate", endDateString)
                .build()
                .expand(Collections.singletonMap("tenantId", TENANT_ID));

        return restTemplate.postForEntity(uriComponents.toUriString(), employees, AvailabilityRosterView.class);
    }

    private ResponseEntity<byte[]> getShiftRosterAsExcel(List<Spot> spotList,
                                                         LocalDate startDate,
                                                         LocalDate endDate) {
        UriComponents uriComponents = UriComponentsBuilder.fromUriString(rosterPathURI + "shiftRosterView/excel")
                .queryParam("startDate", startDate.toString())
                .queryParam("endDate", endDate.toString())
                .queryParam("spotList", spotList.stream().map(Spot::getId).map(id -> id.toString())
                        .collect(Collectors.joining(",")))
                .build()
                .expand(Collections.singletonMap("tenantId", TENANT_ID));

        return restTemplate.getForEntity(uriComponents.toUriString(), byte[].class);
    }

    private ResponseEntity<PublishResult> publishAndProvision() {
        return restTemplate.postForEntity(rosterPathURI + "publishAndProvision", null, PublishResult.class, TENANT_ID);
    }

    private ResponseEntity<Void> commitChanges() {
        return restTemplate.postForEntity(rosterPathURI + "commitChanges", null, Void.class, TENANT_ID);
    }

    private Spot addSpot(String name) {
        SpotView spotView = new SpotView(TENANT_ID, name, Collections.emptySet());
        return restTemplate.postForEntity(spotPathURI + "add", spotView, Spot.class, TENANT_ID).getBody();
    }

    private Contract addContract(String name) {
        ContractView contractView = new ContractView(TENANT_ID, name);
        return restTemplate.postForEntity(contractPathURI + "add", contractView, Contract.class, TENANT_ID).getBody();
    }

    private Employee addEmployee(String name, Contract contract) {
        Employee employee = new Employee(TENANT_ID, name, contract, Collections.emptySet());
        return restTemplate.postForEntity(employeePathURI + "add", employee, Employee.class, TENANT_ID).getBody();
    }

    private ShiftView addShift(Spot spot, Employee employee, LocalDateTime startDateTime,
                               Duration duration) {
        ShiftView shiftView = new ShiftView(TENANT_ID, spot, startDateTime, startDateTime.plus(duration));
        if (employee != null) {
            shiftView.setEmployeeId(employee.getId());
        }
        return restTemplate.postForEntity(shiftPathURI + "add", shiftView, ShiftView.class, TENANT_ID).getBody();
    }

    private EmployeeAvailabilityView addEmployeeAvailability(Employee employee,
                                                             EmployeeAvailabilityState
                                                                     employeeAvailabilityState,
                                                             LocalDateTime startDateTime,
                                                             Duration duration) {
        EmployeeAvailabilityView employeeAvailabilityView = new EmployeeAvailabilityView(TENANT_ID, employee,
                                                                                         startDateTime,
                                                                                         startDateTime.plus(duration),
                                                                                         employeeAvailabilityState);
        return restTemplate.postForEntity(employeeAvailabilityPathURI + "add", employeeAvailabilityView,
                                          EmployeeAvailabilityView.class, TENANT_ID).getBody();
    }

    private void createTestRoster() {
        createTestTenant();

        Contract contract = addContract("contract");
        Employee employeeA = addEmployee("Employee A", contract);
        Employee employeeB = addEmployee("Employee B", contract);

        Spot spotA = addSpot("Spot A");
        Spot spotB = addSpot("Spot B");

        EmployeeAvailabilityView employeeAvailabilityA = addEmployeeAvailability(employeeA,
                                                                                 EmployeeAvailabilityState.UNAVAILABLE,
                                                                                 LocalDateTime.of(2000, 1, 1, 0, 0),
                                                                                 Duration.ofDays(1));

        ShiftView shiftA = addShift(spotA, null, LocalDateTime.of(2000, 1, 1, 9, 0), Duration.ofHours(8));
        ShiftView shiftB = addShift(spotB, employeeB, LocalDateTime.of(2000, 1, 1, 9, 0), Duration.ofHours(8));
        ShiftView shiftC = addShift(spotA, employeeA, LocalDateTime.of(2000, 1, 2, 9, 0), Duration.ofHours(8));
        ShiftView shiftD = addShift(spotB, employeeB, LocalDateTime.of(2000, 1, 2, 9, 0), Duration.ofHours(8));

        spotList = Arrays.asList(spotA, spotB);
        employeeList = Arrays.asList(employeeA, employeeB);
        shiftViewList = Arrays.asList(shiftA, shiftB, shiftC, shiftD);
        employeeAvailabilityViewList = Arrays.asList(employeeAvailabilityA);
    }

    @After
    public void cleanup() {
        deleteTestTenant();
    }

    @Test
    public void getRosterStateTest() {
        RosterStateView rosterStateView = new RosterStateView(null, 7, LocalDate.of(2000, 1, 1), 1, 7, 0, 7,
                                                              LocalDate.of(1999, 12, 24),
                                                              ZoneOffset.UTC);
        rosterStateView.setTenant(new Tenant("test"));
        Tenant tenant = createTestTenant(rosterStateView);
        rosterStateView.setTenant(tenant);
        rosterStateView.setTenantId(tenant.getId());

        ResponseEntity<RosterState> rosterStateResponseEntity = getRosterState(TENANT_ID);
        assertThat(rosterStateResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rosterStateResponseEntity.getBody().getPublishNotice()).isEqualTo(7);
        assertThat(rosterStateResponseEntity.getBody().getFirstDraftDate().toString()).isEqualTo("2000-01-01");
        assertThat(rosterStateResponseEntity.getBody().getPublishLength()).isEqualTo(1);
        assertThat(rosterStateResponseEntity.getBody().getDraftLength()).isEqualTo(7);
        assertThat(rosterStateResponseEntity.getBody().getUnplannedRotationOffset()).isEqualTo(0);
        assertThat(rosterStateResponseEntity.getBody().getRotationLength()).isEqualTo(7);
        assertThat(rosterStateResponseEntity.getBody().getLastHistoricDate().toString()).isEqualTo("1999-12-24");
        assertThat(rosterStateResponseEntity.getBody().getTimeZone().toString()).isEqualTo("Z");
        assertThat(rosterStateResponseEntity.getBody().getTenantId()).isEqualTo(TENANT_ID);
        assertThat(rosterStateResponseEntity.getBody().getTenant().getName()).isEqualTo("TestTenant");
        assertThat(rosterStateResponseEntity.getBody().getTenant().getId()).isEqualTo(TENANT_ID);
    }

    @Test
    public void testGetCurrentShiftRosterView() {
        createTestRoster();

        ResponseEntity<ShiftRosterView> shiftRosterViewResponse = getCurrentShiftRosterView(0, 1);
        ShiftRosterView shiftRosterView = shiftRosterViewResponse.getBody();
        assertThat(shiftRosterViewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(shiftRosterView).isNotNull();
        assertThat(shiftRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList);
        assertThat(shiftRosterView.getSpotList()).containsExactlyElementsOf(spotList.subList(0, 1));
        assertThat(shiftRosterView.getTenantId()).isEqualTo(TENANT_ID);

        shiftRosterViewResponse = getCurrentShiftRosterView(1, 1);
        shiftRosterView = shiftRosterViewResponse.getBody();
        assertThat(shiftRosterViewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(shiftRosterView).isNotNull();
        assertThat(shiftRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList);
        assertThat(shiftRosterView.getSpotList()).containsExactlyElementsOf(spotList.subList(1, 2));
        assertThat(shiftRosterView.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    public void testGetShiftRosterView() {
        createTestRoster();

        LocalDate startDate = LocalDate.of(2000, 1, 1);
        LocalDate endDate = LocalDate.of(2000, 1, 2);
        ResponseEntity<ShiftRosterView> shiftRosterViewResponse = getShiftRosterView(0, 1,
                                                                                     startDate.toString(),
                                                                                     endDate.toString());
        ShiftRosterView shiftRosterView = shiftRosterViewResponse.getBody();
        assertThat(shiftRosterViewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(shiftRosterView).isNotNull();
        assertThat(shiftRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList);
        assertThat(shiftRosterView.getSpotList()).containsExactlyElementsOf(spotList.subList(0, 1));
        assertThat(shiftRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(shiftRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(shiftRosterView.getSpotIdToShiftViewListMap()).containsOnly(
                entry(spotList.get(0).getId(), Arrays.asList(shiftViewList.get(0))));
        assertThat(shiftRosterView.getTenantId()).isEqualTo(TENANT_ID);

        shiftRosterViewResponse = getShiftRosterView(1, 1, startDate.toString(), endDate.toString());
        shiftRosterView = shiftRosterViewResponse.getBody();
        assertThat(shiftRosterViewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(shiftRosterView).isNotNull();
        assertThat(shiftRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList);
        assertThat(shiftRosterView.getSpotList()).containsExactlyElementsOf(spotList.subList(1, 2));
        assertThat(shiftRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(shiftRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(shiftRosterView.getSpotIdToShiftViewListMap()).containsOnly(
                entry(spotList.get(1).getId(), Arrays.asList(shiftViewList.get(1))));
        assertThat(shiftRosterView.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    public void testGetShiftRosterViewFor() {
        createTestRoster();

        LocalDate startDate = LocalDate.of(2000, 1, 1);
        LocalDate endDate = LocalDate.of(2000, 1, 2);
        ResponseEntity<ShiftRosterView> shiftRosterViewResponse = getShiftRosterViewFor(startDate.toString(),
                                                                                        endDate.toString(),
                                                                                        Collections.emptyList());
        ShiftRosterView shiftRosterView = shiftRosterViewResponse.getBody();
        assertThat(shiftRosterViewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(shiftRosterView).isNotNull();
        assertThat(shiftRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList);
        assertThat(shiftRosterView.getSpotList()).isEmpty();
        assertThat(shiftRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(shiftRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(shiftRosterView.getTenantId()).isEqualTo(TENANT_ID);

        shiftRosterViewResponse = getShiftRosterViewFor(startDate.toString(), endDate.toString(),
                                                        Collections.emptyList());
        shiftRosterView = shiftRosterViewResponse.getBody();
        assertThat(shiftRosterViewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(shiftRosterView).isNotNull();
        assertThat(shiftRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList);
        assertThat(shiftRosterView.getSpotList()).isEmpty();
        assertThat(shiftRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(shiftRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(shiftRosterView.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    public void testCurrentGetAvailabilityRosterView() {
        createTestRoster();

        LocalDate startDate = LocalDate.of(1999, 12, 24);
        LocalDate endDate = LocalDate.of(2000, 1, 25);
        ResponseEntity<AvailabilityRosterView> availabilityRosterViewResponse = getCurrentAvailabilityRosterView(0, 1);
        AvailabilityRosterView availabilityRosterView = availabilityRosterViewResponse.getBody();
        assertThat(availabilityRosterViewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(availabilityRosterView).isNotNull();
        assertThat(availabilityRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList.subList(0, 1));
        assertThat(availabilityRosterView.getSpotList()).containsExactlyElementsOf(spotList);
        assertThat(availabilityRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(availabilityRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(availabilityRosterView.getEmployeeIdToAvailabilityViewListMap()).containsOnly(
                entry(employeeList.get(0).getId(), Arrays.asList(employeeAvailabilityViewList.get(0))));
        assertThat(availabilityRosterView.getTenantId()).isEqualTo(TENANT_ID);

        availabilityRosterViewResponse = getCurrentAvailabilityRosterView(1, 1);
        availabilityRosterView = availabilityRosterViewResponse.getBody();
        assertThat(availabilityRosterViewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(availabilityRosterView).isNotNull();
        assertThat(availabilityRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList.subList(1, 2));
        assertThat(availabilityRosterView.getSpotList()).containsExactlyElementsOf(spotList);
        assertThat(availabilityRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(availabilityRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(availabilityRosterView.getEmployeeIdToAvailabilityViewListMap()).isEmpty();
        assertThat(availabilityRosterView.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    public void testGetAvailabilityRosterView() {
        createTestRoster();

        LocalDate startDate = LocalDate.of(2000, 1, 1);
        LocalDate endDate = LocalDate.of(2000, 1, 2);
        ResponseEntity<AvailabilityRosterView> availabilityRosterViewResponse = getAvailabilityRosterView(
                0, 1,
                startDate.toString(),
                endDate.toString());
        AvailabilityRosterView availabilityRosterView = availabilityRosterViewResponse.getBody();
        assertThat(availabilityRosterViewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(availabilityRosterView).isNotNull();
        assertThat(availabilityRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList.subList(0, 1));
        assertThat(availabilityRosterView.getSpotList()).containsExactlyElementsOf(spotList);
        assertThat(availabilityRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(availabilityRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(availabilityRosterView.getEmployeeIdToAvailabilityViewListMap()).containsOnly(
                entry(employeeList.get(0).getId(), Arrays.asList(employeeAvailabilityViewList.get(0))));
        assertThat(availabilityRosterView.getEmployeeIdToShiftViewListMap()).isEmpty();
        assertThat(availabilityRosterView.getTenantId()).isEqualTo(TENANT_ID);

        availabilityRosterViewResponse = getAvailabilityRosterView(1, 1, startDate.toString(), endDate.toString());
        availabilityRosterView = availabilityRosterViewResponse.getBody();
        assertThat(availabilityRosterViewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(availabilityRosterView).isNotNull();
        assertThat(availabilityRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList.subList(1, 2));
        assertThat(availabilityRosterView.getSpotList()).containsExactlyElementsOf(spotList);
        assertThat(availabilityRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(availabilityRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(availabilityRosterView.getEmployeeIdToAvailabilityViewListMap()).isEmpty();
        assertThat(availabilityRosterView.getEmployeeIdToShiftViewListMap()).containsOnly(
                entry(employeeList.get(1).getId(), Arrays.asList(shiftViewList.get(1))));
        assertThat(availabilityRosterView.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    public void testGetAvailabilityRosterViewFor() {
        createTestRoster();

        LocalDate startDate = LocalDate.of(2000, 1, 1);
        LocalDate endDate = LocalDate.of(2000, 1, 2);
        ResponseEntity<AvailabilityRosterView> availabilityRosterViewResponse =
                getAvailabilityRosterViewFor(startDate.toString(), endDate.toString(), Collections.emptyList());
        AvailabilityRosterView availabilityRosterView = availabilityRosterViewResponse.getBody();
        assertThat(availabilityRosterViewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(availabilityRosterView).isNotNull();
        assertThat(availabilityRosterView.getEmployeeList()).isEmpty();
        assertThat(availabilityRosterView.getSpotList()).containsExactlyElementsOf(spotList);
        assertThat(availabilityRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(availabilityRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(availabilityRosterView.getEmployeeIdToShiftViewListMap()).isEmpty();
        assertThat(availabilityRosterView.getTenantId()).isEqualTo(TENANT_ID);

        availabilityRosterViewResponse = getAvailabilityRosterViewFor(startDate.toString(), endDate.toString(),
                                                                      Collections.emptyList());
        availabilityRosterView = availabilityRosterViewResponse.getBody();
        assertThat(availabilityRosterViewResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(availabilityRosterView).isNotNull();
        assertThat(availabilityRosterView.getEmployeeList()).isEmpty();
        assertThat(availabilityRosterView.getSpotList()).containsExactlyElementsOf(spotList);
        assertThat(availabilityRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(availabilityRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(availabilityRosterView.getEmployeeIdToAvailabilityViewListMap()).isEmpty();
        assertThat(availabilityRosterView.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    public void testPublishAndProvision() {
        createTestRoster();

        ResponseEntity<PublishResult> publishResultResponseEntity = publishAndProvision();
        assertThat(publishResultResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(publishResultResponseEntity.getBody().getPublishedFromDate()).isEqualTo("2000-01-01");
        assertThat(publishResultResponseEntity.getBody().getPublishedToDate()).isEqualTo("2000-01-08");
    }

    @Test
    public void testGetShiftRosterAsExcel() {
        createTestRoster();

        LocalDate startDate = LocalDate.of(2000, 1, 1);
        LocalDate endDate = LocalDate.of(2000, 1, 2);

        List<Spot> requestSpotList = spotList.subList(0, 1);
        ResponseEntity<byte[]> response = getShiftRosterAsExcel(requestSpotList, startDate, endDate);

        assertThat(response.getStatusCode()).as("Response should be successful for one spot").isEqualTo(HttpStatus.OK);
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
            assertThat(workbook.getNumberOfSheets()).as("There should only be one sheet in the file").isEqualTo(1);
            Sheet spotSheet = workbook.getSheet(requestSpotList.get(0).getName());
            assertSheetForSpotIsCorrect(spotSheet, requestSpotList.get(0), startDate, endDate);
        } catch (IOException e) {
            fail("Unable to read response as an Excel file");
        }

        requestSpotList = spotList;
        response = getShiftRosterAsExcel(requestSpotList, startDate, endDate);

        assertThat(response.getStatusCode()).as("Response should be successful for two spots").isEqualTo(HttpStatus.OK);
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(response.getBody()))) {
            assertThat(workbook.getNumberOfSheets()).as("There should only be two sheets in the file").isEqualTo(2);
            Sheet spotSheet = workbook.getSheet(requestSpotList.get(0).getName());
            assertSheetForSpotIsCorrect(spotSheet, requestSpotList.get(0), startDate, endDate);
            spotSheet = workbook.getSheet(requestSpotList.get(1).getName());
            assertSheetForSpotIsCorrect(spotSheet, requestSpotList.get(1), startDate, endDate);
        } catch (IOException e) {
            fail("Unable to read response as an Excel file");
        }

        Spot badSpot = new Spot(TENANT_ID, "Bad", Collections.emptySet());
        badSpot.setId(-1L);

        requestSpotList = Arrays.asList(badSpot);
        response = getShiftRosterAsExcel(requestSpotList, startDate, endDate);

        assertThat(response.getStatusCode()).as("Response should fail with BAD_REQUEST for non-existing spot in tenant")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private void assertSheetForSpotIsCorrect(Sheet spotSheet, Spot spot, LocalDate from, LocalDate to) {
        assertThat(spotSheet).as("Spot %s sheet should not be null", spot.getName()).isNotNull();

        Map<Long, String> employeeIdToNameList = employeeList.stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));

        Map<List<LocalDateTime>, List<ShiftView>> requestShiftMapByStartAndEndTime = shiftViewList.stream()
                // Include only shift views for this spot, between from and to
                .filter(sv -> sv.getSpotId().equals(spot.getId()) &&
                        // to is exclusive, from is inclusive
                        !sv.getStartDateTime().toLocalDate().isAfter(to.minusDays(1)) &&
                        !sv.getEndDateTime().toLocalDate().isBefore(from))
                .collect(Collectors.groupingBy(sv -> Arrays.asList(sv.getStartDateTime(), sv.getEndDateTime())));

        assertThat(spotSheet.getRow(0).getCell(0).getStringCellValue())
                .as("Header Column A should be Start").isEqualTo("Start");
        assertThat(spotSheet.getRow(0).getCell(1).getStringCellValue())
                .as("Header Column B should be End").isEqualTo("End");
        assertThat(spotSheet.getRow(0).getCell(2).getStringCellValue())
                .as("Header Column C should be Employee").isEqualTo("Employee");

        // Sort startEndTimePairs by start time then end time
        List<List<LocalDateTime>> sortedStartEndTimePairs = requestShiftMapByStartAndEndTime.keySet().stream()
                .sorted(Comparator.comparing((List<LocalDateTime> pair) -> pair.get(0))
                                .thenComparing(pair -> pair.get(1)))
                .collect(Collectors.toList());

        int rowIndex = 1;
        assertThat(requestShiftMapByStartAndEndTime).isNotEmpty();

        for (List<LocalDateTime> startEndTimePair : sortedStartEndTimePairs) {
            // First row of each time slot group has start date time and end date time
            assertThat(spotSheet.getRow(rowIndex).getCell(0).getStringCellValue())
                    .as("Start time for row %d", rowIndex)
                    .isEqualTo(startEndTimePair.get(0).format(ShiftRosterXlsxFileIO.DATE_TIME_FORMATTER));
            assertThat(spotSheet.getRow(rowIndex).getCell(1).getStringCellValue())
                    .as("End time for row %d", rowIndex)
                    .isEqualTo(startEndTimePair.get(1).format(ShiftRosterXlsxFileIO.DATE_TIME_FORMATTER));

            rowIndex++;
            for (ShiftView shiftView : requestShiftMapByStartAndEndTime.get(startEndTimePair)) {
                // Employee name is in the third column
                assertThat(spotSheet.getRow(rowIndex).getCell(2).getStringCellValue())
                        .as("%s to %s Shift's Employee Name", startEndTimePair.get(0).toString(),
                            startEndTimePair.get(1).toString())
                        .isEqualTo(employeeIdToNameList.getOrDefault(shiftView.getEmployeeId(), "Unassigned"));
                rowIndex++;
            }
        }
        assertThat(spotSheet.getRow(rowIndex)).as("Sheet should end on row %d", rowIndex).isNull();
    }

    @Test
    public void testCommitChanges() {
        createTestRoster();

        ResponseEntity<Void> commitChangesResponseEntity = commitChanges();
        assertThat(commitChangesResponseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
