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

package org.optaweb.employeerostering.gwtui.client.viewport.impl;

import java.time.LocalDateTime;

import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.GridObject;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaweb.employeerostering.shared.common.HasTimeslot;

public abstract class AbstractHasTimeslotGridObject<M> implements GridObject<LocalDateTime, M> {

    private Lane<LocalDateTime, M> lane;

    protected abstract HasTimeslot getTimeslot();

    protected abstract void init(final Lane<LocalDateTime, M> lane);

    private boolean mouseMoved = false;

    @Override
    public LocalDateTime getStartPositionInScaleUnits() {
        return HasTimeslot.EPOCH.plus(getTimeslot().getDurationBetweenReferenceAndStart());
    }

    @Override
    public LocalDateTime getEndPositionInScaleUnits() {
        return getStartPositionInScaleUnits().plus(getTimeslot().getDurationOfTimeslot());
    }

    @Override
    public void withLane(Lane<LocalDateTime, M> lane) {
        this.lane = lane;
        init(lane);
    }

    @Override
    public Lane<LocalDateTime, M> getLane() {
        return lane;
    }

    @EventHandler("root")
    private void onMouseDown(@ForEvent("mousedown") MouseEvent e) {
        mouseMoved = false;
    }

    @EventHandler("root")
    private void onMouseMove(@ForEvent("mousemove") MouseEvent e) {
        mouseMoved = true;
    }

    @EventHandler("root")
    private void onMouseUp(@ForEvent("mouseup") MouseEvent e) {
        if (!mouseMoved) {
            onMouseClick(e);
        }
    }

    protected void onMouseClick(MouseEvent e) {

    }
}
