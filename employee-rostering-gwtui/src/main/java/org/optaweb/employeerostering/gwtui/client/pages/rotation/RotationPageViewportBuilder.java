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

package org.optaweb.employeerostering.gwtui.client.pages.rotation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import elemental2.promise.Promise;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.optaweb.employeerostering.gwtui.client.app.spinner.LoadingSpinner;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.common.Lockable;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaweb.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaweb.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaweb.employeerostering.shared.rotation.RotationRestServiceBuilder;
import org.optaweb.employeerostering.shared.rotation.view.RotationView;
import org.optaweb.employeerostering.shared.rotation.view.ShiftTemplateView;
import org.optaweb.employeerostering.shared.spot.SpotRestServiceBuilder;

import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.ROTATION_INVALIDATE;

@Singleton
public class RotationPageViewportBuilder {

    @Inject
    private PromiseUtils promiseUtils;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private ManagedInstance<ShiftTemplateModel> shiftTemplateModelInstances;

    @Inject
    private EventManager eventManager;

    @Inject
    private LoadingSpinner loadingSpinner;

    private RotationPageViewport viewport;

    private final int WORK_LIMIT_PER_CYCLE = 50;

    private long currentWorkerStartTime;

    @PostConstruct
    private void init() {
        eventManager.subscribeToEventForever(ROTATION_INVALIDATE, e -> buildRotationViewport(viewport));
    }

    public RotationPageViewportBuilder withViewport(RotationPageViewport viewport) {
        this.viewport = viewport;
        return this;
    }

    public RepeatingCommand getWorkerCommand(final RotationView view, final Lockable<Map<Long, Lane<LocalDateTime, RotationMetadata>>> lockableLaneMap, final long timeWhenInvoked) {
        currentWorkerStartTime = timeWhenInvoked;
        final Iterator<ShiftTemplateView> shiftTemplateViewsToAdd = view.getSpotIdToShiftTemplateViewListMap().values().stream().flatMap(List::stream).iterator();

        return new RepeatingCommand() {

            final long timeWhenStarted = timeWhenInvoked;
            final Set<Long> laneIdFilteredSet = new HashSet<>();

            @Override
            public boolean execute() {
                if (timeWhenStarted != getCurrentWorkerStartTime()) {
                    return false;
                }
                lockableLaneMap.acquireIfPossible(laneMap -> {
                    int workDone = 0;
                    while (shiftTemplateViewsToAdd.hasNext() && workDone < WORK_LIMIT_PER_CYCLE) {
                        ShiftTemplateView toAdd = shiftTemplateViewsToAdd.next();
                        if (!laneIdFilteredSet.contains(toAdd.getSpotId())) {
                            Set<Long> shiftTemplateModelId = view.getSpotIdToShiftTemplateViewListMap().get(toAdd.getSpotId()).stream().map(sv -> sv.getId()).collect(Collectors.toSet());
                            laneMap.get(toAdd.getSpotId()).filterGridObjects(ShiftTemplateModel.class,
                                                                             (sv) -> shiftTemplateModelId.contains(sv.getId()));
                            laneIdFilteredSet.add(toAdd.getSpotId());
                        }
                        laneMap.get(toAdd.getSpotId()).addOrUpdateGridObject(
                                ShiftTemplateModel.class, toAdd.getId(), () -> {
                                    ShiftTemplateModel out = shiftTemplateModelInstances.get();
                                    out.withShiftTemplateView(toAdd);
                                    return out;
                                }, (s) -> {
                                    s.withShiftTemplateView(toAdd);
                                    return null;
                                });
                        workDone++;
                    }

                    if (!shiftTemplateViewsToAdd.hasNext()) {
                        laneMap.forEach((l, lane) -> lane.endModifying());
                        loadingSpinner.hideFor(viewport.getLoadingTaskId());
                        viewport.updateElements();
                    }
                });
                return shiftTemplateViewsToAdd.hasNext();
            }
        };
    }

    private long getCurrentWorkerStartTime() {
        return currentWorkerStartTime;
    }

    public Promise<Void> buildRotationViewport(final RotationPageViewport toBuild) {
        return getRotationView().then((srv) -> {
            toBuild.refresh(srv);
            return promiseUtils.resolve();
        });
    }

    public Promise<RotationView> getRotationView() {

        return promiseUtils.promise((res, rej) -> {
            RosterRestServiceBuilder.getRosterState(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess((rosterState) -> {
                SpotRestServiceBuilder.getSpotList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess((spotList) -> {
                    EmployeeRestServiceBuilder.getEmployeeList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess((employeeList) -> {
                        RotationRestServiceBuilder.getShiftTemplateList(tenantStore.getCurrentTenantId(),
                                                                        FailureShownRestCallback.onSuccess((shiftTemplateViewList) -> {
                                                                            RotationView rotationView = new RotationView();
                                                                            rotationView.setTenantId(tenantStore.getCurrentTenantId());
                                                                            rotationView.setRotationLength(rosterState.getRotationLength());
                                                                            rotationView.setSpotList(spotList);
                                                                            rotationView.setEmployeeList(employeeList);
                                                                            Map<Long, List<ShiftTemplateView>> spotIdToShiftTemplateViewListMap =
                                                                                    new HashMap<>();
                                                                            spotList.forEach(spot -> {
                                                                                spotIdToShiftTemplateViewListMap.put(spot.getId(), new ArrayList<>());
                                                                            });

                                                                            shiftTemplateViewList.forEach(stv -> spotIdToShiftTemplateViewListMap.get(stv.getSpotId()).add(stv));
                                                                            rotationView.setSpotIdToShiftTemplateViewListMap(spotIdToShiftTemplateViewListMap);
                                                                            res.onInvoke(rotationView);
                                                                        }));
                    }));
                }));
            }));
        });
    }
}
