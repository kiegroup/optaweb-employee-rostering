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

package org.optaweb.employeerostering.webapp.roster;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.optaweb.employeerostering.shared.contract.Contract;
import org.optaweb.employeerostering.shared.contract.ContractRestService;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.shared.employee.EmployeeRestService;
import org.optaweb.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.shared.roster.RosterRestService;
import org.optaweb.employeerostering.shared.roster.RosterState;
import org.optaweb.employeerostering.shared.roster.view.AvailabilityRosterView;
import org.optaweb.employeerostering.shared.roster.view.ShiftRosterView;
import org.optaweb.employeerostering.shared.shift.ShiftRestService;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.spot.SpotRestService;
import org.optaweb.employeerostering.shared.tenant.Tenant;
import org.optaweb.employeerostering.webapp.AbstractEntityRequireTenantRestServiceIT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class RosterRestServiceIT extends AbstractEntityRequireTenantRestServiceIT {

    private ShiftRestService shiftRestService;
    private SpotRestService spotRestService;
    private EmployeeRestService employeeRestService;
    private RosterRestService rosterRestService;
    private ContractRestService contractRestService;

    private List<Spot> spotList;
    private List<Employee> employeeList;
    private List<ShiftView> shiftViewList;
    private List<EmployeeAvailabilityView> employeeAvailabilityViewList;

    public RosterRestServiceIT() {
        shiftRestService = serviceClientFactory.createShiftRestServiceClient();
        spotRestService = serviceClientFactory.createSpotRestServiceClient();
        employeeRestService = serviceClientFactory.createEmployeeRestServiceClient();
        rosterRestService = serviceClientFactory.createRosterRestServiceClient();
        contractRestService = serviceClientFactory.createContractRestServiceClient();
    }

    private Contract createContract(String name) {
        Contract contract = new Contract(TENANT_ID, name);
        Contract out = contractRestService.addContract(TENANT_ID, contract);
        assertClientResponseOk();
        return out;
    }

    private Employee createEmployee(String name, Contract contract) {
        Employee employee = new Employee(TENANT_ID, name, contract, Collections.emptySet());
        Employee out = employeeRestService.addEmployee(TENANT_ID, employee);
        assertClientResponseOk();
        return out;
    }

    private Spot createSpot(String name) {
        Spot spot = new Spot(TENANT_ID, name, Collections.emptySet());
        Spot out = spotRestService.addSpot(TENANT_ID, spot);
        assertClientResponseOk();
        return out;
    }

    private ShiftView createShift(Spot spot, Employee employee, LocalDateTime startDateTime, Duration duration) {
        ShiftView shift = new ShiftView(TENANT_ID, spot, startDateTime, startDateTime.plus(duration));
        if (employee != null) {
            shift.setEmployeeId(employee.getId());
        }
        ShiftView out = shiftRestService.addShift(TENANT_ID, shift);
        assertClientResponseOk();
        return out;
    }

    private EmployeeAvailabilityView createEmployeeAvailability(Employee employee, EmployeeAvailabilityState employeeAvailabilityState, LocalDateTime startDateTime, Duration duration) {
        EmployeeAvailabilityView availabilityView = new EmployeeAvailabilityView(TENANT_ID, employee, startDateTime, startDateTime.plus(duration), employeeAvailabilityState);
        EmployeeAvailabilityView out = employeeRestService.addEmployeeAvailability(TENANT_ID, availabilityView);
        assertClientResponseOk();
        return out;
    }

    private void createTestRoster() {
        createTestTenant();

        Contract contract = createContract("contract");
        Employee employeeA = createEmployee("Employee A", contract);
        Employee employeeB = createEmployee("Employee B", contract);

        Spot spotA = createSpot("Spot A");
        Spot spotB = createSpot("Spot B");

        EmployeeAvailabilityView employeeAvailabilityA = createEmployeeAvailability(employeeA, EmployeeAvailabilityState.UNAVAILABLE, LocalDateTime.of(2000, 1, 1, 0, 0), Duration.ofDays(1));

        ShiftView shiftA = createShift(spotA, null, LocalDateTime.of(2000, 1, 1, 9, 0), Duration.ofHours(8));
        ShiftView shiftB = createShift(spotB, employeeB, LocalDateTime.of(2000, 1, 1, 9, 0), Duration.ofHours(8));
        ShiftView shiftC = createShift(spotA, employeeA, LocalDateTime.of(2000, 1, 2, 9, 0), Duration.ofHours(8));
        ShiftView shiftD = createShift(spotB, employeeB, LocalDateTime.of(2000, 1, 2, 9, 0), Duration.ofHours(8));

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
    public void testGetRosterState() {
        RosterState rosterState = new RosterState(null, 7, LocalDate.of(2000, 1, 1), 7, 7, 0, 7, LocalDate.of(1999, 12, 26),
                                                  ZoneOffset.UTC);
        rosterState.setTenant(new Tenant("test"));
        Tenant tenant = createTestTenant(rosterState);
        rosterState.setTenant(tenant);
        rosterState.setTenantId(tenant.getId());

        RosterState receivedRosterState = rosterRestService.getRosterState(TENANT_ID);
        assertThat(receivedRosterState).isEqualToIgnoringGivenFields(rosterState, IGNORED_FIELDS);
    }

    @Test
    public void testGetShiftRosterView() {
        createTestRoster();

        LocalDate startDate = LocalDate.of(2000, 1, 1);
        LocalDate endDate = LocalDate.of(2000, 1, 2);
        ShiftRosterView shiftRosterView = rosterRestService.getShiftRosterView(TENANT_ID, 0, 1, startDate.toString(), endDate.toString());
        assertClientResponseOk();
        assertThat(shiftRosterView).isNotNull();
        assertThat(shiftRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList);
        assertThat(shiftRosterView.getSpotList()).containsExactlyElementsOf(spotList.subList(0, 1));
        assertThat(shiftRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(shiftRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(shiftRosterView.getSpotIdToShiftViewListMap()).containsOnly(entry(spotList.get(0).getId(), Arrays.asList(shiftViewList.get(0))));
        assertThat(shiftRosterView.getTenantId()).isEqualTo(TENANT_ID);

        shiftRosterView = rosterRestService.getShiftRosterView(TENANT_ID, 1, 1, startDate.toString(), endDate.toString());
        assertClientResponseOk();
        assertThat(shiftRosterView).isNotNull();
        assertThat(shiftRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList);
        assertThat(shiftRosterView.getSpotList()).containsExactlyElementsOf(spotList.subList(1, 2));
        assertThat(shiftRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(shiftRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(shiftRosterView.getSpotIdToShiftViewListMap()).containsOnly(entry(spotList.get(1).getId(), Arrays.asList(shiftViewList.get(1))));
        assertThat(shiftRosterView.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    public void testGetAvailabilityRosterView() {
        createTestRoster();

        LocalDate startDate = LocalDate.of(2000, 1, 1);
        LocalDate endDate = LocalDate.of(2000, 1, 2);
        AvailabilityRosterView availabilityRosterView = rosterRestService.getAvailabilityRosterView(TENANT_ID, 0, 1, startDate.toString(), endDate.toString());
        assertClientResponseOk();
        assertThat(availabilityRosterView).isNotNull();
        assertThat(availabilityRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList.subList(0, 1));
        assertThat(availabilityRosterView.getSpotList()).containsExactlyElementsOf(spotList);
        assertThat(availabilityRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(availabilityRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(availabilityRosterView.getEmployeeIdToAvailabilityViewListMap()).containsOnly(entry(employeeList.get(0).getId(), Arrays.asList(employeeAvailabilityViewList.get(0))));
        assertThat(availabilityRosterView.getEmployeeIdToShiftViewListMap()).isEmpty();
        assertThat(availabilityRosterView.getTenantId()).isEqualTo(TENANT_ID);

        availabilityRosterView = rosterRestService.getAvailabilityRosterView(TENANT_ID, 1, 1, startDate.toString(), endDate.toString());
        assertClientResponseOk();
        assertThat(availabilityRosterView).isNotNull();
        assertThat(availabilityRosterView.getEmployeeList()).containsExactlyElementsOf(employeeList.subList(1, 2));
        assertThat(availabilityRosterView.getSpotList()).containsExactlyElementsOf(spotList);
        assertThat(availabilityRosterView.getStartDate()).isEqualTo(startDate);
        assertThat(availabilityRosterView.getEndDate()).isEqualTo(endDate);
        assertThat(availabilityRosterView.getEmployeeIdToAvailabilityViewListMap()).isEmpty();
        assertThat(availabilityRosterView.getEmployeeIdToShiftViewListMap()).containsOnly(entry(employeeList.get(1).getId(), Arrays.asList(shiftViewList.get(1))));
        assertThat(availabilityRosterView.getTenantId()).isEqualTo(TENANT_ID);
    }
}
