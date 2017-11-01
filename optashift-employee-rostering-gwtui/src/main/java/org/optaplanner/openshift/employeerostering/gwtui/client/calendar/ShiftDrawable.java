package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import elemental2.dom.CanvasRenderingContext2D;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.ColorUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;


public class ShiftDrawable extends AbstractDrawable {
    String spot;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String color;
    int index;
    TwoDayView view;
    
    
    public ShiftDrawable(TwoDayView view, String spot, LocalDateTime startTime, LocalDateTime endTime, String color, int index) {
        this.view = view;
        this.startTime = startTime;
        this.endTime = endTime;
        this.spot = spot;
        this.color = color;
        this.index = index;
    }

    @Override
    public void doDraw(CanvasRenderingContext2D g) {
        CanvasUtils.setFillColor(g, color);
        
        double start = startTime.toEpochSecond(ZoneOffset.UTC) / 60;
        double end = endTime.toEpochSecond(ZoneOffset.UTC) / 60;
        double duration = end - start;
        
        CanvasUtils.drawCurvedRect(g, getLocalX(), getLocalY(), duration*view.getWidthPerMinute(), view.getSpotHeight());
        
        CanvasUtils.setFillColor(g, ColorUtils.getTextColor(color));
        g.fillText(spot, getLocalX(), getLocalY() + view.getSpotHeight());
        
        if (view.getMouseX() >= getGlobalX() && view.getMouseX() <= getGlobalX() + view.getWidthPerMinute()*duration &&
                view.getMouseY() >= getGlobalY() && view.getMouseY() <= getGlobalY() + view.getSpotHeight() ) {
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
        Long cursorIndex = view.getSpotCursorIndex(spot);
        return (null != cursorIndex && cursorIndex > index)? index*view.getSpotHeight() : (index+1)*view.getSpotHeight();
    }
    
    public void setIndex(int index) {
        this.index = index;
    }
    
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(spot);
        out.append(' ');
        out.append(pad(startTime.getHour() + "", 2));
        out.append(':');
        out.append(pad(startTime.getMinute() + "", 2));
        out.append('-');
        out.append(pad(endTime.getHour() + "", 2));
        out.append(':');
        out.append(pad(endTime.getMinute() + "", 2));
        return out.toString();
    }
    
    private String pad(String str, int len) {
        StringBuilder out = new StringBuilder(str);
        while (out.length() < len) {
            out.insert(0, "0");
        }
        return out.toString();
    }

}
