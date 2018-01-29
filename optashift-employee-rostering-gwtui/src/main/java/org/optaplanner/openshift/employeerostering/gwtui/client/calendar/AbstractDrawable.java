package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.MouseEvent;

/**
 * Implements most of {@link Drawable} methods, requiring only
 * {@link AbstractDrawable#doDraw}, {@link Drawable#getLocalX()} and
 * {@link Drawable#getLocalY()} to be defined. Defines
 * {@link Drawable#draw(CanvasRenderingContext2D)} to first shift the view
 * by its parent global position so it can just use its local position in
 * {@link AbstractDrawable#doDraw}.
 */
public abstract class AbstractDrawable implements Drawable {

    /**
     * Parent of the Drawable.
     */
    Drawable parent;

    /**
     * The draw methods for subclasses of this class
     * @param g Where to draw to.
     */
    public abstract void doDraw(CanvasRenderingContext2D g);

    @Override
    public void draw(CanvasRenderingContext2D g) {
        g.save();
        double tx, ty;
        if (null != parent) {
            tx = parent.getGlobalX();
            ty = parent.getGlobalY();
        } else {
            tx = 0;
            ty = 0;
        }
        g.translate(tx, ty);
        doDraw(g);
        g.restore();
    }

    @Override
    public double getGlobalX() {
        if (null != parent) {
            return parent.getGlobalX() + getLocalX();
        } else {
            return getLocalX();
        }
    }

    @Override
    public double getGlobalY() {
        if (null != parent) {
            return parent.getGlobalY() + getLocalY();
        } else {
            return getLocalY();
        }
    }

    @Override
    public Drawable getParent() {
        return this.parent;
    }

    @Override
    public void setParent(Drawable parent) {
        this.parent = parent;
    }

    @Override
    public boolean onMouseEnter(MouseEvent e, double x, double y) {
        return false;
    }

    @Override
    public boolean onMouseExit(MouseEvent e, double x, double y) {
        return false;
    }

    @Override
    public boolean onMouseMove(MouseEvent e, double x, double y) {
        return false;
    }

    @Override
    public boolean onMouseDrag(MouseEvent e, double x, double y) {
        return false;
    }

    @Override
    public PostMouseDownEvent onMouseDown(MouseEvent e, double x, double y) {
        return PostMouseDownEvent.IGNORE;
    }

    @Override
    public boolean onMouseUp(MouseEvent e, double x, double y) {
        return false;
    }
}
