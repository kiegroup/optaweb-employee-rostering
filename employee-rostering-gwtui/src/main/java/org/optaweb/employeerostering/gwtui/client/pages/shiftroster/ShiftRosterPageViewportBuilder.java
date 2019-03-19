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

package org.optaweb.employeerostering.gwtui.client.pages.shiftroster;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import elemental2.promise.Promise;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.optaweb.employeerostering.gwtui.client.app.spinner.LoadingSpinner;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.common.LocalDateRange;
import org.optaweb.employeerostering.gwtui.client.common.Lockable;
import org.optaweb.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaweb.employeerostering.gwtui.client.util.DateTimeUtils;
import org.optaweb.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaweb.employeerostering.shared.roster.Pagination;
import org.optaweb.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaweb.employeerostering.shared.roster.view.ShiftRosterView;
import org.optaweb.employeerostering.shared.shift.Shift;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.spot.Spot;

import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.DATA_INVALIDATION;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_DATE_RANGE;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_INVALIDATE;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_PAGINATION;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_UPDATE;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_END;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_START;

@Singleton
public class ShiftRosterPageViewportBuilder {

    @Inject
    private PromiseUtils promiseUtils;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private ManagedInstance<ShiftGridObject> shiftGridObjectInstances;

    @Inject
    private EventManager eventManager;

    @Inject
    private LoadingSpinner loadingSpinner;

    @Inject
    private DateTimeUtils dateTimeUtils;

    private ShiftRosterPageViewport viewport;

    private boolean isUpdatingRoster;
    private boolean isSolving;

    private final int WORK_LIMIT_PER_CYCLE = 50;

    private Pagination pagination;
    private LocalDateRange localDateRange;
    private long currentWorkerStartTime;

    @PostConstruct
    public void init() {
        pagination = Pagination.of(0, 10);
        eventManager.subscribeToEventForever(SOLVE_START, (m) -> this.onSolveStart());
        eventManager.subscribeToEventForever(SOLVE_END, (m) -> this.onSolveEnd());
        eventManager.subscribeToEventForever(SHIFT_ROSTER_PAGINATION, (pagination) -> {
            this.pagination = pagination;
            buildShiftRosterViewport(viewport);
        });

        eventManager.subscribeToEventForever(DATA_INVALIDATION, (dataInvalidated) -> {
            if (dataInvalidated.equals(Spot.class) || dataInvalidated.equals(Shift.class)) {
                buildShiftRosterViewport(viewport);
            }
        });

        eventManager.subscribeToEventForever(SHIFT_ROSTER_INVALIDATE, (nil) -> {
            buildShiftRosterViewport(viewport);
        });

        eventManager.subscribeToEventForever(SHIFT_ROSTER_DATE_RANGE, dr -> {
            localDateRange = dr;
            buildShiftRosterViewport(viewport);
        });

        RosterRestServiceBuilder.getRosterState(tenantStore.getCurrentTenantId(),
                                                FailureShownRestCallback.onSuccess((rs) -> {
                                                    LocalDate startDate = dateTimeUtils.getFirstDateOfWeek(rs.getFirstDraftDate());
                                                    LocalDate endDate = dateTimeUtils.getLastDateOfWeek(rs.getFirstDraftDate()).plusDays(1);
                                                    eventManager.fireEvent(SHIFT_ROSTER_DATE_RANGE, new LocalDateRange(startDate, endDate));
                                                }));
    }

    public ShiftRosterPageViewportBuilder withViewport(ShiftRosterPageViewport viewport) {
        this.viewport = viewport;
        return this;
    }

    public RepeatingCommand getWorkerCommand(final ShiftRosterView view, final Lockable<Map<Long, Lane<LocalDateTime, ShiftRosterMetadata>>> lockableLaneMap, final long timeWhenInvoked) {
        currentWorkerStartTime = timeWhenInvoked;

        if (view.getSpotList().isEmpty() && !pagination.isOnFirstPage()) {
            eventManager.fireEvent(SHIFT_ROSTER_PAGINATION, pagination.previousPage());
            return () -> false;
        }

        final Iterator<ShiftView> shiftViewsToAdd = view.getSpotIdToShiftViewListMap().values().stream().flatMap(List::stream).iterator();
        eventManager.fireEvent(SHIFT_ROSTER_UPDATE, view);
        setUpdatingRoster(true);

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
                    while (shiftViewsToAdd.hasNext() && workDone < WORK_LIMIT_PER_CYCLE) {
                        ShiftView toAdd = shiftViewsToAdd.next();
                        if (!laneIdFilteredSet.contains(toAdd.getSpotId())) {
                            Set<Long> shiftViewsId = view.getSpotIdToShiftViewListMap().get(toAdd.getSpotId()).stream().map(sv -> sv.getId()).collect(Collectors.toSet());
                            laneMap.get(toAdd.getSpotId()).filterGridObjects(ShiftGridObject.class,
                                                                             (sv) -> shiftViewsId.contains(sv.getId()));
                            laneIdFilteredSet.add(toAdd.getSpotId());
                        }
                        laneMap.get(toAdd.getSpotId()).addOrUpdateGridObject(
                                ShiftGridObject.class, toAdd.getId(), () -> {
                                    ShiftGridObject out = shiftGridObjectInstances.get();
                                    out.withShiftView(toAdd);
                                    return out;
                                }, (s) -> {
                                    s.withShiftView(toAdd);
                                    return null;
                                });
                        workDone++;
                    }

                    if (!shiftViewsToAdd.hasNext()) {
                        laneMap.forEach((l, lane) -> lane.endModifying());
                        loadingSpinner.hideFor(viewport.getLoadingTaskId());
                        setUpdatingRoster(false);
                        viewport.updateElements();
                    }
                });
                return shiftViewsToAdd.hasNext();
            }
        };
    }

    private void setUpdatingRoster(boolean isUpdatingRoster) {
        this.isUpdatingRoster = isUpdatingRoster;
    }

    public boolean isSolving() {
        return isSolving;
    }

    private long getCurrentWorkerStartTime() {
        return currentWorkerStartTime;
    }

    public void onSolveStart() {
        viewport.lock();
        isSolving = true;
        Scheduler.get().scheduleFixedPeriod(() -> {
            if (!isUpdatingRoster) {
                setUpdatingRoster(true);
                getShiftRosterView().then(srv -> {
                    viewport.refresh(srv);
                    return promiseUtils.resolve();
                });
            }
            return isSolving();
        }, 2000);
    }

    public void onSolveEnd() {
        viewport.unlock();
        isSolving = false;
        eventManager.fireEvent(SHIFT_ROSTER_INVALIDATE);
    }

    public Promise<Void> buildShiftRosterViewport(final ShiftRosterPageViewport toBuild) {
        return getShiftRosterView().then((srv) -> {
            toBuild.refresh(srv);
            return promiseUtils.resolve();
        });
    }

    public Promise<ShiftRosterView> getShiftRosterView() {
        return promiseUtils
                .promise(
                        (res, rej) -> {
                            RosterRestServiceBuilder
                                    .getShiftRosterView(tenantStore.getCurrentTenantId(),
                                                        pagination.getPageNumber(), pagination.getNumberOfItemsPerPage(),
                                                        localDateRange
                                                                .getStartDate()
                                                                .toString(),
                                                        localDateRange.getEndDate().toString(),
                                                        FailureShownRestCallback.onSuccess((s) -> {
                                                            res.onInvoke(s);
                                                        }));
                        });
    }
}
