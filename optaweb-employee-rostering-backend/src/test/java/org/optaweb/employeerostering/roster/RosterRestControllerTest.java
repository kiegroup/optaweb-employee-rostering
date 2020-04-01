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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.optaweb.employeerostering.AbstractEntityRequireTenantRestServiceTest;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.view.ContractView;
import org.optaweb.employeerostering.domain.employee.CovidRiskType;
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

    private ResponseEntity<PublishResult> publishAndProvision() {
        return restTemplate.postForEntity(rosterPathURI + "publishAndProvision", null, PublishResult.class, TENANT_ID);
    }

    private Spot addSpot(String name) {
        SpotView spotView = new SpotView(TENANT_ID, name, Collections.emptySet(), false);
        return restTemplate.postForEntity(spotPathURI + "add", spotView, Spot.class, TENANT_ID).getBody();
    }

    private Contract addContract(String name) {
        ContractView contractView = new ContractView(TENANT_ID, name);
        return restTemplate.postForEntity(contractPathURI + "add", contractView, Contract.class, TENANT_ID).getBody();
    }

    private Employee addEmployee(String name, Contract contract) {
        Employee employee = new Employee(TENANT_ID, name, contract, Collections.emptySet(), CovidRiskType.INOCULATED);
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
}
