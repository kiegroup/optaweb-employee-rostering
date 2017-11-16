package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.HTMLCanvasElement;
import elemental2.dom.MouseEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Panel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.DataProvider;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.Fetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;

public class Calendar<I extends HasTimeslot> {
    HTMLCanvasElement canvas;
    CalendarView<I> view;
    Collection<I> shifts;
    Integer tenantId;
    Panel topPanel;
    Panel bottomPanel;
    Panel sidePanel;
    Fetchable<Collection<I>> dataProvider;
    Fetchable<List<String>> groupProvider;
    DataProvider<I> instanceCreator;
    
    private Calendar(HTMLCanvasElement canvasElement, Integer tenantId, Panel topPanel, Panel bottomPanel, Panel sidePanel,
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
        
        groupProvider.setUpdatable((groups) -> getView().setGroups(groups));
        dataProvider.setUpdatable((d) -> {shifts = new ArrayList<>(d); getView().setShifts(shifts);});
        
        refresh();
        
        Window.addResizeHandler((e) -> {
            canvas.width = e.getWidth() - canvasElement.offsetLeft - sidePanel.getOffsetWidth() - 100;
            canvas.height = e.getHeight() - canvasElement.offsetTop - topPanel.getOffsetHeight() - bottomPanel.getOffsetHeight() - 100;
            draw();
        });
    }
    
    private void setView(CalendarView<I> view) {
        this.view = view;
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
    
    public void forceUpdate() {
        groupProvider.fetchData(() -> dataProvider.fetchData(() -> draw()));
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
    
    public void setDate(LocalDateTime date) {
        view.setDate(date);
    }
    
    public void addShift(String groupId, LocalDateTime start, LocalDateTime end) {
        I shift = instanceCreator.getInstance(groupId, start, end);
        shifts.add(shift);
        getView().setShifts(shifts);
        draw();
    }
    
    public static class Builder<T extends HasTimeslot, D extends TimeRowDrawable> {
        HTMLCanvasElement canvas;
        Collection<T> shifts;
        Integer tenantId;
        Panel topPanel;
        Panel bottomPanel;
        Panel sidePanel;
        LocalDateTime startAt;
        Fetchable<Collection<T>> dataProvider;
        Fetchable<List<String>> groupProvider;
        DataProvider<T> instanceCreator;
        
        public Builder(HTMLCanvasElement canvas, Integer tenantId) {
            this.canvas = canvas;
            this.tenantId = tenantId;
            
            topPanel = null;
            bottomPanel = null;
            sidePanel = null;
            dataProvider = null;
            groupProvider = null;
            instanceCreator = null;
            startAt = null;
        }
        
        public Builder<T,D> withTopPanel(Panel topPanel) {
            this.topPanel = topPanel;
            return this;
        }
        
        public Builder<T,D> withBottomPanel(Panel bottomPanel) {
            this.bottomPanel = bottomPanel;
            return this;
        }
        
        public Builder<T,D> withSidePanel(Panel sidePanel) {
            this.sidePanel = sidePanel;
            return this;
        }
        
        public Builder<T,D> fetchingDataFrom(Fetchable<Collection<T>> dataProvider) {
            this.dataProvider = dataProvider;
            return this;
        }
        
        public Builder<T,D> fetchingGroupsFrom(Fetchable<List<String>> groupProvider) {
            this.groupProvider = groupProvider;
            return this;
        }
        
        public Builder<T,D> creatingDataInstancesWith(DataProvider<T> instanceCreator) {
            this.instanceCreator = instanceCreator;
            return this;
        }
        
        public Builder<T,D> startingAt(LocalDateTime start) { 
            startAt = start;
            return this;
        }
        
        public Calendar<T> asTwoDayView(TimeRowDrawableProvider<T,D> drawableProvider) {
            if (null != topPanel && null != bottomPanel && null != sidePanel && null != dataProvider
                    && null != groupProvider && null != instanceCreator) {
            Calendar<T> calendar = new Calendar<>(canvas, tenantId,
                    topPanel, bottomPanel, sidePanel,
                    dataProvider, groupProvider, instanceCreator);
            TwoDayView<T,D> view = new TwoDayView<T,D>(calendar,topPanel,bottomPanel,sidePanel,drawableProvider);
            calendar.setView(view);
            
            if (null != startAt) {
                view.setDate(startAt);
            }
            return calendar;
            }
            else {
                throw new IllegalStateException("You must set all of " + 
            "(topPanel,bottomPanel,sidePanel,dataProvider,groupProvider,instanceProvider) before calling this method.");
            }
        }
        
    }
}
