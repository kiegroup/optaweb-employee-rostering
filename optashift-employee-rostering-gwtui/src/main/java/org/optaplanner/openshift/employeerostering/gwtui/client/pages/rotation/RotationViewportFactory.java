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

package org.optaplanner.openshift.employeerostering.gwtui.client.pages.rotation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.grid.CssGridLinesFactory;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.grid.TicksFactory;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.list.ListElementViewPool;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.LinearScale;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.SubLane;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Viewport;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers.CollisionFreeSubLaneFactory;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.TimingUtils;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.common.HasTimeslot;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.rotation.view.RotationView;
import org.optaplanner.openshift.employeerostering.shared.rotation.view.ShiftTemplateView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class RotationViewportFactory {

    @Inject
    private TenantStore tenantStore;

    @Inject
    private ListElementViewPool<ShiftTemplateBlobView> shiftBlobViewPool;

    @Inject
    private ManagedInstance<ShiftTemplateBlobView> shiftBlobViews;

    @Inject
    private CssGridLinesFactory cssGridLinesFactory;

    @Inject
    private TicksFactory<LocalDateTime> ticksFactory;

    @Inject
    private TimingUtils timingUtils;

    @Inject
    private CollisionFreeSubLaneFactory conflictFreeSubLanesFactory;

    public Viewport<LocalDateTime> getViewport(final RotationView rotationView) {

        return timingUtils.time("Rotation viewport instantiation", () -> {

            shiftBlobViewPool.init(2000L, shiftBlobViews::get);

            final Integer durationInDays = rotationView.getRotationLength();

            final LinearScale<LocalDateTime> scale = new Infinite60MinutesScale(HasTimeslot.EPOCH,
                    HasTimeslot.EPOCH.plusDays(durationInDays));

            Map<Long, Spot> spotsById = indexById(rotationView.getSpotList());
            Map<Long, Employee> employeesById = indexById(rotationView.getEmployeeList());

            final List<Lane<LocalDateTime>> lanes = buildLanes(spotsById, employeesById, rotationView.getSpotIdToShiftTemplateViewListMap(), scale);

            return new RotationViewport(tenantStore.getCurrentTenantId(),
                    HasTimeslot.EPOCH,
                    shiftBlobViewPool::get,
                    scale,
                    cssGridLinesFactory.newWithSteps(2L, 24L),
                    ticksFactory.newTicks(scale, "date-tick", 24L),
                    ticksFactory.newTicks(scale, "time-tick", 4L),
                    lanes,
                    spotsById,
                    employeesById);
        });
    }

    private List<Lane<LocalDateTime>> buildLanes(final Map<Long, Spot> spotsById,
                                                 final Map<Long, Employee> employeesById,
                                                 final Map<Long, List<ShiftTemplateView>> spotIdToShiftTemplateViewListMap,
                                                 final LinearScale<LocalDateTime> scale) {

        return spotIdToShiftTemplateViewListMap.entrySet().stream()
                .map(entry -> newSpotLane(spotsById, employeesById, scale, spotsById.get(entry.getKey()),
                        spotIdToShiftTemplateViewListMap.get(entry.getKey())))
                .collect(toList());
    }

    private SpotLane newSpotLane(final Map<Long, Spot> spotsById,
                                 final Map<Long, Employee> employeesById,
                                 final LinearScale<LocalDateTime> scale,
                                 final Spot spot,
                                 final List<ShiftTemplateView> shiftTemplateViewList) {

        final List<SubLane<LocalDateTime>> subLanes = conflictFreeSubLanesFactory.createSubLanes(
                shiftTemplateViewList.stream().map(shift -> new ShiftTemplateBlob(shift, spotsById, employeesById, scale)));

        return new SpotLane(spot, subLanes);
    }

    private <T extends AbstractPersistable> Map<Long, T> indexById(final List<T> abstractPersistables) {
        return abstractPersistables.stream().collect(toMap(AbstractPersistable::getId, identity()));
    }
}
