package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.MouseEvent;

public interface TimeRowDrawable extends Drawable{
    void doDrawAt(CanvasRenderingContext2D g, double x, double y);
    int getIndex();
    String getGroupId();
    LocalDateTime getStartTime();
    LocalDateTime getEndTime();
}
