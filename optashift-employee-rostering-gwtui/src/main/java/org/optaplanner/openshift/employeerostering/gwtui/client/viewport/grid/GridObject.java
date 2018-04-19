package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid;

import org.jboss.errai.ui.client.local.api.elemental2.IsElement;

public interface GridObject<T, M> extends IsElement {

    T getStartPositionInScaleUnits();

    /**
     * This method only affects the backing data, not the actual position of the
     * element, which is handled in GridObjectPlacer
     * @param newPosition New start position
     */
    void setStartPositionInScaleUnits(T newStartPosition);

    T getEndPositionInScaleUnits();

    /**
     * This method only affects the backing data, not the actual position of the
     * element, which is handled in GridObjectPlacer
     * @param newPosition New start position
     */
    void setEndPositionInScaleUnits(T newEndPosition);

    void withLane(Lane<T, M> lane);

    Long getId();

    Lane<T, M> getLane();

    /**
     * Update the server version of this GridObject
     */
    void save();

    default void setClassProperty(String clazz, boolean isSet) {
        if (isSet) {
            getElement().classList.add(clazz);
        } else {
            getElement().classList.remove(clazz);
        }
    }
}
