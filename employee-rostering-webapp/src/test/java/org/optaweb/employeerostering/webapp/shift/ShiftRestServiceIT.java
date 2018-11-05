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

package org.optaweb.employeerostering.webapp.shift;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Test;
import org.optaweb.employeerostering.shared.contract.Contract;
import org.optaweb.employeerostering.shared.contract.ContractRestService;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeRestService;
import org.optaweb.employeerostering.shared.shift.ShiftRestService;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.spot.SpotRestService;
import org.optaweb.employeerostering.shared.violation.ShiftEmployeeConflict;
import org.optaweb.employeerostering.webapp.AbstractEntityRequireTenantRestServiceIT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ShiftRestServiceIT extends AbstractEntityRequireTenantRestServiceIT {

    private ShiftRestService shiftRestService;
    private SpotRestService spotRestService;
    private EmployeeRestService employeeRestService;
    private ContractRestService contractRestService;

    private static final String[] SHIFT_VIEW_NON_INDICTMENT_FIELDS = {"employeeId", "spotId", "rotationEmployeeId", "startDateTime", "endDateTime", "tenantId"};

    public ShiftRestServiceIT() {
        shiftRestService = serviceClientFactory.createShiftRestServiceClient();
        spotRestService = serviceClientFactory.createSpotRestServiceClient();
        employeeRestService = serviceClientFactory.createEmployeeRestServiceClient();
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

    @After
    public void cleanup() {
        deleteTestTenant();
    }

    @Test
    public void testDeleteNonExistingShift() {
        createTestTenant();
        final long nonExistingShiftId = 123456L;
        boolean result = shiftRestService.removeShift(TENANT_ID, nonExistingShiftId);
        assertThat(result).isFalse();
        assertClientResponseOk();
    }

    @Test
    public void testUpdateNonExistingShift() {
        createTestTenant();
        final long nonExistingShiftId = 123456L;
        Spot spot = createSpot("spot");
        Contract contract = createContract("contract");
        Employee employee = createEmployee("employee", contract);
        Employee rotationEmployee = createEmployee("rotationEmployee", contract);
        LocalDateTime startDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(8);

        ShiftView nonExistingShift = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime);
        nonExistingShift.setEmployeeId(employee.getId());
        nonExistingShift.setRotationEmployeeId(rotationEmployee.getId());
        nonExistingShift.setId(nonExistingShiftId);
        ShiftView updatedShift = shiftRestService.updateShift(TENANT_ID, nonExistingShift);
        assertClientResponseOk();

        assertThat(updatedShift.getSpotId()).isEqualTo(nonExistingShift.getSpotId());
        assertThat(updatedShift.getEmployeeId()).isEqualTo(nonExistingShift.getEmployeeId());
        assertThat(updatedShift.getStartDateTime()).isEqualTo(nonExistingShift.getStartDateTime());
        assertThat(updatedShift.getEndDateTime()).isEqualTo(nonExistingShift.getEndDateTime());
        assertThat(updatedShift.getRotationEmployeeId()).isEqualTo(nonExistingShift.getRotationEmployeeId());
        assertThat(updatedShift.getId()).isNotNull().isNotEqualTo(nonExistingShiftId);
    }

    @Test
    public void testGetOfNonExistingShift() {
        createTestTenant();
        final long nonExistingShiftId = 123456L;
        assertThatExceptionOfType(javax.ws.rs.NotFoundException.class)
                .isThrownBy(() -> shiftRestService.getShift(TENANT_ID, nonExistingShiftId));
        assertClientResponseError(Response.Status.NOT_FOUND);
    }

    @Test
    public void testCrudShift() {
        createTestTenant();
        Spot spot = createSpot("spot");
        Contract contract = createContract("contract");
        Employee employee = createEmployee("employee", contract);
        Employee rotationEmployee = createEmployee("rotationEmployee", contract);
        LocalDateTime startDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(8);

        ShiftView testAddShift = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime);
        shiftRestService.addShift(TENANT_ID, testAddShift);
        assertClientResponseOk();

        List<ShiftView> shifts = shiftRestService.getShiftList(TENANT_ID);
        assertClientResponseOk();
        assertThat(shifts)
                .usingComparatorForElementFieldsWithType(Comparator.naturalOrder(), Integer.class)
                .usingComparatorForElementFieldsWithType(Comparator.naturalOrder(), Long.class)
                .usingComparatorForElementFieldsWithType(Comparator.naturalOrder(), LocalDateTime.class)
                .usingElementComparatorOnFields(SHIFT_VIEW_NON_INDICTMENT_FIELDS)
                .containsExactly(testAddShift);

        ShiftView testUpdateShift = shifts.get(0);
        testUpdateShift.setEmployeeId(employee.getId());
        testUpdateShift.setRotationEmployeeId(rotationEmployee.getId());
        shiftRestService.updateShift(TENANT_ID, testUpdateShift);

        ShiftView retrievedShift = shiftRestService.getShift(TENANT_ID, testUpdateShift.getId());
        assertClientResponseOk();
        assertThat(retrievedShift).isNotNull().isEqualToComparingOnlyGivenFields(testUpdateShift, SHIFT_VIEW_NON_INDICTMENT_FIELDS);

        boolean result = shiftRestService.removeShift(TENANT_ID, retrievedShift.getId());
        assertThat(result).isTrue();
        assertClientResponseOk();

        shifts = shiftRestService.getShiftList(TENANT_ID);
        assertThat(shifts).isEmpty();
    }

    @Test
    public void testShiftWithDaylightSavingTime() {
        createTestTenant(ZoneId.of("America/New_York"));
        Spot spot = createSpot("spot");
        Contract contract = createContract("contract");
        Employee employee = createEmployee("employee", contract);
        LocalDateTime startDateTime = LocalDateTime.of(2018, 3, 10, 23, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(8);

        ShiftView testAddShift = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime);
        testAddShift.setEmployeeId(employee.getId());

        ShiftView addedShift = shiftRestService.addShift(TENANT_ID, testAddShift);
        ShiftView retrievedShift = shiftRestService.getShift(TENANT_ID, addedShift.getId());
        assertClientResponseOk();
        assertThat(retrievedShift).isNotNull().isEqualToComparingOnlyGivenFields(testAddShift, SHIFT_VIEW_NON_INDICTMENT_FIELDS);

        // Trigger an indictment so we can read zone offset
        LocalDateTime conflictingStartDateTime = endDateTime.plusHours(9);
        LocalDateTime conflictingEndDateTime = conflictingStartDateTime.plusHours(8);

        ShiftView testConflictingShift = new ShiftView(TENANT_ID, spot, conflictingStartDateTime, conflictingEndDateTime);
        testConflictingShift.setEmployeeId(employee.getId());
        addedShift = shiftRestService.addShift(TENANT_ID, testConflictingShift);
        ShiftView retrievedConflictingShift = shiftRestService.getShift(TENANT_ID, addedShift.getId());

        assertThat(retrievedShift.getEndDateTime().plusHours(9)).isEqualTo(retrievedConflictingShift.getStartDateTime());
        assertThat(retrievedConflictingShift.getShiftEmployeeConflictList()).hasSize(1);
        ShiftEmployeeConflict shiftEmployeeConflict = retrievedConflictingShift.getShiftEmployeeConflictList().get(0);
        assertThat(shiftEmployeeConflict.getRightShift().getStartDateTime().getOffset()).isNotEqualTo(shiftEmployeeConflict.getRightShift().getEndDateTime().getOffset());
        assertThat(Duration.between(shiftEmployeeConflict.getRightShift().getStartDateTime(), shiftEmployeeConflict.getRightShift().getEndDateTime()).toHours()).isEqualTo(7);

        assertThat(shiftEmployeeConflict.getLeftShift().getStartDateTime().getOffset()).isEqualTo(shiftEmployeeConflict.getLeftShift().getEndDateTime().getOffset());
        assertThat(Duration.between(shiftEmployeeConflict.getLeftShift().getStartDateTime(), shiftEmployeeConflict.getLeftShift().getEndDateTime()).toHours()).isEqualTo(8);
    }
}
