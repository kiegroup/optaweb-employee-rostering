package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;


public final class Position {
    final double x, y;
    
    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
}
