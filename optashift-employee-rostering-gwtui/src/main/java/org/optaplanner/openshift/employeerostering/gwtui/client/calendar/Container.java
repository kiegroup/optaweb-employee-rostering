package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import elemental2.dom.CanvasRenderingContext2D;

/**
 * Dummy Drawable that is invisible and to be use as an anchor for
 * other Drawables.
 */
public class Container extends AbstractDrawable {

    final double x, y;

    public Container(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public double getLocalX() {
        return x;
    }

    @Override
    public double getLocalY() {
        return y;
    }

    @Override
    public void doDraw(CanvasRenderingContext2D g) {}
}
