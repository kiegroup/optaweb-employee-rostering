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

package org.optaweb.employeerostering.webapp.rotation;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeRestService;
import org.optaweb.employeerostering.shared.roster.PublishResult;
import org.optaweb.employeerostering.shared.roster.RosterRestService;
import org.optaweb.employeerostering.shared.roster.RosterState;
import org.optaweb.employeerostering.shared.rotation.view.RotationView;
import org.optaweb.employeerostering.shared.rotation.view.ShiftTemplateView;
import org.optaweb.employeerostering.shared.shift.ShiftRestService;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.spot.SpotRestService;
import org.optaweb.employeerostering.webapp.AbstractEntityRequireTenantRestServiceIT;

import static org.assertj.core.api.Assertions.assertThat;

public class RotationRestServiceIT extends AbstractEntityRequireTenantRestServiceIT {

    private ShiftRestService shiftRestService;
    private SpotRestService spotRestService;
    private EmployeeRestService employeeRestService;
    private RosterRestService rosterRestService;

    public RotationRestServiceIT() {
        shiftRestService = serviceClientFactory.createShiftRestServiceClient();
        spotRestService = serviceClientFactory.createSpotRestServiceClient();
        employeeRestService = serviceClientFactory.createEmployeeRestServiceClient();
        rosterRestService = serviceClientFactory.createRosterRestServiceClient();
    }

