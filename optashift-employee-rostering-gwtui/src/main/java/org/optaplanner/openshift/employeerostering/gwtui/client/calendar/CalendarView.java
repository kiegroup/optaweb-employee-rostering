package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.util.Collection;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.MouseEvent;

public interface CalendarView {
    void setShifts(Collection<ShiftData> shifts);
    void setTenantId(Integer id);
    void draw(CanvasRenderingContext2D g, double screenWidth, double screenHeight);
    void onMouseDown(MouseEvent e);
    void onMouseMove(MouseEvent e);
    void onMouseUp(MouseEvent e);
}
