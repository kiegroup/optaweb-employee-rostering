package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import elemental2.dom.HTMLElement;
import elemental2.dom.MouseEvent;
import elemental2.promise.Promise;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;

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

    private LinearScale<T> scale;
    private GridObjectPlacer gridObjectPlacer;
    private Function<T, HasGridObjects<T, M>> gridObjectCreator;
    private M metadata;

    private Map<String, Collection<HasGridObjects<T, M>>> addedGridObjects;
    private Map<String, Map<Long, HasGridObjects<T, M>>> gridObjectMap;
    private Set<HasGridObjects<T, M>> notUpdatedGridObjects;

    private boolean mouseMoved = false;
    private boolean isLocked = false;

    @PostConstruct
    private void init() {
        gridObjectMap = new HashMap<>();
        addedGridObjects = new HashMap<>();
        notUpdatedGridObjects = new HashSet<>();
    }

    public Lane<T, M> withTitle(String title) {
        laneTitleLabel.innerHTML = new SafeHtmlBuilder().appendEscaped(title).toSafeHtml().asString();
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
        laneContent.insertBefore(gridObject.getElement(), dummy);
        gridObject.withLane(this);
        positionGridObject(gridObject);
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
        out.addAll(((Map<Long, Y>) gridObjectMap.get(clazz.getName())).values());
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

    public void removeGridObjectElement(GridObject<T, M> gridObject) {
        laneContent.removeChild(gridObject.getElement());
    }

    public GridObjectPlacer getGridObjectPlacer() {
        return gridObjectPlacer;
    }

    public void positionGridObject(GridObject<T, M> gridObject) {
        gridObjectPlacer.positionObjectOnGrid(gridObject, scale);
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
            T positionInScaleUnits = scale.toScaleUnits(Math.round(scale.toGridUnitsFromScreenPixels(e.offsetX)));
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
}
