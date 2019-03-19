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

package org.optaweb.employeerostering.gwtui.client.pages.availabilityroster;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
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
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.employee.EmployeeAvailability;
import org.optaweb.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.shared.roster.Pagination;
import org.optaweb.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaweb.employeerostering.shared.roster.view.AvailabilityRosterView;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;

import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.AVAILABILITY_ROSTER_DATE_RANGE;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.AVAILABILITY_ROSTER_INVALIDATE;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.AVAILABILITY_ROSTER_PAGINATION;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.AVAILABILITY_ROSTER_UPDATE;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.DATA_INVALIDATION;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_END;
import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SOLVE_START;

@Singleton
public class AvailabilityRosterPageViewportBuilder {

    @Inject
    private PromiseUtils promiseUtils;

    @Inject
    private TenantStore tenantStore;

    @Inject
    private ManagedInstance<ShiftGridObject> shiftGridObjectInstances;

    @Inject
    private ManagedInstance<AvailabilityGridObject> employeeAvailabilityGridObjectInstances;

    @Inject
    private EventManager eventManager;

    @Inject
    private LoadingSpinner loadingSpinner;

    @Inject
    private DateTimeUtils dateTimeUtils;

    private AvailabilityRosterPageViewport viewport;

    private boolean isUpdatingRoster;
    private boolean isSolving;

    private final int WORK_LIMIT_PER_CYCLE = 50;

    private long currentWorkerStartTime;
    private Pagination pagination;
    private LocalDateRange localDateRange;

    @PostConstruct
    private void init() {
        pagination = Pagination.of(0, 10);
        eventManager.subscribeToEventForever(SOLVE_START, (m) -> this.onSolveStart());
        eventManager.subscribeToEventForever(SOLVE_END, (m) -> this.onSolveEnd());
        eventManager.subscribeToEventForever(AVAILABILITY_ROSTER_PAGINATION, (pagination) -> {
            this.pagination = pagination;
            buildAvailabilityRosterViewport(viewport);
        });
        eventManager.subscribeToEventForever(AVAILABILITY_ROSTER_INVALIDATE, (nil) -> {
            buildAvailabilityRosterViewport(viewport);
        });
        eventManager.subscribeToEventForever(DATA_INVALIDATION, (dataInvalidated) -> {
            if (dataInvalidated.equals(Employee.class) || dataInvalidated.equals(EmployeeAvailability.class)) {
                buildAvailabilityRosterViewport(viewport);
            }
        });
        eventManager.subscribeToEventForever(AVAILABILITY_ROSTER_DATE_RANGE, dr -> {
            localDateRange = dr;
            buildAvailabilityRosterViewport(viewport);
        });
        RosterRestServiceBuilder.getRosterState(tenantStore.getCurrentTenantId(),
                                                FailureShownRestCallback.onSuccess((rs) -> {
                                                    LocalDate startDate = dateTimeUtils.getFirstDateOfWeek(rs.getFirstDraftDate());
                                                    LocalDate endDate = dateTimeUtils.getLastDateOfWeek(rs.getFirstDraftDate()).plusDays(1);
                                                    eventManager.fireEvent(AVAILABILITY_ROSTER_DATE_RANGE, new LocalDateRange(startDate, endDate));
                                                }));
    }

    public AvailabilityRosterPageViewportBuilder withViewport(AvailabilityRosterPageViewport viewport) {
        this.viewport = viewport;
        return this;
    }

