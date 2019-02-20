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

package org.optaweb.employeerostering.gwtui.client.viewport;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import elemental2.dom.HTMLDivElement;
import org.jboss.errai.common.client.dom.elemental2.Elemental2DomUtil;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.optaweb.employeerostering.gwtui.client.app.spinner.LoadingSpinner;
import org.optaweb.employeerostering.gwtui.client.common.JQuery;
import org.optaweb.employeerostering.gwtui.client.common.Lockable;
import org.optaweb.employeerostering.gwtui.client.header.HeaderView;
import org.optaweb.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.DateTimeHeader;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.GridObjectPlacer;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.HasGridObjects;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.LinearScale;

public abstract class DateTimeViewport<T, M> {

    @Inject
    @DataField("date-time-header")
    private DateTimeHeader dateTimeHeader;

    @Inject
    @DataField("lane-container")
    private HTMLDivElement laneContainer;

    @Inject
    @DataField("viewport-overlay")
    private HTMLDivElement viewportOverlay;

    @Inject
    private ManagedInstance<Lane<LocalDateTime, M>> laneInstance;

    @Inject
    private HeaderView headerView;

    @Inject
    protected PromiseUtils promiseUtils;

    @Inject
    private Lockable<Map<Long, Lane<LocalDateTime, M>>> lockableLaneMap;

    private Map<Long, Lane<LocalDateTime, M>> laneMap;

    @Inject
    private LoadingSpinner loadingSpinner;

    @Inject
    private Elemental2DomUtil domUtils;

    private GridObjectPlacer gridObjectPlacer;
    private LinearScale<LocalDateTime> scale;

    private T view;

    protected abstract void withView(T view);

    protected abstract M getMetadata();

    protected abstract Function<LocalDateTime, HasGridObjects<LocalDateTime, M>> getInstanceCreator(T view, Long laneId);

    protected abstract LinearScale<LocalDateTime> getScaleFor(T view);

    protected abstract Map<Long, String> getLaneTitlesFor(T view);

    protected abstract List<Long> getLaneOrder(T view);

    protected abstract RepeatingCommand getViewportBuilderCommand(T view, Lockable<Map<Long, Lane<LocalDateTime, M>>> lockableLaneMap);

    protected abstract Function<LocalDateTime, String> getDateHeaderFunction();

    protected abstract Function<LocalDateTime, String> getTimeHeaderFunction();

    protected abstract Function<LocalDateTime, List<String>> getDateHeaderIconClassesFunction();

    protected abstract String getLoadingTaskId();

    protected abstract boolean showLoadingSpinner();

    protected abstract Lane.DummySublane getDummySublane();

    @PostConstruct
    private void init() {
        viewportOverlay.hidden = true;
        gridObjectPlacer = GridObjectPlacer.HORIZONTAL;
        laneMap = new HashMap<>();
        lockableLaneMap.setInstance(laneMap);
    }

    public void refresh(T view) {
        if (showLoadingSpinner()) {
            loadingSpinner.showFor(getLoadingTaskId());
        }
        promiseUtils.manage(lockableLaneMap.acquire().then(laneMap -> {
            this.view = view;
            withView(view);
            // Need to defer it so we have height information
            Scheduler.get().scheduleDeferred(() -> {
                dateTimeHeader.getElement().style.top = JQuery.get(headerView.getElement()).height() + "px";
            });
            scale = getScaleFor(view);

            dateTimeHeader.generateTicks(gridObjectPlacer, scale, 0L,
                                         getDateHeaderFunction(),
                                         getTimeHeaderFunction(),
                                         getDateHeaderIconClassesFunction());

            Map<Long, String> viewLanes = getLaneTitlesFor(view);
            for (Long laneId : viewLanes.keySet()) {
                Lane<LocalDateTime, M> lane = laneInstance.get().withDummySublane(getDummySublane()).withGridObjectPlacer(gridObjectPlacer)
                        .withScale(scale).withTitle(viewLanes.get(laneId))
                        .withGridObjectCreator(getInstanceCreator(view, laneId));
                laneMap.put(laneId, lane);
            }

            final M metadata = getMetadata();
            laneMap.forEach((l, lane) -> {
                lane.setMetadata(metadata);
                lane.startModifying();
            });
            Scheduler.get().scheduleIncremental(getViewportBuilderCommand(view, lockableLaneMap));
            return promiseUtils.resolve();
        }));
    }

    public void updateElements() {
        domUtils.removeAllElementChildren(laneContainer);
        getLaneOrder(view).forEach(laneId -> laneContainer.appendChild(laneMap.get(laneId).getElement()));
    }

    public void lock() {
        viewportOverlay.hidden = false;
    }

    public void unlock() {
        viewportOverlay.hidden = true;
    }

    public <X> Map<Long, X> getIdMapFor(Collection<X> collection, Function<X, Long> idMapper) {
        return collection.stream().collect(Collectors.toMap((o) -> idMapper.apply(o), (o) -> o));
    }

    protected Lockable<Map<Long, Lane<LocalDateTime, M>>> getLockableLaneMap() {
        return lockableLaneMap;
    }
}
