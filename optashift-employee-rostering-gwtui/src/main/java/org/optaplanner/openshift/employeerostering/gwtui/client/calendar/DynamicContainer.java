package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import elemental2.dom.CanvasRenderingContext2D;

public class DynamicContainer extends AbstractDrawable {

    Positionable pos;

    public DynamicContainer(Positionable pos) {
        this.pos = pos;
    }

    public double getLocalX() {
        return pos.getPosition().getX();
    }

    public double getLocalY() {
        return pos.getPosition().getY();
    }

    public void setPositionable(Positionable pos) {
        this.pos = pos;
    }

    @Override
    public void doDraw(CanvasRenderingContext2D g) {
    }

    public interface Positionable {

        Position getPosition();
    }
}