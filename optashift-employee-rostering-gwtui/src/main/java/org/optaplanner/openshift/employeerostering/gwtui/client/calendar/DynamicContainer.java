package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import elemental2.dom.CanvasRenderingContext2D;

/**
 * Dummy Drawable to be used as an Anchor for other Drawables, but is
 * positioned dynamically.
 */
public class DynamicContainer extends AbstractDrawable {

    /**
     * Controls the position of the {@link DynamicContainer}.
     */
    Positionable pos;

    public DynamicContainer(Positionable pos) {
        this.pos = pos;
    }

    @Override
    public double getLocalX() {
        return pos.getPosition().getX();
    }

    @Override
    public double getLocalY() {
        return pos.getPosition().getY();
    }

    public void setPositionable(Positionable pos) {
        this.pos = pos;
    }

    @Override
    public void doDraw(CanvasRenderingContext2D g) {}

    /**
     * Controls the position of the {@link DynamicContainer}.
     */
    public interface Positionable {

        /**
         * Position the {@link DynamicContainer} should be.
         * @return The position.
         */
        Position getPosition();
    }
}
