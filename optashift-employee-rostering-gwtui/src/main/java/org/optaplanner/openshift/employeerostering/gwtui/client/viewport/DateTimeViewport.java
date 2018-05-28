package org.optaplanner.openshift.employeerostering.gwtui.client.viewport;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import elemental2.dom.HTMLDivElement;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.JQuery;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.Lockable;
import org.optaplanner.openshift.employeerostering.gwtui.client.header.HeaderView;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.DateTimeHeader;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.GridObjectPlacer;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.HasGridObjects;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.LinearScale;

public abstract class DateTimeViewport<T, M> {

    @Inject
    @DataField("date-time-header")
    private DateTimeHeader dateTimeHeader;

    @Inject
    @DataField("lane-container")
    private HTMLDivElement laneContainer;

    @Inject
    private ManagedInstance<Lane<LocalDateTime, M>> laneInstance;

    @Inject
    private HeaderView headerView;
    
    @Inject
    protected PromiseUtils promiseUtils;
    
    @Inject
    private Lockable<Map<Long, Lane<LocalDateTime, M>>> lockableLaneMap;

    private GridObjectPlacer gridObjectPlacer;
    private LinearScale<LocalDateTime> scale;

    protected abstract void withView(T view);

    protected abstract M getMetadata();

    protected abstract Function<LocalDateTime, HasGridObjects<LocalDateTime, M>> getInstanceCreator(T view, Long laneId);

    protected abstract LinearScale<LocalDateTime> getScaleFor(T view);

    protected abstract Map<Long, String> getLaneTitlesFor(T view);

    protected abstract RepeatingCommand getViewportBuilderCommand(T view, Lockable<Map<Long, Lane<LocalDateTime, M>>> lockableLaneMap);

    protected abstract Function<LocalDateTime, String> getDateHeaderFunction();

    protected abstract Function<LocalDateTime, String> getTimeHeaderFunction();

    protected abstract Function<LocalDateTime, List<String>> getDateHeaderAdditionalClassesFunction();

    @PostConstruct
    private void init() {
        gridObjectPlacer = GridObjectPlacer.HORIZONTAL;
        lockableLaneMap.setInstance(new HashMap<>());
    }

    public void refresh(T view) {
        lockableLaneMap.acquire().then(laneMap -> {
            withView(view);
            // Need to defer it so we have height information
            Scheduler.get().scheduleDeferred(() -> {
                dateTimeHeader.getElement().style.top = JQuery.get(headerView.getElement()).height() + "px";
            });
            Set<Long> lanesToRemove = new HashSet<>(laneMap.keySet());
            scale = getScaleFor(view);

            dateTimeHeader.generateTicks(gridObjectPlacer, scale, 0L,
                                         getDateHeaderFunction(),
                                         getTimeHeaderFunction(),
                                         getDateHeaderAdditionalClassesFunction());

            Map<Long, String> viewLanes = getLaneTitlesFor(view);
            for (Long laneId : viewLanes.keySet()) {
                if (!laneMap.containsKey(laneId)) {
                    Lane<LocalDateTime, M> lane = laneInstance.get().withGridObjectPlacer(gridObjectPlacer)
                                                              .withScale(scale).withTitle(viewLanes.get(laneId)).withGridObjectCreator(getInstanceCreator(view, laneId));
                    laneMap.put(laneId, lane);
                    laneContainer.appendChild(lane.getElement());
                } else {
                    laneMap.get(laneId).withScale(scale);
                    lanesToRemove.remove(laneId);
                }
            }
            lanesToRemove.forEach((id) -> {
                Lane<LocalDateTime, M> toRemove = laneMap.remove(id);
                toRemove.getElement().remove();
                laneInstance.destroy(toRemove);
            });

            final M metadata = getMetadata();
            laneMap.forEach((l, lane) -> {
                lane.setMetadata(metadata);
                lane.startModifying();
            });
            Scheduler.get().scheduleIncremental(getViewportBuilderCommand(view, lockableLaneMap));
            return promiseUtils.resolve();
        });
        
    }

    public <X> Map<Long, X> getIdMapFor(Collection<X> collection, Function<X, Long> idMapper) {
        return collection.stream().collect(Collectors.toMap((o) -> idMapper.apply(o), (o) -> o));
    }

    protected Lockable<Map<Long, Lane<LocalDateTime, M>>> getLockableLaneMap() {
        return lockableLaneMap;
    }
}
