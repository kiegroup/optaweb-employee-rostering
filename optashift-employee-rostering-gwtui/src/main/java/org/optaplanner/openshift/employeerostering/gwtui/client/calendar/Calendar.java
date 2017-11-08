package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.MouseEvent;
import com.google.gwt.user.client.Window;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.DataProvider;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Fetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;

public class Calendar<I extends HasTimeslot> {
    HTMLCanvasElement canvas;
    CalendarView<I> view;
    Collection<I> shifts;
    Integer tenantId;
    Div topPanel;
    Div bottomPanel;
    Span sidePanel;
    Fetchable<Collection<I>> dataProvider;
    Fetchable<List<String>> groupProvider;
    DataProvider<I> instanceCreator;
    
    public Calendar(HTMLCanvasElement canvasElement, Integer tenantId, Div topPanel, Div bottomPanel, Span sidePanel,
            Fetchable<Collection<I>> dataProvider, Fetchable<List<String>> groupProvider, DataProvider<I> instanceCreator) {
        this.canvas = canvasElement;
        this.tenantId = tenantId;
        this.topPanel = topPanel;
        this.bottomPanel = bottomPanel;
        this.sidePanel = sidePanel;
        this.groupProvider = groupProvider;
        this.dataProvider = dataProvider;
        this.instanceCreator = instanceCreator;
        
        canvas.draggable = false;
        canvas.style.background = "#FFFFFF";
        
        canvas.onmousedown = (e) -> {onMouseDown((MouseEvent) e); return e;};
        canvas.onmousemove = (e) -> {onMouseMove((MouseEvent) e); return e;};
        canvas.onmouseup = (e) -> {onMouseUp((MouseEvent) e); return e;};
        
        shifts = new ArrayList<>();
        
        view = new TwoDayView<I,ShiftDrawable>(this,topPanel,bottomPanel,sidePanel,
                (v,d,i) -> new ShiftDrawable(v, d.getGroupId(),d.getStartTime(), d.getEndTime(), "#000000", i));
        groupProvider.setUpdatable((groups) -> getView().setGroups(groups));
        dataProvider.setUpdatable((d) -> {shifts = d; getView().setShifts(d);});
        
        refresh();
        
        Window.addResizeHandler((e) -> {
            canvas.width = e.getWidth() - canvasElement.offsetLeft - sidePanel.getOffsetWidth() - 100;
            canvas.height = e.getHeight() - canvasElement.offsetTop - topPanel.getOffsetHeight() - bottomPanel.getOffsetHeight() - 100;
            draw();
        });
    }
    
    private CalendarView<I> getView() {
        return view;
    }
    
    public Integer getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
        groupProvider.fetchData(() -> dataProvider.fetchData(Fetchable.DO_NOTHING));
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
    
    public void addShift(String groupId, LocalDateTime start, LocalDateTime end) {
        I shift = instanceCreator.getInstance(groupId, start, end);
        shifts.add(shift);
        getView().setShifts(shifts);
        draw();
    }
}