    private Employee createEmployee(String name) {
        Employee employee = new Employee(TENANT_ID, name);
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

    private ShiftTemplateView createShiftTemplate(Spot spot, Employee rotationEmployee, Duration offsetFromStart, Duration shiftLength) {
        return new ShiftTemplateView(TENANT_ID, spot.getId(), offsetFromStart, shiftLength, (rotationEmployee != null) ? rotationEmployee.getId() : null);
    }

    @Before
    public void setup() {
        createTestTenant();
    }

    @After
    public void cleanup() {
        deleteTestTenant();
    }

    // TODO: Discuss about removing the updateRotation/getRotation and replacing with
    // getShiftTemplate, getShiftTemplateList, updateShiftTemplate, removeShiftTemplate
    @Test
    public void testCrudRotation() {
        Spot spotA = createSpot("Spot A");
        Spot spotB = createSpot("Spot B");

        Employee employeeA = createEmployee("Employee A");
        Employee employeeB = createEmployee("Employee B");

        ShiftTemplateView shiftTemplateA = createShiftTemplate(spotA, employeeA, Duration.ZERO, Duration.ofHours(9));
        ShiftTemplateView shiftTemplateB = createShiftTemplate(spotB, employeeB, Duration.ZERO, Duration.ofHours(9));
        ShiftTemplateView shiftTemplateC = createShiftTemplate(spotA, null, Duration.ofDays(1), Duration.ofHours(9));
        ShiftTemplateView shiftTemplateD = createShiftTemplate(spotB, employeeA, Duration.ofDays(1), Duration.ofHours(9));

        Map<Long, List<ShiftTemplateView>> spotIdToShiftTemplateViewListMap = new HashMap<>();
        spotIdToShiftTemplateViewListMap.put(spotA.getId(), Arrays.asList(shiftTemplateA, shiftTemplateC));
        spotIdToShiftTemplateViewListMap.put(spotB.getId(), Arrays.asList(shiftTemplateB, shiftTemplateD));

        RotationView rotationView = new RotationView();
        rotationView.setTenantId(TENANT_ID);
        rotationView.setSpotList(Arrays.asList(spotA, spotB));
        rotationView.setEmployeeList(Arrays.asList(employeeA, employeeB));
        rotationView.setSpotIdToShiftTemplateViewListMap(spotIdToShiftTemplateViewListMap);
        rotationView.setRotationLength(7);
        shiftRestService.updateRotation(TENANT_ID, rotationView);
        assertClientResponseEmpty();

        RotationView retrivedRotationView = shiftRestService.getRotation(TENANT_ID);
        assertClientResponseOk();
        assertThat(retrivedRotationView).isEqualToIgnoringGivenFields(rotationView, "spotIdToShiftTemplateViewListMap");
        RosterState rosterState = rosterRestService.getRosterState(TENANT_ID);
        assertClientResponseOk();
        assertThat(rosterState.getRotationLength()).isEqualTo(7);

        rotationView.setRotationLength(14);
        shiftRestService.updateRotation(TENANT_ID, rotationView);
        assertClientResponseEmpty();

        retrivedRotationView = shiftRestService.getRotation(TENANT_ID);
        assertClientResponseOk();
        assertThat(retrivedRotationView).isEqualToIgnoringGivenFields(rotationView, "spotIdToShiftTemplateViewListMap");

        rosterState = rosterRestService.getRosterState(TENANT_ID);
        assertClientResponseOk();
        assertThat(rosterState.getRotationLength()).isEqualTo(14);
    }

    @Test
    public void testProvisionAndPublish() {
        Spot spotA = createSpot("Spot A");
        Spot spotB = createSpot("Spot B");

        Employee employeeA = createEmployee("Employee A");
        Employee employeeB = createEmployee("Employee B");

        ShiftTemplateView shiftTemplateA = createShiftTemplate(spotA, employeeA, Duration.ZERO, Duration.ofHours(9));
        ShiftTemplateView shiftTemplateB = createShiftTemplate(spotB, employeeB, Duration.ZERO, Duration.ofHours(9));
        ShiftTemplateView shiftTemplateC = createShiftTemplate(spotA, null, Duration.ofDays(1), Duration.ofHours(9));
        ShiftTemplateView shiftTemplateD = createShiftTemplate(spotB, employeeA, Duration.ofDays(1), Duration.ofHours(9));

        Map<Long, List<ShiftTemplateView>> spotIdToShiftTemplateViewListMap = new HashMap<>();
        spotIdToShiftTemplateViewListMap.put(spotA.getId(), Arrays.asList(shiftTemplateA, shiftTemplateC));
        spotIdToShiftTemplateViewListMap.put(spotB.getId(), Arrays.asList(shiftTemplateB, shiftTemplateD));

        RotationView rotationView = new RotationView();
        rotationView.setTenantId(TENANT_ID);
        rotationView.setSpotList(Arrays.asList(spotA, spotB));
        rotationView.setEmployeeList(Arrays.asList(employeeA, employeeB));
        rotationView.setSpotIdToShiftTemplateViewListMap(spotIdToShiftTemplateViewListMap);
        rotationView.setRotationLength(2);
        shiftRestService.updateRotation(TENANT_ID, rotationView);
        assertClientResponseEmpty();

        RosterState oldRosterState = rosterRestService.getRosterState(TENANT_ID);
        assertClientResponseOk();
        PublishResult publishResult = rosterRestService.publishAndProvision(TENANT_ID);
        assertClientResponseOk();
        RosterState newRosterState = rosterRestService.getRosterState(TENANT_ID);
        assertClientResponseOk();
        List<ShiftView> shiftList = shiftRestService.getShiftList(TENANT_ID);

        assertThat(publishResult.getPublishedFromDate()).isEqualTo(oldRosterState.getFirstDraftDate());
        assertThat(publishResult.getPublishedToDate()).isEqualTo(oldRosterState.getFirstDraftDate().plusDays(oldRosterState.getPublishLength()));

        assertThat(newRosterState.getFirstDraftDate()).isEqualTo(oldRosterState.getFirstDraftDate().plusDays(oldRosterState.getPublishLength()));
        assertThat(newRosterState.getDraftLength()).isEqualTo(oldRosterState.getDraftLength());
        assertThat(newRosterState.getUnplannedRotationOffset()).isEqualTo(oldRosterState.getPublishLength() % 2);
        assertThat(newRosterState.getPublishDeadline()).isEqualTo(oldRosterState.getPublishDeadline().plusDays(oldRosterState.getPublishLength()));
        assertThat(shiftList).size().isEqualTo(oldRosterState.getPublishLength() * 2);
    }
}
