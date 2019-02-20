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

import elemental2.dom.HTMLElement;
import org.optaweb.employeerostering.gwtui.client.viewport.CSSGlobalStyle;

public enum GridObjectPlacer {
    HORIZONTAL {

        @Override
        public <T> void setStartPositionInScaleUnits(GridObject<T, ?> obj, LinearScale<T> scale, T positionInScaleUnits, boolean snapToGrid) {
            double positionInGridUnits = scale.toGridUnits(positionInScaleUnits);
            Long positionSnapToGrid = (long) Math.floor(positionInGridUnits);
            setStartPositionInGridUnits(obj.getElement(), scale, positionInGridUnits, snapToGrid);
            if (snapToGrid) {
                obj.setStartPositionInScaleUnits(scale.toScaleUnits(positionSnapToGrid));
            } else {
                obj.setStartPositionInScaleUnits(positionInScaleUnits);
            }
            hideIfHidden(obj, scale);
        }

        @Override
        public <T> void setEndPositionInScaleUnits(GridObject<T, ?> obj, LinearScale<T> scale, T positionInScaleUnits, boolean snapToGrid) {
            double positionInGridUnits = scale.toGridUnits(positionInScaleUnits);
            Long positionSnapToGrid = (long) Math.floor(positionInGridUnits);
            setEndPositionInGridUnits(obj.getElement(), scale, positionInGridUnits, snapToGrid);
            if (snapToGrid) {
                obj.setEndPositionInScaleUnits(scale.toScaleUnits(positionSnapToGrid));
            } else {
                obj.setEndPositionInScaleUnits(positionInScaleUnits);
            }
            hideIfHidden(obj, scale);
        }

        @Override
        public void setStartPositionInGridUnits(HTMLElement element, LinearScale<?> scale, double position, boolean snapToGrid) {
            Long positionSnapToGrid = (long) Math.floor(position);
            double remainder = position - positionSnapToGrid;
            element.style.set("grid-column-start", clamp(position, scale).toString());
            if (!snapToGrid) {
                element.style.set("margin-left", "calc(" + remainder + "*" + GRID_UNIT_SIZE + ")");
            }
        }

        @Override
        public void setEndPositionInGridUnits(HTMLElement element, LinearScale<?> scale, double position, boolean snapToGrid) {
            Long positionSnapToGrid = (long) Math.floor(position);
            double remainder = position - positionSnapToGrid;
            element.style.set("grid-column-end", clamp(position, scale).toString());
            if (!snapToGrid) {
                element.style.set("margin-right", "calc(" + remainder + "*" + GRID_UNIT_SIZE + ")");
            }
        }

        @Override
        public Long getStartPositionInGridUnits(HTMLElement element) {
            String start = element.style.get("grid-column-start");
            return (start != null && !start.isEmpty()) ? Long.parseLong(start) : -1L;
        }

        @Override
        public Long getEndPositionInGridUnits(HTMLElement element) {
            String end = element.style.get("grid-column-end");
            return (end != null && !end.isEmpty()) ? Long.parseLong(end) : -1L;
        }

    };

    public static <T> void hideIfHidden(GridObject<T, ?> obj, LinearScale<T> scale) {
        if (isHidden(scale.toGridUnits(obj.getStartPositionInScaleUnits()),
                     scale.toGridUnits(obj.getEndPositionInScaleUnits()),
                     scale)) {
            obj.getElement().classList.add("hidden");
        } else {
            obj.getElement().classList.remove("hidden");
        }
    }

    public static boolean isHidden(double gridStart, double gridEnd, LinearScale<?> scale) {
        return gridStart > scale.getEndInGridPixels() || gridEnd < scale.getStartInGridPixels();
    }

    private static Long clamp(double position, LinearScale<?> scale) {
        if (position > scale.getEndInGridPixels()) {
            return (long) Math.floor(scale.getEndInGridPixels() - scale.getStartInGridPixels() + 1);
        } else if (position < scale.getStartInGridPixels()) {
            return 1L;
        } else {
            return (long) Math.floor(position + 1);
        }
    }

    final static String GRID_UNIT_SIZE = CSSGlobalStyle.GridVariables.GRID_UNIT_SIZE.get();

    public abstract Long getStartPositionInGridUnits(HTMLElement element);

    public abstract Long getEndPositionInGridUnits(HTMLElement element);

    public abstract void setStartPositionInGridUnits(HTMLElement element, LinearScale<?> scale, double position, boolean snapToGrid);

    public abstract void setEndPositionInGridUnits(HTMLElement element, LinearScale<?> scale, double position, boolean snapToGrid);

    /**
     * Does not change end position; Modifies obj data
     * 
     * @param obj
     * @param scale
     * @param positionInScaleUnits
     * @param snapToGrid
     */
    public abstract <T> void setStartPositionInScaleUnits(GridObject<T, ?> obj, LinearScale<T> scale, T positionInScaleUnits, boolean snapToGrid);

    /**
     * Does not change start position; Modifies obj data
     * @param obj
     * @param scale
     * @param positionInScaleUnits
     * @param snapToGrid
     */
    public abstract <T> void setEndPositionInScaleUnits(GridObject<T, ?> obj, LinearScale<T> scale, T positionInScaleUnits, boolean snapToGrid);

    /**
     * Changes both start and end position; Modifies obj data
     */
    public <T> void setPositionInScaleUnits(GridObject<T, ?> obj, LinearScale<T> scale, T positionInScaleUnits, boolean snapToGrid) {
        T newEndPosition = scale.toScaleUnits(scale.toGridUnits(positionInScaleUnits) + scale.toGridUnits(obj.getEndPositionInScaleUnits()) - scale.toGridUnits(obj.getStartPositionInScaleUnits()));
        setStartPositionInScaleUnits(obj, scale, positionInScaleUnits, snapToGrid);
        setEndPositionInScaleUnits(obj, scale, newEndPosition, snapToGrid);
    }

    /**
     * Positions obj on grid; does not modify obj data; does not snap to grid
     * @param obj
     * @param scale
     */
    public <T> void positionObjectOnGrid(GridObject<T, ?> obj, LinearScale<T> scale) {
        setStartPositionInGridUnits(obj.getElement(), scale, scale.toGridUnits(obj.getStartPositionInScaleUnits()), false);
        setEndPositionInGridUnits(obj.getElement(), scale, scale.toGridUnits(obj.getEndPositionInScaleUnits()), false);
        hideIfHidden(obj, scale);
    }
}
