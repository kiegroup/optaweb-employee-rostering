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
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;

public class Calendar {
    HTMLCanvasElement canvas;
    CalendarView view;
    Collection<ShiftData> shifts;
    Integer tenantId;
    
    public Calendar(HTMLCanvasElement canvasElement, Integer tenantId) {
        this.canvas = canvasElement;
        this.tenantId = tenantId;
        
        double width = Window.getClientWidth() - canvasElement.offsetLeft - 100;
        double height = Window.getClientHeight() - canvasElement.offsetTop - 100;
        
        canvas.width = width;
        canvas.height = height;
        
        Window.addResizeHandler((e) -> {
            canvas.width = e.getWidth() - canvasElement.offsetLeft - 100;
            canvas.height = e.getHeight() - canvasElement.offsetTop - 100;
            draw();
        });
        canvas.draggable = false;
        canvas.style.background = "#FFFFFF";
        
        canvas.onmousedown = (e) -> {onMouseDown((MouseEvent) e); return e;};
        canvas.onmousemove = (e) -> {onMouseMove((MouseEvent) e); return e;};
        canvas.onmouseup = (e) -> {onMouseUp((MouseEvent) e); return e;};
        
        shifts = new ArrayList<ShiftData>();
        
        view = new TwoDayView(this);
        
        draw();
    }
    
    private CalendarView getView() {
        return view;
    }
    
    public Integer getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }
    
    public void draw() {
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
                CanvasRenderingContext2D g = (CanvasRenderingContext2D) (Object) canvas.getContext("2d");            
                view.draw(g, canvas.width, canvas.height);
            }
        });
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
    }
}
