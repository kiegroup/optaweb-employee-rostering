package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.util.Collection;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;

public interface CalendarView {
    void draw(Context2d g, int screenWidth, int screenHeight, Collection<Calendar.ShiftData> shifts);
    void onMouseDown(MouseDownEvent e);
    void onMouseMove(MouseMoveEvent e);
    void onMouseUp(MouseUpEvent e);
}
