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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.CollisionFreeSubLaneFactory;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.TimingUtils;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;
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
    private TicksFactory<OffsetDateTime> ticksFactory;

    @Inject
    private TimingUtils timingUtils;

    @Inject
    private CollisionFreeSubLaneFactory conflictFreeSubLanesFactory;

    private Map<Spot, List<ShiftView>> spotRosterModel;

    private LinearScale<OffsetDateTime> scale;

    public SpotRosterViewport getViewport(final SpotRosterView spotRosterView) {
        return timingUtils.time("Spot Roster viewport instantiation", () -> {
            shiftBlobViewPool.init(1500L, shiftBlobViewInstances::get); //FIXME: Make maxSize variable

            spotRosterModel = buildSpotRosterModel(spotRosterView);

            scale = new Positive2HoursScale(OffsetDateTime.of(spotRosterView.getStartDate().atTime(0, 0), ZoneOffset.UTC),
                    OffsetDateTime.of(spotRosterView.getEndDate().atTime(0, 0), ZoneOffset.UTC));

            final List<Lane<OffsetDateTime>> lanes = buildLanes(spotRosterView);

            return new SpotRosterViewport(tenantStore.getCurrentTenantId(),
                    shiftBlobViewPool::get,
                    scale,
                    cssGridLinesFactory.newWithSteps(2L, 12L),
                    ticksFactory.newTicks(scale, 2L, 12L),
                    lanes);
        });
    }

    private Map<Spot, List<ShiftView>> buildSpotRosterModel(final SpotRosterView spotRosterView) {
        return spotRosterView.getSpotList()
                .stream().collect(Collectors.toMap(spot -> spot,
                        spot -> spotRosterView.getSpotIdToShiftViewListMap().getOrDefault(spot.getId(), new ArrayList<>())));
    }

    private List<Lane<OffsetDateTime>> buildLanes(final SpotRosterView spotRosterView) {

        final Map<Long, Employee> employeesById = indexById(spotRosterView.getEmployeeList());
        return spotRosterModel
                .entrySet()
                .stream()
                .map(e -> new SpotLane(e.getKey(), buildSubLanes(e.getKey(), e.getValue(), employeesById)))
                .collect(toList());
    }

    private List<SubLane<OffsetDateTime>> buildSubLanes(final Spot spot,
                                                        final List<ShiftView> timeSlotsByShift,
                                                        final Map<Long, Employee> employeesById) {

        //FIXME: Handle overlapping blobs and discover why some TimeSlots are null

        if (timeSlotsByShift.isEmpty()) {
            return new ArrayList<>(singletonList(new SubLane<>()));
        }

        final Stream<Blob<OffsetDateTime>> blobs = timeSlotsByShift
                .stream()
                .filter(e -> e != null) //FIXME: Why are there null Time Slots?
                .map(e -> {
                    final ShiftView shiftView = e;
                    final Employee employee = employeesById.get(shiftView.getEmployeeId());
                    return buildShiftBlob(spot, shiftView, employee);
                });

        return conflictFreeSubLanesFactory.createSubLanes(blobs);
    }

    private ShiftBlob buildShiftBlob(final Spot spot,
                                     final ShiftView shiftView,
                                     final Employee employee) {

        final Shift shift = new Shift(shiftView, spot, shiftView.getStartDateTime(), shiftView.getEndDateTime());
        shift.setEmployee(employee);
        return new ShiftBlob(scale, shift);
    }

    private <T extends AbstractPersistable> Map<Long, T> indexById(final List<T> abstractPersistables) {
        return abstractPersistables.stream().collect(toMap(AbstractPersistable::getId, identity()));
    }
}
