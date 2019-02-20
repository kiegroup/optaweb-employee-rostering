/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

import java.util.function.Consumer;

import elemental2.dom.Event;
import elemental2.dom.MouseEvent;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.GridObject;

public class ClickableDecorator<T, M> {

    private Consumer<MouseEvent> onClick;

    private double startX;
    private double startY;
    private double dx;
    private double dy;

    private final double MOVE_TOLERANCE = 20;

    public ClickableDecorator<T, M> applyFor(GridObject<T, M> gridObject) {
        gridObject.getElement().addEventListener("mousedown", this::gridObjectMouseDownListener);
        gridObject.getElement().addEventListener("mousemove", this::gridObjectMouseMoveListener);
        gridObject.getElement().addEventListener("click", this::gridObjectMouseClickListener);
        return this;
    }

    public ClickableDecorator<T, M> onClick(Consumer<MouseEvent> onClick) {
        this.onClick = onClick;
        return this;
    }

    public void gridObjectMouseDownListener(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        startX = mouseEvent.screenX;
        startY = mouseEvent.screenY;
        dx = 0;
        dy = 0;
    }

    public void gridObjectMouseMoveListener(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        dx += Math.abs(startX - mouseEvent.screenX);
        dy += Math.abs(startY - mouseEvent.screenY);
        startX = mouseEvent.screenX;
        startY = mouseEvent.screenY;
    }

    public void gridObjectMouseClickListener(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        if (dx + dy <= MOVE_TOLERANCE) {
            onClick.accept(mouseEvent);
        }
    }
}
