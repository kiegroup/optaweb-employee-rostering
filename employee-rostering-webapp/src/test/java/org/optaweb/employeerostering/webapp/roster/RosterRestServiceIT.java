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
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaweb.employeerostering.shared.employee.EmployeeRestService;
import org.optaweb.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.shared.roster.RosterRestService;
import org.optaweb.employeerostering.shared.roster.RosterState;
import org.optaweb.employeerostering.shared.shift.ShiftRestService;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.spot.SpotRestService;
import org.optaweb.employeerostering.shared.tenant.Tenant;
import org.optaweb.employeerostering.webapp.AbstractEntityRequireTenantRestServiceIT;

import static org.assertj.core.api.Assertions.assertThat;

public class RosterRestServiceIT extends AbstractEntityRequireTenantRestServiceIT {

    private ShiftRestService shiftRestService;
    private SpotRestService spotRestService;
    private EmployeeRestService employeeRestService;
    private RosterRestService rosterRestService;

    private List<Spot> spotList;
    private List<Employee> employeeList;
    private List<ShiftView> shiftViewList;
    private List<EmployeeAvailabilityView> employeeAvailabilityViewList;

    private final String[] shiftViewNonIndictmentFields = {"employeeId", "spotId", "rotationEmployeeId", "startDateTime", "endDateTime", "tenantId"};

    public RosterRestServiceIT() {
        shiftRestService = serviceClientFactory.createShiftRestServiceClient();
        spotRestService = serviceClientFactory.createSpotRestServiceClient();
        employeeRestService = serviceClientFactory.createEmployeeRestServiceClient();
        rosterRestService = serviceClientFactory.createRosterRestServiceClient();
    }

    private Employee createEmployee(String name) {
        Employee employee = new Employee(TENANT_ID, name);
        return employeeRestService.addEmployee(TENANT_ID, employee);
    }

    private Spot createSpot(String name) {
        Spot spot = new Spot(TENANT_ID, name, Collections.emptySet());
        return spotRestService.addSpot(TENANT_ID, spot);
    }

    private ShiftView createShift(Spot spot, Employee employee, LocalDateTime startDateTime, Duration duration) {
        ShiftView shift = new ShiftView(TENANT_ID, spot, startDateTime, startDateTime.plus(duration));
        if (employee != null) {
            shift.setEmployeeId(employee.getId());
        }
        return shiftRestService.addShift(TENANT_ID, shift);
    }

    private EmployeeAvailabilityView createEmployeeAvailability(Employee employee, EmployeeAvailabilityState employeeAvailabilityState, LocalDateTime startDateTime, Duration duration) {
        EmployeeAvailabilityView availabilityView = new EmployeeAvailabilityView(TENANT_ID, employee, startDateTime, startDateTime.plus(duration), employeeAvailabilityState);
        return employeeRestService.addEmployeeAvailability(TENANT_ID, availabilityView);
    }

    private void createTestRoster() {
        createTestTenant();

        Employee employeeA = createEmployee("Employee A");
        Employee employeeB = createEmployee("Employee B");

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

        RosterState recievedRosterState = rosterRestService.getRosterState(TENANT_ID);
        assertThat(recievedRosterState).isEqualToIgnoringGivenFields(rosterState, IGNORED_FIELDS);
    }

    @Test
    public void testGetShiftRosterView() {

    }

}
