package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.util.Collection;
import java.util.List;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.MouseEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;

public interface CalendarView<I extends HasTimeslot> {
    void setShifts(Collection<I> shifts);
    void setGroups(List<String> groups);
    void draw(CanvasRenderingContext2D g, double screenWidth, double screenHeight);
    void onMouseDown(MouseEvent e);
    void onMouseMove(MouseEvent e);
    void onMouseUp(MouseEvent e);
}
