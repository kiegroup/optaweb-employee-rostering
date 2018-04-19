package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.impl;

import java.time.LocalDateTime;

import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.GridObject;
import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.Lane;
import org.optaplanner.openshift.employeerostering.shared.common.HasTimeslot;

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
