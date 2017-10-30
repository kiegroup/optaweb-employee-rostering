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
    public void onMouseMove(MouseEvent e, double x, double y);
    public void onMouseDown(MouseEvent e, double x, double y);
    public void onMouseUp(MouseEvent e, double x, double y);
}
