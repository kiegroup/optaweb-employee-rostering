package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.MouseEvent;

public interface Drawable {

    public void draw(CanvasRenderingContext2D g);

    public Drawable getParent();

    public void setParent(Drawable parent);

    public double getLocalX();

    public double getLocalY();

    public double getGlobalX();

    public double getGlobalY();

    //These events return true if the event is consumed
    public boolean onMouseEnter(MouseEvent e, double x, double y);

    public boolean onMouseExit(MouseEvent e, double x, double y);

    public boolean onMouseDrag(MouseEvent e, double x, double y);

    public boolean onMouseMove(MouseEvent e, double x, double y);

    public PostMouseDownEvent onMouseDown(MouseEvent e, double x, double y);

    public boolean onMouseUp(MouseEvent e, double x, double y);

    public enum PostMouseDownEvent {
        REMOVE_FOCUS,
        CAPTURE_DRAG,
        IGNORE
    }
}
