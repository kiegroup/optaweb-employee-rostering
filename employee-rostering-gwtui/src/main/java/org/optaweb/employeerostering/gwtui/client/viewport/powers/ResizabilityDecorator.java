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

package org.optaweb.employeerostering.gwtui.client.viewport.powers;

import javax.inject.Inject;

import elemental2.dom.Event;
import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.optaweb.employeerostering.gwtui.client.common.JQuery;
import org.optaweb.employeerostering.gwtui.client.viewport.CSSGlobalStyle;
import org.optaweb.employeerostering.gwtui.client.viewport.CSSGlobalStyle.GridVariables;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.GridObject;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.LinearScale;

public class ResizabilityDecorator<T, M> {

    private GridObject<T, M> gridObject;
    private LinearScale<T> scale;
    private double start;

    @Inject
    private CSSGlobalStyle cssGlobalStyle;

    public void applyFor(final GridObject<T, M> gridObject, final LinearScale<T> scale) {

        this.gridObject = gridObject;
        this.scale = scale;

        makeResizable(gridObject.getElement(),
                      "e"); // TODO: Add support for "w" (jquery doesn't like snapping on west but like it on east
    }

    private void makeResizable(final HTMLElement blob,
                               final String orientation) {
        JQueryResizabilityOptions options = new JQueryResizabilityOptions();
        JQueryResizability resizableBlob = JQueryResizability.get(blob);
        options.handles = orientation;
        options.minHeight = 0;
        options.start = (e, ui) -> onResizeStart(cast(JQueryResiabilityData.class, resizableBlob.data("ui-resizable")).axis, ui.size.height, ui.size.width);
        options.stop = (e, ui) -> onResizeEnd(cast(JQueryResiabilityData.class, resizableBlob.data("ui-resizable")).axis, ui.size.height, ui.size.width);
        options.resize = (e, ui) -> {
            ui.size.width = snapToGrid(ui.size.width);
            onResize(cast(JQueryResiabilityData.class, resizableBlob.data("ui-resizable")).axis, ui.size.height, ui.size.width);
        };
        resizableBlob.resizable(options);
    }

    private int snapToGrid(double coordinate) {
        double pixelSize = cssGlobalStyle.getGridVariableValue(GridVariables.GRID_UNIT_SIZE).intValue();
        return (int) Math.floor(coordinate - (coordinate % pixelSize));
    }

    private boolean onResizeStart(final String orientation, final int height, final int width) {
        switch (orientation) {
            case "e":
                start = scale.toGridUnits(gridObject.getStartPositionInScaleUnits());
                break;

            case "w":
                start = scale.toGridUnits(gridObject.getEndPositionInScaleUnits());
        }

        return true;
    }

    private boolean onResizeEnd(final String orientation, final int height, final int width) {
        gridObject.getLane().positionGridObject(gridObject);
        gridObject.getElement().style.removeProperty("left");
        gridObject.getElement().style.removeProperty("top");
        gridObject.getElement().style.removeProperty("width");
        gridObject.getElement().style.removeProperty("height");
        gridObject.save();
        return true;
    }

    private boolean onResize(final String orientation, final int height, final int width) {
        double newGridPos;
        switch (orientation) {
            case "e":
                newGridPos = start + cssGlobalStyle.toGridUnits(width);
                gridObject.setEndPositionInScaleUnits(scale.toScaleUnits(newGridPos));
                break;

            case "w":
                newGridPos = start - cssGlobalStyle.toGridUnits(width);
                gridObject.setStartPositionInScaleUnits(scale.toScaleUnits(newGridPos));
                break;
        }
        return true;
    }

    private static class JQueryResizability extends JQuery {

        @JsMethod(name = "$", namespace = JsPackage.GLOBAL)
        public static native JQueryResizability get(HTMLElement element);

        @JsMethod
        public native void resizable(JQueryResizabilityOptions options);

        @JsMethod
        public native void resizable(String method);
    }

    @JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
    private static class JQueryResiabilityData {

        public String axis;
    }

    @JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
    @SuppressWarnings("unused")
    private static class JQueryResizabilityOptions {

        public int minHeight;
        public String handles;
        public int[] grid;
        public boolean scroll;
        public JQueryResizabilityFunction start;
        public JQueryResizabilityFunction stop;
        public JQueryResizabilityFunction resize;
    }

    @JsType(isNative = true, name = "UIEvent", namespace = JsPackage.GLOBAL)
    private static class UIEvent {

        public Size size;
    }

    @JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
    private static class Size {

        public int width;
        public int height;
    }

    @JsFunction
    private interface JQueryResizabilityFunction {

        void onEvent(Event event, UIEvent uiEvent);
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Class<? extends T> clazz, Object obj) {
        return (T) obj;
    }
}
