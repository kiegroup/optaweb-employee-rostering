package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.MouseEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.ColorUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;

public class ShiftDrawable extends AbstractDrawable implements TimeRowDrawable {
    String spot;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String color;
    int index;
    boolean isMouseOver;
    TwoDayView view;
    
    
    public ShiftDrawable(TwoDayView view, ShiftData data, int index) {
        this.view = view;
        this.startTime = data.getStartTime();
        this.endTime = data.getEndTime();
        this.spot = data.getGroupId();
        this.color = ColorUtils.getColor(view.getGroupIndex(data.getGroupId()));
        this.index = index;
        this.isMouseOver = false;
    }

    @Override
    public void doDraw(CanvasRenderingContext2D g) {
        CanvasUtils.setFillColor(g, color);
        
        double start = startTime.toEpochSecond(ZoneOffset.UTC) / 60;
        double end = endTime.toEpochSecond(ZoneOffset.UTC) / 60;
        double duration = end - start;
        
        CanvasUtils.drawCurvedRect(g, getLocalX(), getLocalY(), duration*view.getWidthPerMinute(), view.getGroupHeight());
        
        CanvasUtils.setFillColor(g, ColorUtils.getTextColor(color));
        g.fillText(spot, getLocalX(), getLocalY() + view.getGroupHeight());
        
        if (view.getGlobalMouseX() >= getGlobalX() && view.getGlobalMouseX() <= getGlobalX() + view.getWidthPerMinute()*duration &&
                view.getGlobalMouseY() >= getGlobalY() && view.getGlobalMouseY() <= getGlobalY() + view.getGroupHeight() ) {
            view.preparePopup(this.toString());
            
        }
    }
    
    @Override
    public void doDrawAt(CanvasRenderingContext2D g, double x, double y) {
        CanvasUtils.setFillColor(g, (isMouseOver)? ColorUtils.brighten(color, 0.25) :color);
        
        double start = startTime.toEpochSecond(ZoneOffset.UTC) / 60;
        double end = endTime.toEpochSecond(ZoneOffset.UTC) / 60;
        double duration = end - start;
        
        CanvasUtils.drawCurvedRect(g, x, y, duration*view.getWidthPerMinute(), view.getGroupHeight());
        
        CanvasUtils.setFillColor(g, ColorUtils.getTextColor(color));
        g.fillText(spot, x, y + view.getGroupHeight());
        
        if (view.getGlobalMouseX() >= getGlobalX() && view.getGlobalMouseX() <= getGlobalX() + view.getWidthPerMinute()*duration &&
                view.getLocalMouseY() >= y && view.getLocalMouseY() <= y + view.getGroupHeight() ) {
            view.preparePopup(this.toString());
            
        }
    }

    @Override
    public double getLocalX() {
        double start= startTime.toEpochSecond(ZoneOffset.UTC) / 60;
        return start*view.getWidthPerMinute();
    }

    @Override
    public double getLocalY() {
        Integer cursorIndex = view.getCursorIndex(spot);
        return (null != cursorIndex && cursorIndex > index)? index*view.getGroupHeight() : (index+1)*view.getGroupHeight();
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    @Override
    public boolean onMouseEnter(MouseEvent e, double x, double y) {
        isMouseOver = true;
        return true;
    }
    
    @Override
    public boolean onMouseExit(MouseEvent e, double x, double y) {
        isMouseOver = false;
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(spot);
        out.append(' ');
        out.append(CommonUtils.pad(startTime.getHour() + "", 2));
        out.append(':');
        out.append(CommonUtils.pad(startTime.getMinute() + "", 2));
        out.append('-');
        out.append(CommonUtils.pad(endTime.getHour() + "", 2));
        out.append(':');
        out.append(CommonUtils.pad(endTime.getMinute() + "", 2));
        return out.toString();
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String getGroupId() {
        return spot;
    }

}
