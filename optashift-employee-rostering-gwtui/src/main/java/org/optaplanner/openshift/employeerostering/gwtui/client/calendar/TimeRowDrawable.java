package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;

import elemental2.dom.CanvasRenderingContext2D;

public interface TimeRowDrawable extends Drawable{
    public void doDrawAt(CanvasRenderingContext2D g, double x, double y); 
    int getIndex();
    String getGroupId();
    LocalDateTime getStartTime();
    LocalDateTime getEndTime();
}
