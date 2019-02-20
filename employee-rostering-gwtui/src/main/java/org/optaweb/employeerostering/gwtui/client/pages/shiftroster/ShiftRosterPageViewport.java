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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.EventManager;
import org.optaweb.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaweb.employeerostering.gwtui.client.common.Lockable;
import org.optaweb.employeerostering.gwtui.client.util.DateTimeUtils;
import org.optaweb.employeerostering.gwtui.client.viewport.DateTimeViewport;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.HasGridObjects;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.Lane.DummySublane;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.LinearScale;
import org.optaweb.employeerostering.gwtui.client.viewport.impl.DynamicScale;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.roster.RosterState;
import org.optaweb.employeerostering.shared.roster.view.ShiftRosterView;
import org.optaweb.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaweb.employeerostering.shared.shift.view.ShiftView;
import org.optaweb.employeerostering.shared.spot.Spot;

import static org.optaweb.employeerostering.gwtui.client.common.EventManager.Event.SHIFT_ROSTER_INVALIDATE;

@Templated
public class ShiftRosterPageViewport extends DateTimeViewport<ShiftRosterView, ShiftRosterMetadata>
        implements
        IsElement {

    @Inject
    private ShiftRosterPageViewportBuilder viewportBuilder;
    @Inject
    private ManagedInstance<ShiftGridObject> shiftGridObjectInstances;
    @Inject
    private DateTimeUtils dateTimeUtils;
    @Inject
    private EventManager eventManager;

    private RosterState rosterState;
    private Map<Long, Spot> spotIdToSpotMap;
    private Map<Long, Employee> employeeIdToEmployeeMap;

    @PostConstruct
    private void init() {
        viewportBuilder.withViewport(this);
    }

    @Override
    protected void withView(ShiftRosterView view) {
        rosterState = view.getRosterState();
        spotIdToSpotMap = super.getIdMapFor(view.getSpotList(), (s) -> s.getId());
        employeeIdToEmployeeMap = super.getIdMapFor(view.getEmployeeList(), (s) -> s.getId());
    }

    @Override
    protected LinearScale<LocalDateTime> getScaleFor(ShiftRosterView view) {
        LocalDateTime endDateTime = LocalDateTime.of(view.getEndDate(),
                                                     LocalTime.of(0, 0, 0));
        LocalDateTime startDateTime = view.getEndDate().minusDays(20).isAfter(view.getStartDate()) ? endDateTime.minusDays(20) : LocalDateTime.of(view.getStartDate(),
                                                                                                                                                  LocalTime.of(0, 0, 0));
        return new DynamicScale(startDateTime, endDateTime, Duration.ofHours(1));
    }

    @Override
    protected Map<Long, String> getLaneTitlesFor(ShiftRosterView view) {
        return view.getSpotList().stream().collect(Collectors.toMap((s) -> s.getId(), (s) -> s.getName()));
    }

    @Override
    protected RepeatingCommand getViewportBuilderCommand(ShiftRosterView view, Lockable<Map<Long, Lane<LocalDateTime, ShiftRosterMetadata>>> lockableLaneMap) {
        return viewportBuilder.getWorkerCommand(view, lockableLaneMap, System.currentTimeMillis());
    }

    @Override
    protected Function<LocalDateTime, HasGridObjects<LocalDateTime, ShiftRosterMetadata>> getInstanceCreator(ShiftRosterView view, Long laneId) {
        final Spot spot = spotIdToSpotMap.get(laneId);
        final Integer tenantId = view.getTenantId();
        return (t) -> {
            ShiftView shift = new ShiftView(tenantId, spot, t, t.plusHours(8));
            ShiftGridObject out = shiftGridObjectInstances.get().withShiftView(shift);
            ShiftRestServiceBuilder.addShift(tenantId, shift, FailureShownRestCallback.onSuccess(sv -> {
                out.withShiftView(sv);
                getLockableLaneMap().acquire().then(laneMap -> {
                    laneMap.get(laneId).moveAddedGridObjectToIdMap(out);
                    eventManager.fireEvent(SHIFT_ROSTER_INVALIDATE);
                    return promiseUtils.resolve();
                });
            }));
            return out;
        };
    }

    @Override
    protected ShiftRosterMetadata getMetadata() {
        return new ShiftRosterMetadata(rosterState, spotIdToSpotMap, employeeIdToEmployeeMap);
    }

    @Override
    protected Function<LocalDateTime, String> getDateHeaderFunction() {
        return (date) -> {
            return dateTimeUtils.translateLocalDate(date.toLocalDate());
        };
    }

    @Override
    protected Function<LocalDateTime, String> getTimeHeaderFunction() {
        return (date) -> {
            return dateTimeUtils.translateLocalTime(date.toLocalTime());
        };
    }

    @Override
    protected Function<LocalDateTime, List<String>> getDateHeaderIconClassesFunction() {
        return (date) -> (rosterState.isHistoric(date)) ? Collections.emptyList() : (rosterState.isPublished(date)) ? Arrays.asList("fa", "fa-check") : Arrays.asList("fa", "fa-list-alt");
    }

    @Override
    protected String getLoadingTaskId() {
        return "shift-roster";
    }

    @Override
    protected boolean showLoadingSpinner() {
        return !viewportBuilder.isSolving();
    }

    @Override
    protected DummySublane getDummySublane() {
        return DummySublane.BOTTOM;
    }

    @Override
    protected List<Long> getLaneOrder(ShiftRosterView view) {
        return view.getSpotList().stream().map(s -> s.getId()).collect(Collectors.toList());
    }
}
