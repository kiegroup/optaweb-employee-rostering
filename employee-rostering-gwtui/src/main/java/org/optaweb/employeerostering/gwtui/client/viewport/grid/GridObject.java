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
