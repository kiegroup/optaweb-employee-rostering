package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.MouseEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;

public class Calendar {
    HTMLCanvasElement canvas;
    CalendarView view;
    Collection<ShiftData> shifts;
    Integer tenantId;
    Div topPanel;
    Div bottomPanel;
    Span sidePanel;
    
    public Calendar(HTMLCanvasElement canvasElement, Integer tenantId, Div topPanel, Div bottomPanel, Span sidePanel) {
        this.canvas = canvasElement;
        this.tenantId = tenantId;
        this.topPanel = topPanel;
        this.bottomPanel = bottomPanel;
        this.sidePanel = sidePanel;
        
        canvas.draggable = false;
        canvas.style.background = "#FFFFFF";
        
        canvas.onmousedown = (e) -> {onMouseDown((MouseEvent) e); return e;};
        canvas.onmousemove = (e) -> {onMouseMove((MouseEvent) e); return e;};
        canvas.onmouseup = (e) -> {onMouseUp((MouseEvent) e); return e;};
        
        shifts = new ArrayList<ShiftData>();
        
        view = new TwoDayView(this,topPanel,bottomPanel,sidePanel);
        
        refresh();
        
        Window.addResizeHandler((e) -> {
            canvas.width = e.getWidth() - canvasElement.offsetLeft - sidePanel.getOffsetWidth() - 100;
            canvas.height = e.getHeight() - canvasElement.offsetTop - topPanel.getOffsetHeight() - bottomPanel.getOffsetHeight() - 100;
            draw();
        });
    }
    
    private CalendarView getView() {
        return view;
    }
    
    public Integer getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
        view.setTenantId(tenantId);
        ShiftRestServiceBuilder.getShifts(tenantId, new FailureShownRestCallback<List<Shift>>() {
            @Override
            public void onSuccess(List<Shift> theShifts) {
                shifts = new ArrayList<>();
                LocalDateTime min = theShifts.stream().min((a,b) -> a.getTimeSlot().getStartDateTime().compareTo(b.getTimeSlot().getStartDateTime())).get().getTimeSlot().getStartDateTime();
                for (Shift shift : theShifts) {
                    shifts.add(new ShiftData(shift.getTimeSlot().getStartDateTime().minusSeconds(min.toEpochSecond(ZoneOffset.UTC)),
                            shift.getTimeSlot().getEndDateTime().minusSeconds(min.toEpochSecond(ZoneOffset.UTC)),
                            Arrays.asList(shift.getSpot().toString())));
                }
                view.setShifts(shifts);
            }
        });
    }
    
    public void draw() {
        refresh();
        CanvasRenderingContext2D g = (CanvasRenderingContext2D) (Object) canvas.getContext("2d");            
        view.draw(g, canvas.width, canvas.height);
    }
    
    public void refresh() {
        double width = Window.getClientWidth() - canvas.offsetLeft - sidePanel.getOffsetWidth()  - 100;
        double height = Window.getClientHeight() - canvas.offsetTop - topPanel.getOffsetHeight() - bottomPanel.getOffsetHeight() - 100;
        
        canvas.width = width;
        canvas.height = height;
    }
    
    public void onMouseDown(MouseEvent e) {
        getView().onMouseDown(e);
    }
    
    public void onMouseMove(MouseEvent e) {
        getView().onMouseMove(e);
    }
    
    public void onMouseUp(MouseEvent e) {
        getView().onMouseUp(e);
    }
    
    public void addShift(ShiftData shift) {
        shifts.add(shift);
        getView().setShifts(shifts);
        draw();
    }
}
