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

package org.optaweb.employeerostering.gwtui.client.viewport.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaweb.employeerostering.gwtui.client.common.JQuery;
import org.optaweb.employeerostering.gwtui.client.viewport.CSSGlobalStyle;

@Templated
public class Lane<T, M> implements IsElement {

    @Inject
    @DataField("lane-title")
    @Named("span")
    private HTMLElement laneTitleLabel;

    @Inject
    @DataField("lane-content")
    @Named("span")
    private HTMLElement laneContent;

    @Inject
    @DataField("dummy")
    @Named("span")
    private HTMLElement dummy;

    @Inject
    private CSSGlobalStyle cssGlobalStyle;

    private LinearScale<T> scale;
    private GridObjectPlacer gridObjectPlacer;
    private Function<T, HasGridObjects<T, M>> gridObjectCreator;
    private M metadata;

    private Map<String, Collection<HasGridObjects<T, M>>> addedGridObjects;
    private Map<String, Map<Long, HasGridObjects<T, M>>> gridObjectMap;
    private Set<HasGridObjects<T, M>> notUpdatedGridObjects;

    private boolean mouseMoved = false;
    private DummySublane dummySublane;
    private long rowCount;

    @PostConstruct
    private void init() {
        gridObjectMap = new HashMap<>();
        addedGridObjects = new HashMap<>();
        notUpdatedGridObjects = new HashSet<>();
        rowCount = 1;
    }

    public Lane<T, M> withTitle(String title) {
        laneTitleLabel.innerHTML = new SafeHtmlBuilder().appendEscaped(title).toSafeHtml().asString();
        return this;
    }

    public Lane<T, M> withDummySublane(DummySublane dummySublane) {
        this.dummySublane = dummySublane;
        if (dummySublane == DummySublane.NONE) {
            dummy.hidden = true;
        } else {
            dummy.hidden = false;
        }
        return this;
    }

    /**
     * withGridObjectPlacer MUST be called before this
     * @param scale
     * @return
     */
    public Lane<T, M> withScale(LinearScale<T> scale) {
        this.scale = scale;
        makeSpanning(dummy);
        return this;
    }

    public Lane<T, M> withGridObjectPlacer(GridObjectPlacer gridObjectPlacer) {
        this.gridObjectPlacer = gridObjectPlacer;
        return this;
    }

    public Lane<T, M> withGridObjectCreator(Function<T, HasGridObjects<T, M>> creator) {
        this.gridObjectCreator = creator;
        return this;
    }

    public void addGridObject(HasGridObjects<T, M> gridObjectsToAdd) {
        for (GridObject<T, M> gridObject : gridObjectsToAdd.getGridObjects()) {
            addGridObjectElement(gridObject);
        }
        if (gridObjectsToAdd.getId() != null) {
            gridObjectMap.computeIfAbsent(gridObjectsToAdd.getClass().getName(),
                                          (k) -> new HashMap<>()).put(gridObjectsToAdd.getId(), gridObjectsToAdd);
        } else {
            addedGridObjects.computeIfAbsent(gridObjectsToAdd.getClass().getName(),
                                             (k) -> new ArrayList<>()).add(gridObjectsToAdd);
        }

    }

    public void addGridObjectElement(GridObject<T, M> gridObject) {
        switch (dummySublane) {
            case BOTTOM:
                laneContent.insertBefore(gridObject.getElement(), dummy);
                break;
            case TOP:
            case NONE:
                laneContent.appendChild(gridObject.getElement());
                break;
            default:
                throw new IllegalStateException("No case for " + dummySublane + " in addGridObjectElement.");
        }

        gridObject.withLane(this);
        positionGridObject(gridObject);
        refreshSpanningElements();
    }

    public void moveAddedGridObjectToIdMap(HasGridObjects<T, M> addedGridObject) {
        addedGridObjects.get(addedGridObject.getClass().getName()).remove(addedGridObject);
        gridObjectMap.computeIfAbsent(addedGridObject.getClass().getName(),
                                      (k) -> new HashMap<>()).put(addedGridObject.getId(), addedGridObject);
    }

    @SuppressWarnings("unchecked")
    /**
     * Throws NPE if there is no object with id, use "addOrUpdateGridObject" in grid object may not exist
     * @param clazz
     * @param id
     * @param updater
     */
    public <Y extends HasGridObjects<T, M>> void updateGridObject(Class<? extends Y> clazz, Long id, Function<Y, Void> updater) {
        Y gridObject = (Y) gridObjectMap.get(clazz.getName()).get(id);
        notUpdatedGridObjects.remove(gridObject);
        updater.apply(gridObject);
    }

    @SuppressWarnings("unchecked")
    // TODO: Should existing and new grid objects be received via different methods?
    public <Y extends HasGridObjects<T, M>> Collection<Y> getGridObjects(Class<? extends Y> clazz) {
        List<Y> out = new ArrayList<>();
        out.addAll(((Map<Long, Y>) gridObjectMap.getOrDefault(clazz.getName(), Collections.emptyMap())).values());
        out.addAll((Collection<Y>) addedGridObjects.getOrDefault(clazz.getName(), Collections.emptyList()));
        return out;
    }

