package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import elemental2.dom.CanvasRenderingContext2D;

public class Container extends AbstractDrawable {
    double x, y;
    
    public Container(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getLocalX() {
        return x;
    }
    
    public double getLocalY() {
        return y;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public void doDraw(CanvasRenderingContext2D g) {    
    }
}