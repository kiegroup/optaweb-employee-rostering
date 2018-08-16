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

package org.optaweb.employeerostering.webapp.shift;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeRestService;
import org.optaweb.employeerostering.shared.shift.ShiftRestService;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.spot.SpotRestService;
import org.optaweb.employeerostering.webapp.AbstractRestServiceIT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ShiftRestServiceIT extends AbstractRestServiceIT {

    private ShiftRestService shiftRestService;
    private SpotRestService spotRestService;
    private EmployeeRestService employeeRestService;

    private final String[] shiftViewNonIndictmentFields = {"employeeId", "spotId", "rotationEmployeeId", "startDateTime", "endDateTime", "tenantId"};

    public ShiftRestServiceIT() {
        shiftRestService = serviceClientFactory.createShiftRestServiceClient();
        spotRestService = serviceClientFactory.createSpotRestServiceClient();
        employeeRestService = serviceClientFactory.createEmployeeRestServiceClient();
    }

    private Employee createEmployee(String name) {
        Employee employee = new Employee(TENANT_ID, name);
        return employeeRestService.addEmployee(TENANT_ID, employee);
    }

    private Spot createSpot(String name) {
        Spot spot = new Spot(TENANT_ID, name, Collections.emptySet());
        return spotRestService.addSpot(TENANT_ID, spot);
    }

    @Test
    public void testDeleteNonExistingShift() {
        final long nonExistingShiftId = 123456L;
        boolean result = shiftRestService.removeShift(TENANT_ID, nonExistingShiftId);
        assertThat(result).isFalse();
        assertClientResponseOk();
    }

    @Test
    public void testUpdateNonExistingShift() {
        final long nonExistingShiftId = 123456L;
        Spot spot = createSpot("spot");
        Employee employee = createEmployee("employee");
        Employee rotationEmployee = createEmployee("rotationEmployee");
        LocalDateTime startDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(8);

        ShiftView nonExistingShift = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime);
        nonExistingShift.setEmployeeId(employee.getId());
        nonExistingShift.setRotationEmployeeId(rotationEmployee.getId());
        nonExistingShift.setId(nonExistingShiftId);
        ShiftView updatedShift = shiftRestService.updateShift(TENANT_ID, nonExistingShift);

        assertThat(updatedShift.getSpotId()).isEqualTo(updatedShift.getSpotId());
        assertThat(updatedShift.getEmployeeId()).isEqualTo(updatedShift.getEmployeeId());
        assertThat(updatedShift.getStartDateTime()).isEqualTo(updatedShift.getStartDateTime());
        assertThat(updatedShift.getEndDateTime()).isEqualTo(updatedShift.getEndDateTime());
        assertThat(updatedShift.getRotationEmployeeId()).isEqualTo(updatedShift.getRotationEmployeeId());
        assertThat(updatedShift.getId()).isNotNull().isNotEqualTo("spot");
    }

    @Test
    public void testGetOfNonExistingShift() {
        final long nonExistingShiftId = 123456L;
        assertThatExceptionOfType(javax.ws.rs.NotFoundException.class)
                .isThrownBy(() -> shiftRestService.getShift(TENANT_ID, nonExistingShiftId));
        assertClientResponseError(Response.Status.NOT_FOUND);
    }

    @Test
    public void testCrudShift() {
        Spot spot = createSpot("spot");
        Employee employee = createEmployee("employee");
        Employee rotationEmployee = createEmployee("rotationEmployee");
        LocalDateTime startDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusHours(8);

        ShiftView testAddShift = new ShiftView(TENANT_ID, spot, startDateTime, endDateTime);
        shiftRestService.addShift(TENANT_ID, testAddShift);
        assertClientResponseOk();

        List<ShiftView> shifts = shiftRestService.getShifts(TENANT_ID);
        assertClientResponseOk();
        assertThat(shifts)
                .usingComparatorForElementFieldsWithType((a, b) -> a.compareTo(b), Integer.class)
                .usingComparatorForElementFieldsWithType((a, b) -> a.compareTo(b), Long.class)
                .usingComparatorForElementFieldsWithType((a, b) -> a.compareTo(b), LocalDateTime.class)
                .usingElementComparatorOnFields(shiftViewNonIndictmentFields);

        ShiftView testUpdateShift = shifts.get(0);
        testUpdateShift.setEmployeeId(employee.getId());
        testUpdateShift.setRotationEmployeeId(rotationEmployee.getId());
        shiftRestService.updateShift(TENANT_ID, testUpdateShift);

        ShiftView retrievedShift = shiftRestService.getShift(TENANT_ID, testUpdateShift.getId());
        assertClientResponseOk();
        assertThat(retrievedShift).isNotNull().isEqualToComparingOnlyGivenFields(testUpdateShift, shiftViewNonIndictmentFields);

        boolean result = shiftRestService.removeShift(TENANT_ID, retrievedShift.getId());
        assertThat(result).isTrue();
        assertClientResponseOk();

        shifts = shiftRestService.getShifts(TENANT_ID);
        assertThat(shifts).isEmpty();
    }
}