    public RepeatingCommand getWorkerCommand(final AvailabilityRosterView view, final Lockable<Map<Long, Lane<LocalDateTime, AvailabilityRosterMetadata>>> lockableLaneMap, final long timeWhenInvoked) {
        currentWorkerStartTime = timeWhenInvoked;

        if (view.getEmployeeList().isEmpty() && !pagination.isOnFirstPage()) {
            eventManager.fireEvent(AVAILABILITY_ROSTER_PAGINATION, pagination.previousPage());
            return () -> false;
        }

        final Iterator<ShiftView> shiftViewsToAdd = view.getEmployeeIdToShiftViewListMap().values().stream().flatMap(List::stream).iterator();
        final Iterator<EmployeeAvailabilityView> employeeAvaliabilitiesViewsToAdd = view.getEmployeeIdToAvailabilityViewListMap().values().stream().flatMap(List::stream).iterator();

        setUpdatingRoster(true);
        eventManager.fireEvent(AVAILABILITY_ROSTER_UPDATE, view);

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
                        if (!laneIdFilteredSet.contains(toAdd.getEmployeeId())) {
                            Set<Long> shiftViewsId = view.getEmployeeIdToShiftViewListMap().getOrDefault(toAdd.getEmployeeId(), Collections.emptyList()).stream().map(sv -> sv.getId()).collect(Collectors.toSet());
                            Set<Long> availabilityViewsId = view.getEmployeeIdToAvailabilityViewListMap().getOrDefault(toAdd.getEmployeeId(), Collections.emptyList()).stream().map(sv -> sv.getId()).collect(Collectors
                                                                                                                                                                                                                      .toSet());
                            laneMap.get(toAdd.getEmployeeId()).filterGridObjects(ShiftGridObject.class,
                                                                                 (sv) -> shiftViewsId.contains(sv.getId()));
                            laneMap.get(toAdd.getEmployeeId()).filterGridObjects(AvailabilityGridObject.class,
                                                                                 (sv) -> availabilityViewsId.contains(sv.getId()));
                            laneIdFilteredSet.add(toAdd.getEmployeeId());
                        }
                        laneMap.get(toAdd.getEmployeeId()).addOrUpdateGridObject(
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

                    while (employeeAvaliabilitiesViewsToAdd.hasNext() && workDone < WORK_LIMIT_PER_CYCLE) {
                        EmployeeAvailabilityView toAdd = employeeAvaliabilitiesViewsToAdd.next();
                        if (!laneIdFilteredSet.contains(toAdd.getEmployeeId())) {
                            Set<Long> shiftViewsId = view.getEmployeeIdToShiftViewListMap().getOrDefault(toAdd.getEmployeeId(), Collections.emptyList()).stream().map(sv -> sv.getId()).collect(Collectors.toSet());
                            Set<Long> availabilityViewsId = view.getEmployeeIdToAvailabilityViewListMap().getOrDefault(toAdd.getEmployeeId(), Collections.emptyList()).stream().map(sv -> sv.getId()).collect(Collectors
                                                                                                                                                                                                                      .toSet());
                            laneMap.get(toAdd.getEmployeeId()).filterGridObjects(ShiftGridObject.class,
                                                                                 (sv) -> shiftViewsId.contains(sv.getId()));
                            laneMap.get(toAdd.getEmployeeId()).filterGridObjects(AvailabilityGridObject.class,
                                                                                 (sv) -> availabilityViewsId.contains(sv.getId()));
                            laneIdFilteredSet.add(toAdd.getEmployeeId());
                        }
                        laneMap.get(toAdd.getEmployeeId()).addOrUpdateGridObject(
                                AvailabilityGridObject.class, toAdd.getId(), () -> {
                                    AvailabilityGridObject out = employeeAvailabilityGridObjectInstances.get();
                                    out.withEmployeeAvailabilityView(toAdd);
                                    return out;
                                }, (a) -> {
                                    a.withEmployeeAvailabilityView(toAdd);
                                    return null;
                                });
                        workDone++;
                    }

                    if (!shiftViewsToAdd.hasNext() && !employeeAvaliabilitiesViewsToAdd.hasNext()) {
                        laneMap.forEach((l, lane) -> lane.endModifying());
                        loadingSpinner.hideFor(viewport.getLoadingTaskId());
                        setUpdatingRoster(false);
                        viewport.updateElements();
                    }
                });
                return shiftViewsToAdd.hasNext() || employeeAvaliabilitiesViewsToAdd.hasNext();
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
                getAvailabilityRosterView().then(srv -> {
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
    }

    public Promise<Void> buildAvailabilityRosterViewport(final AvailabilityRosterPageViewport toBuild) {
        return getAvailabilityRosterView().then((erv) -> {
            toBuild.refresh(erv);
            return promiseUtils.resolve();
        });
    }

    public Promise<AvailabilityRosterView> getAvailabilityRosterView() {
        return promiseUtils
                .promise(
                        (res, rej) -> RosterRestServiceBuilder.getAvailabilityRosterView(tenantStore.getCurrentTenantId(), pagination.getPageNumber(), pagination.getNumberOfItemsPerPage(),
                                                                                         localDateRange
                                                                                                 .getStartDate()
                                                                                                 .toString(),
                                                                                         localDateRange.getEndDate().toString(),
                                                                                         FailureShownRestCallback.onSuccess((s) -> {
                                                                                             res.onInvoke(s);
                                                                                         })));
    }
}
