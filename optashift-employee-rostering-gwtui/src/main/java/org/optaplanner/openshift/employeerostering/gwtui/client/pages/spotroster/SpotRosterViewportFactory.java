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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.spotroster;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.grid.CssGridLinesFactory;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.grid.TicksFactory;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.list.ListElementViewPool;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.beta.java.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Dependent
public class SpotRosterViewportFactory {

    @Inject
    private ListElementViewPool<ShiftBlobView> shiftBlobViewPool;

    @Inject
    private ManagedInstance<ShiftBlobView> shiftBlobViewInstances;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private CssGridLinesFactory cssGridLinesFactory;

    @Inject
    private TicksFactory<LocalDateTime> ticksFactory;

    private Map<Spot, Map<ShiftView, TimeSlot>> spotRosterModel;

    private LinearScale<LocalDateTime> scale;

    public SpotRosterViewport getViewport(final SpotRosterView spotRosterView) {

        shiftBlobViewPool.init(1500L, shiftBlobViewInstances::get); //FIXME: Make maxSize variable

        spotRosterModel = buildSpotRosterModel(spotRosterView);

        scale = new Positive2HoursScale(spotRosterView.getStartDate().atTime(0, 0),
                                        spotRosterView.getEndDate().atTime(0, 0));

        return new SpotRosterViewport(tenantStore.getCurrentTenantId(),
                                      shiftBlobViewPool::get,
                                      scale,
                                      cssGridLinesFactory.newWithSteps(2L, 12L),
                                      ticksFactory.newTicks(scale, 2L, 12L),
                                      buildLanes(spotRosterView)
        );
    }

    private Map<Spot, Map<ShiftView, TimeSlot>> buildSpotRosterModel(final SpotRosterView spotRosterView) {

        final Map<Long, Spot> spotsById = indexById(spotRosterView.getSpotList());
        final Map<Long, TimeSlot> timeSlotsById = indexById(spotRosterView.getTimeSlotList());

        return spotRosterView.getTimeSlotIdToSpotIdToShiftViewListMap().values().stream()
                .flatMap(s -> s.values().stream())
                .flatMap(Collection::stream)
                .collect(groupingBy(shiftView -> spotsById.get(shiftView.getSpotId()),
                                    toMap(identity(), shiftView -> timeSlotsById.get(shiftView.getTimeSlotId()))));
    }

    private List<Lane<LocalDateTime>> buildLanes(final SpotRosterView spotRosterView) {

        final Map<Long, Employee> employeesById = indexById(spotRosterView.getEmployeeList());

        return spotRosterModel
                .entrySet()
                .stream()
                .map(e -> new SpotLane(e.getKey(), buildSubLanes(e.getKey(), e.getValue(), employeesById)))
                .collect(toList());
    }

    private List<SubLane<LocalDateTime>> buildSubLanes(final Spot spot,
                                                       final Map<ShiftView, TimeSlot> timeSlotsByShift,
                                                       final Map<Long, Employee> employeesById) {

        //FIXME: Handle overlapping blobs and discover why some TimeSlots are null

        if (timeSlotsByShift.isEmpty()) {
            return new ArrayList<>(singletonList(new SubLane<>(new ArrayList<>())));
        }

        final List<Blob<LocalDateTime>> blobs = timeSlotsByShift.entrySet()
                .stream()
                .filter(e -> e.getValue() != null) //FIXME: Why are there null Time Slots?
                .map(e -> {
                    final ShiftView shiftView = e.getKey();
                    final TimeSlot timeSlot = e.getValue();
                    final Employee employee = employeesById.get(shiftView.getEmployeeId());
                    return buildShiftBlob(spot, shiftView, timeSlot, employee);
                })
                .collect(toList());

        return new ArrayList<>(singletonList(new SubLane<>(blobs)));
    }

    private ShiftBlob buildShiftBlob(final Spot spot,
                                     final ShiftView shiftView,
                                     final TimeSlot timeSlot,
                                     final Employee employee) {

        final Shift shift = new Shift(shiftView, spot, timeSlot);
        shift.setEmployee(employee);
        return new ShiftBlob(scale, shift);
    }

    private <T extends AbstractPersistable> Map<Long, T> indexById(final List<T> abstractPersistables) {
        return abstractPersistables.stream().collect(toMap(AbstractPersistable::getId, identity()));
    }
}
