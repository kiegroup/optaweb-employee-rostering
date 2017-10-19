package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;

public class Calendar {
    Canvas canvas;
    CalendarView view;
    Collection<ShiftData> shifts;
    
    public Calendar(DivElement canvasElement) {
        this.canvas = Canvas.createIfSupported();
        FocusPanel focusPanel = new FocusPanel();
        int width = Window.getClientWidth() - canvasElement.getAbsoluteLeft() - 100;
        int height = Window.getClientHeight() - canvasElement.getAbsoluteTop() - 100;
        
        canvas.setWidth(width + "px");
        canvas.setCoordinateSpaceWidth(width);
         
        canvas.setHeight(height + "px");      
        canvas.setCoordinateSpaceHeight(height);
        
        canvas.getCanvasElement().getStyle().setBackgroundColor("#FFFFFF");
        canvas.getCanvasElement().setDraggable(Element.DRAGGABLE_FALSE);
        
        
        canvas.addMouseMoveHandler((e) -> getView().onMouseMove(e));
        canvas.addMouseUpHandler((e) -> getView().onMouseUp(e));
        
        focusPanel.add(canvas);
        
        
        shifts = new ArrayList<ShiftData>();
        
        view = new WeekView(this);
        
        canvasElement.appendChild(focusPanel.getElement());
        
        focusPanel.addDomHandler((e) -> {ErrorPopup.show("test");getView().onMouseDown(e);}, MouseDownEvent.getType());
        Event.sinkEvents(canvas.getCanvasElement(), Event.MOUSEEVENTS);
        draw();
    }
    
    private CalendarView getView() {
        return view;
    }
    
    public void draw() {
        Context2d g = canvas.getContext2d();
        
        int screenWidth = canvas.getCoordinateSpaceWidth();
        int screenHeight = canvas.getCoordinateSpaceHeight(); 
        
        view.draw(g, screenWidth, screenHeight, shifts);
    }
    
    public static class ShiftData {
        LocalDateTime start;
        LocalDateTime end;
        Collection<String> spots;
    }
}
