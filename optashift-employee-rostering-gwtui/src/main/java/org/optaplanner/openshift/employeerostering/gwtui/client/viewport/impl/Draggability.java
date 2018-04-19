/*
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.impl;

import elemental2.dom.Event;
import elemental2.dom.HTMLElement;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.JQuery;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.GridObject;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.LinearScale;

public class Draggability<T, M> {

    private GridObject<T, M> gridObject;
    private LinearScale<T> scale;
    private double dragStart;

    public void applyFor(final GridObject<T, M> gridObject, final LinearScale<T> scale) {
        this.gridObject = gridObject;
        this.scale = scale;

        makeDraggable(gridObject.getElement(),
                      scale.getScreenPixelsPerGridUnit(),
                      "x");
    }

    private void makeDraggable(final HTMLElement blob,
                               final int pixelSize,
                               final String orientation) {
        JQueryDraggabilityOptions options = new JQueryDraggabilityOptions();
        options.addClasses = false;
        options.cancel = ".blob div";
        options.axis = orientation;
        options.grid = new int[]{pixelSize, pixelSize};
        options.start = (e, ui) -> onDragStart(ui.position.top, ui.position.left);
        options.stop = (e, ui) -> onDragEnd(ui.position.top, ui.position.left);
        options.drag = (e, ui) -> onDrag(ui.position.top, ui.position.left);
        options.scroll = false;
        JQueryDraggability.get(blob).draggable(options);
    }

    private boolean onDragStart(final int top, final int left) {
        dragStart = scale.toGridUnits(gridObject.getStartPositionInScaleUnits());
        return true;
    }

    private boolean onDragEnd(final int top, final int left) {
        gridObject.getLane().positionGridObject(gridObject);
        gridObject.getElement().style.removeProperty("left");
        gridObject.save();
        return true;
    }

    private boolean onDrag(final int top, final int left) {
        double startPositionInGridUnits = scale.toGridUnitsFromScreenPixels(left) + dragStart;
        double endPositionInGridUnits = scale.toGridUnits(gridObject.getEndPositionInScaleUnits()) - scale.toGridUnits(gridObject.getStartPositionInScaleUnits()) + startPositionInGridUnits;

        gridObject.setStartPositionInScaleUnits(scale.toScaleUnits(startPositionInGridUnits));
        gridObject.setEndPositionInScaleUnits(scale.toScaleUnits(endPositionInGridUnits));
        return true;
    }

    @JsType(isNative = true, name = "JQuery", namespace = JsPackage.GLOBAL)
    private static class JQueryDraggability extends JQuery {

        @JsMethod(name = "$", namespace = JsPackage.GLOBAL)
        public static native JQueryDraggability get(HTMLElement element);

        public native void draggable(JQueryDraggabilityOptions options);
    }

    @JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
    private static class JQueryDraggabilityOptions {

        public boolean addClasses;
        public String cancel;
        public String axis;
        public int[] grid;
        public boolean scroll;
        public JQueryDraggabilityFunction start;
        public JQueryDraggabilityFunction stop;
        public JQueryDraggabilityFunction drag;
    }

    @JsType(isNative = true, name = "UIEvent", namespace = JsPackage.GLOBAL)
    private static class UIEvent {

        public Position position;
    }

    @JsType(isNative = true, name = "Object", namespace = JsPackage.GLOBAL)
    private static class Position {

        public int top;
        public int left;
    }

    @JsFunction
    private interface JQueryDraggabilityFunction {

        void onEvent(Event event, UIEvent uiEvent);
    }

}
