/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.employeeroster;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Positive2HoursScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.grid.CssGridLinesFactory;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.grid.TicksFactory;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.list.ListElementViewPool;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.TimingUtils;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.roster.view.EmployeeRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Dependent
public class EmployeeRosterViewportFactory {

    @Inject
    private ListElementViewPool<EmployeeBlobView> shiftBlobViewPool;

    @Inject
    private ManagedInstance<EmployeeBlobView> shiftBlobViewInstances;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private CssGridLinesFactory cssGridLinesFactory;

    @Inject
    private TicksFactory<OffsetDateTime> ticksFactory;

    @Inject
    private TimingUtils timingUtils;

    @Inject
    private CommonUtils commonUtils;

    private Map<Employee, List<ShiftView>> employeeShiftRosterModel;

    private Map<Employee, List<EmployeeAvailabilityView>> employeeAvailabilityRosterModel;

    private LinearScale<OffsetDateTime> scale;

    public EmployeeRosterViewport getViewport(final EmployeeRosterView employeeRosterView) {

        return timingUtils.time("Employee Roster viewport instantiation", () -> {

            shiftBlobViewPool.init(1500L, shiftBlobViewInstances::get); //FIXME: Make maxSize variable

            employeeAvailabilityRosterModel = buildEmployeeAvailabilityRosterModel(employeeRosterView);

            employeeShiftRosterModel = buildEmployeeShiftRosterModel(employeeRosterView);

            scale = new Positive2HoursScale(OffsetDateTime.of(employeeRosterView.getStartDate().atTime(0, 0), ZoneOffset.UTC),
                    OffsetDateTime.of(employeeRosterView.getEndDate().atTime(0, 0), ZoneOffset.UTC));

            final List<Lane<OffsetDateTime>> lanes = buildLanes(employeeRosterView);

            return new EmployeeRosterViewport(tenantStore.getCurrentTenantId(),
                    shiftBlobViewPool::get,
                    scale,
                    cssGridLinesFactory.newWithSteps(2L, 12L),
                    ticksFactory.newTicks(scale, 2L, 12L),
                    lanes);
        });
    }

    private Map<Employee, List<EmployeeAvailabilityView>> buildEmployeeAvailabilityRosterModel(final EmployeeRosterView employeeRosterView) {
        return employeeRosterView.getEmployeeList().stream()
                .collect(Collectors.toMap((employee) -> employee,
                        (employee) -> employeeRosterView.getEmployeeIdToAvailabilityViewListMap().get(employee.getId())));
    }

    private Map<Employee, List<ShiftView>> buildEmployeeShiftRosterModel(final EmployeeRosterView employeeRosterView) {
        return employeeRosterView.getEmployeeList().stream()
                .collect(Collectors.toMap((employee) -> employee,
                        (employee) -> employeeRosterView.getEmployeeIdToShiftViewListMap().get(employee.getId())));
    }

    private List<Lane<OffsetDateTime>> buildLanes(final EmployeeRosterView employeeRosterView) {

        final Map<Long, Employee> employeesById = indexById(employeeRosterView.getEmployeeList());
        final Map<Long, Spot> spotsById = indexById(employeeRosterView.getSpotList());

        return employeeRosterView.getEmployeeList()
                .stream()
                .map(e -> new EmployeeLane(e, buildSubLanes(e, employeeRosterView.getEmployeeIdToAvailabilityViewListMap(), employeeRosterView.getEmployeeIdToShiftViewListMap(), employeesById, spotsById)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<SubLane<OffsetDateTime>> buildSubLanes(final Employee employee,
                                                        final Map<Long, List<EmployeeAvailabilityView>> employeeIdToAvailabilityViewList,
                                                        final Map<Long, List<ShiftView>> employeeIdToShiftViewList,
                                                        final Map<Long, Employee> employeeIdToEmployee,
                                                        final Map<Long, Spot> spotIdToSpot) {

        if (employeeIdToAvailabilityViewList.isEmpty()) {
            return new ArrayList<>(singletonList(new SubLane<>()));
        }

        List<EmployeeAvailabilityView> employeeAvailabilities = new ArrayList<>();
        List<ShiftView> employeeShifts = new ArrayList<>();
        for (EmployeeAvailabilityView eav : commonUtils.flatten(employeeIdToAvailabilityViewList.values())) {
            employeeAvailabilities.add(eav);
        }
        for (ShiftView sv : commonUtils.flatten(employeeIdToShiftViewList.values())) {
            employeeShifts.add(sv);
        }

        final List<Blob<OffsetDateTime>> employeeAvailabilitiesBlobs = employeeAvailabilities
                .stream()
                .filter(a -> a.getEmployeeId().equals(employee.getId()))
                .map(a -> {
                    return buildEmployeeAvailabilityBlob(employee, a);
                }).collect(Collectors.toList());

        final List<Blob<OffsetDateTime>> employeeShiftsBlobs = employeeShifts
                .stream()
                .filter(s -> s.getEmployeeId().equals(employee.getId()))
                .map(s -> {
                    return buildEmployeeShiftBlob(employee, spotIdToSpot.get(s.getSpotId()), s);
                }).collect(Collectors.toList());
        // Impossible for an employee to have two employee availabilities at the same time
        return new ArrayList<>(Arrays.asList(new SubLane<>(employeeAvailabilitiesBlobs), new SubLane<>(employeeShiftsBlobs)));//conflictFreeSubLanesFactory.createSubLanes(blobs);
    }

    private EmployeeBlob buildEmployeeAvailabilityBlob(final Employee employee,
                                                       final EmployeeAvailabilityView availabilityView) {

        final EmployeeAvailability availability = new EmployeeAvailability(availabilityView, employee, availabilityView.getDate(), availabilityView.getStartTime(), availabilityView.getEndTime());
        availability.setState(availabilityView.getState());
        return new EmployeeBlob(scale, availability);
    }

    private EmployeeBlob buildEmployeeShiftBlob(final Employee employee,
                                                final Spot spot,
                                                final ShiftView shiftView) {
        final Shift shift = new Shift(shiftView, spot, shiftView.getStartDateTime(), shiftView.getEndDateTime());
        shift.setEmployee(employee);
        return new EmployeeBlob(scale, shift);
    }

    private <T extends AbstractPersistable> Map<Long, T> indexById(final List<T> abstractPersistables) {
        return abstractPersistables.stream().collect(toMap(AbstractPersistable::getId, identity()));
    }
}