    public void removeGridObject(HasGridObjects<T, M> gridObjects) {
        if (gridObjects.getId() != null) {
            gridObjectMap.get(gridObjects.getClass().getName()).remove(gridObjects.getId());
        } else {
            addedGridObjects.get(gridObjects.getClass().getName()).remove(gridObjects);
        }
        for (GridObject<T, M> gridObject : gridObjects.getGridObjects()) {
            removeGridObjectElement(gridObject);
        }
        notUpdatedGridObjects.remove(gridObjects);
    }

    /**
     * Removes grid objects of type clazz that does not match predicate
     */
    public <Y extends HasGridObjects<T, M>> void filterGridObjects(Class<Y> clazz, Predicate<Y> predicate) {
        Iterator<Entry<Long, HasGridObjects<T, M>>> gridObjectIterator = gridObjectMap.getOrDefault(clazz.getName(), Collections.emptyMap()).entrySet().iterator();
        while (gridObjectIterator.hasNext()) {
            @SuppressWarnings("unchecked")
            Y gridObject = (Y) gridObjectIterator.next().getValue();
            if (!predicate.test(gridObject)) {
                gridObjectIterator.remove();
                for (GridObject<T, M> element : gridObject.getGridObjects()) {
                    removeGridObjectElement(element);
                }
                notUpdatedGridObjects.remove(gridObject);
            }
        }
    }

    public void removeGridObjectElement(GridObject<T, M> gridObject) {
        laneContent.removeChild(gridObject.getElement());
        refreshSpanningElements();
    }

    private void refreshSpanningElements() {
        rowCount = Math.round(JQuery.get(getElement()).height() / cssGlobalStyle.getGridVariableValue(CSSGlobalStyle.GridVariables.GRID_ROW_SIZE).doubleValue());
        JQuery.get(getElement()).children(".spanning-blob").css("grid-row-end", rowCount + "");
    }

    public GridObjectPlacer getGridObjectPlacer() {
        return gridObjectPlacer;
    }

    public void positionGridObject(GridObject<T, M> gridObject) {
        gridObjectPlacer.positionObjectOnGrid(gridObject, scale);
        refreshSpanningElements();
    }

    public void makeSpanning(HTMLElement element) {
        gridObjectPlacer.setStartPositionInGridUnits(element, scale, scale.getStartInGridPixels(), false);
        gridObjectPlacer.setEndPositionInGridUnits(element, scale, scale.getEndInGridPixels(), false);
    }

    public LinearScale<T> getScale() {
        return scale;
    }

    public <Y extends HasGridObjects<T, M>> void addOrUpdateGridObject(Class<? extends Y> clazz,
                                                                       Long id,
                                                                       Supplier<Y> gridObjectSupplier,
                                                                       Function<Y, Void> updater) {
        if (gridObjectMap.getOrDefault(clazz.getName(),
                                       Collections.emptyMap()).containsKey(id)) {
            updateGridObject(clazz, id, updater);
        } else {
            addGridObject(gridObjectSupplier.get());
        }
    }

    @EventHandler("lane-content")
    private void onMouseDown(@ForEvent("mousedown") MouseEvent e) {
        mouseMoved = false;
    }

    @EventHandler("lane-content")
    private void onMouseMove(@ForEvent("mousemove") MouseEvent e) {
        mouseMoved = true;
    }

    @EventHandler("lane-content")
    private void onMouseUp(@ForEvent("mouseup") MouseEvent e) {
        if (!mouseMoved) {
            onMouseClick(e);
        }
    }

    protected void onMouseClick(MouseEvent e) {
        if (e.target == laneContent) {
            T positionInScaleUnits = scale.toScaleUnits(Math.round(cssGlobalStyle.toGridUnits(e.offsetX)));
            addGridObject(gridObjectCreator.apply(positionInScaleUnits));
        }
    }

    public void startModifying() {
        List<HasGridObjects<T, M>> toRemove = new ArrayList<>();
        addedGridObjects.forEach((k, v) -> toRemove.addAll(v));
        toRemove.forEach(gobj -> removeGridObject(gobj));
        gridObjectMap.values().forEach(gobjs -> notUpdatedGridObjects.addAll(gobjs.values()));
    }

    public void endModifying() {
        List<HasGridObjects<T, M>> toRemove = new ArrayList<>(notUpdatedGridObjects);
        toRemove.forEach(gobj -> removeGridObject(gobj));
    }

    public M getMetadata() {
        return metadata;
    }

    public void setMetadata(M metadata) {
        this.metadata = metadata;
    }

    public static enum DummySublane {
        TOP,
        BOTTOM,
        NONE;
    }
}
