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

package org.optaweb.employeerostering.webapp.rotation;

import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.optaweb.employeerostering.shared.contract.Contract;
import org.optaweb.employeerostering.shared.contract.ContractRestService;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeRestService;
import org.optaweb.employeerostering.shared.roster.PublishResult;
import org.optaweb.employeerostering.shared.roster.RosterRestService;
import org.optaweb.employeerostering.shared.roster.RosterState;
import org.optaweb.employeerostering.shared.rotation.RotationRestService;
import org.optaweb.employeerostering.shared.rotation.view.ShiftTemplateView;
import org.optaweb.employeerostering.shared.shift.ShiftRestService;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.spot.SpotRestService;
import org.optaweb.employeerostering.webapp.AbstractEntityRequireTenantRestServiceIT;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class RotationRestServiceIT extends AbstractEntityRequireTenantRestServiceIT {

    private ShiftRestService shiftRestService;
    private SpotRestService spotRestService;
    private EmployeeRestService employeeRestService;
    private RotationRestService rotationRestService;
    private RosterRestService rosterRestService;
    private ContractRestService contractRestService;

    public RotationRestServiceIT() {
        shiftRestService = serviceClientFactory.createShiftRestServiceClient();
        spotRestService = serviceClientFactory.createSpotRestServiceClient();
        employeeRestService = serviceClientFactory.createEmployeeRestServiceClient();
        rosterRestService = serviceClientFactory.createRosterRestServiceClient();
        rotationRestService = serviceClientFactory.createRotationRestServiceClient();
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

    @Test
    public void testDeleteNonExistingShiftTemplate() {
        final long nonExistingShiftTemplateId = 123456L;
        boolean result = rotationRestService.removeShiftTemplate(TENANT_ID, nonExistingShiftTemplateId);
        assertThat(result).isFalse();
        assertClientResponseOk();
    }

    @Test
    public void testUpdateNonExistingShiftTemplate() {
        final long nonExistingShiftTemplateId = 123456L;
        Spot spot = createSpot("spot");
        Contract contract = createContract("contract");
        Employee rotationEmployee = createEmployee("rotationEmployee", contract);
        Duration startOffset = Duration.ofDays(1);
        Duration shiftDuration = Duration.ofHours(8);

        ShiftTemplateView nonExistingShiftTemplate = createShiftTemplate(spot, rotationEmployee, startOffset, shiftDuration);
        nonExistingShiftTemplate.setId(nonExistingShiftTemplateId);
        ShiftTemplateView updatedShiftTemplate = rotationRestService.updateShiftTemplate(TENANT_ID, nonExistingShiftTemplate);
        assertClientResponseOk();

        assertThat(updatedShiftTemplate.getSpotId()).isEqualTo(nonExistingShiftTemplate.getSpotId());
        assertThat(updatedShiftTemplate.getRotationEmployeeId()).isEqualTo(nonExistingShiftTemplate.getRotationEmployeeId());
        assertThat(updatedShiftTemplate.getDurationBetweenRotationStartAndTemplateStart()).isEqualTo(nonExistingShiftTemplate.getDurationBetweenRotationStartAndTemplateStart());
        assertThat(updatedShiftTemplate.getShiftTemplateDuration()).isEqualTo(nonExistingShiftTemplate.getShiftTemplateDuration());
        assertThat(updatedShiftTemplate.getId()).isNotNull().isNotEqualTo(nonExistingShiftTemplateId);
    }

    @Test
    public void testGetOfNonExistingShiftTemplate() {
        final long nonExistingShiftTemplateId = 123456L;
        assertThatExceptionOfType(javax.ws.rs.NotFoundException.class)
                .isThrownBy(() -> rotationRestService.getShiftTemplate(TENANT_ID, nonExistingShiftTemplateId));
        assertClientResponseError(Response.Status.NOT_FOUND);
    }

    @Test
    public void testCrudShiftTemplate() {
        Spot spot = createSpot("spot");
        Contract contract = createContract("contract");
        Employee rotationEmployee = createEmployee("rotationEmployee", contract);
        Duration startOffset = Duration.ofDays(1);
        Duration shiftDuration = Duration.ofHours(8);

        ShiftTemplateView testAddShiftTemplate = createShiftTemplate(spot, null, startOffset, shiftDuration);
        rotationRestService.addShiftTemplate(TENANT_ID, testAddShiftTemplate);
        assertClientResponseOk();

        List<ShiftTemplateView> shiftTemplates = rotationRestService.getShiftTemplateList(TENANT_ID);
        assertClientResponseOk();
        assertThat(shiftTemplates)
                .usingComparatorForElementFieldsWithType(Comparator.naturalOrder(), Integer.class)
                .usingComparatorForElementFieldsWithType(Comparator.naturalOrder(), Long.class)
                .usingComparatorForElementFieldsWithType(Comparator.naturalOrder(), Duration.class)
                .usingElementComparatorIgnoringFields(IGNORED_FIELDS)
                .containsExactly(testAddShiftTemplate);

        ShiftTemplateView testUpdateShiftTemplate = shiftTemplates.get(0);
        testUpdateShiftTemplate.setRotationEmployeeId(rotationEmployee.getId());
        rotationRestService.updateShiftTemplate(TENANT_ID, testUpdateShiftTemplate);

        ShiftTemplateView retrievedShiftTemplate = rotationRestService.getShiftTemplate(TENANT_ID, testUpdateShiftTemplate.getId());
        assertClientResponseOk();
        assertThat(retrievedShiftTemplate).isNotNull().isEqualToIgnoringGivenFields(testUpdateShiftTemplate, IGNORED_FIELDS);

        boolean result = rotationRestService.removeShiftTemplate(TENANT_ID, retrievedShiftTemplate.getId());
        assertThat(result).isTrue();
        assertClientResponseOk();

        shiftTemplates = rotationRestService.getShiftTemplateList(TENANT_ID);
        assertThat(shiftTemplates).isEmpty();
    }

    @Test
    public void testProvisionAndPublish() {
        Spot spotA = createSpot("Spot A");
        Spot spotB = createSpot("Spot B");

        Contract contract = createContract("contract");
        Employee employeeA = createEmployee("Employee A", contract);
        Employee employeeB = createEmployee("Employee B", contract);

        ShiftTemplateView shiftTemplateA = createShiftTemplate(spotA, employeeA, Duration.ZERO, Duration.ofHours(9));
        ShiftTemplateView shiftTemplateB = createShiftTemplate(spotB, employeeB, Duration.ZERO, Duration.ofHours(9));
        ShiftTemplateView shiftTemplateC = createShiftTemplate(spotA, null, Duration.ofDays(1), Duration.ofHours(9));
        ShiftTemplateView shiftTemplateD = createShiftTemplate(spotB, employeeA, Duration.ofDays(1), Duration.ofHours(9));

        rotationRestService.addShiftTemplate(TENANT_ID, shiftTemplateA);
        rotationRestService.addShiftTemplate(TENANT_ID, shiftTemplateB);
        rotationRestService.addShiftTemplate(TENANT_ID, shiftTemplateC);
        rotationRestService.addShiftTemplate(TENANT_ID, shiftTemplateD);

        assertClientResponseOk();

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
        assertThat(newRosterState.getUnplannedRotationOffset()).isEqualTo(oldRosterState.getPublishLength() % oldRosterState.getRotationLength());
        assertThat(newRosterState.getPublishDeadline()).isEqualTo(oldRosterState.getPublishDeadline().plusDays(oldRosterState.getPublishLength()));
        assertThat(shiftList).hasSize(4);
    }
}
