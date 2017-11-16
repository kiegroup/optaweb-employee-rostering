package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.MouseEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.AbstractDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TimeRowDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TwoDayView;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.ColorUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class EmployeeDrawable  extends AbstractDrawable implements TimeRowDrawable {
    
    TwoDayView view;
    EmployeeData data;
    int index;
    boolean isMouseOver;
    
    public EmployeeDrawable(TwoDayView view, EmployeeData data, int index) {
        this.view = view;
        this.data = data;
        this.index = index;
        this.isMouseOver = false;
        //ErrorPopup.show(this.toString());
    }
    
    @Override
    public double getLocalX() {
        double start= getStartTime().toEpochSecond(ZoneOffset.UTC) / 60;
        return start*view.getWidthPerMinute();
    }

    @Override
    public double getLocalY() {
        Integer cursorIndex = view.getCursorIndex(getGroupId());
        return (null != cursorIndex && cursorIndex > index)? index*view.getGroupHeight() : (index+1)*view.getGroupHeight();
    }

    @Override
    public void doDrawAt(CanvasRenderingContext2D g, double x, double y) {
        String color = getFillColor();
        CanvasUtils.setFillColor(g, color);
        
        double start = getStartTime().toEpochSecond(ZoneOffset.UTC) / 60;
        double end = getEndTime().toEpochSecond(ZoneOffset.UTC) / 60;
        double duration = end - start;
        
        CanvasUtils.drawCurvedRect(g, x, y, duration*view.getWidthPerMinute(), view.getGroupHeight());
        
        CanvasUtils.setFillColor(g, ColorUtils.getTextColor(color));
        
        String spots;
        if (data.getSpots().isEmpty()) {
            spots = "Unassigned";
        }
        else {
            spots = "";
            for (Spot spot : data.getSpots()) {
                spots = (null != spot)? spots + "," + spot.getName() : spots;
            }
            spots = (spots.isEmpty())? "Unassigned" : spots.substring(1);
        }
        g.fillText(spots, x, y + view.getGroupHeight());
    }
    
    @Override
    public boolean onMouseMove(MouseEvent e, double x, double y) {
        view.preparePopup(this.toString());
        return true;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public String getGroupId() {
        return data.getGroupId();
    }

    @Override
    public LocalDateTime getStartTime() {
        return data.getStartTime();
    }

    @Override
    public LocalDateTime getEndTime() {
        return data.getEndTime();
    }

    @Override
    public void doDraw(CanvasRenderingContext2D g) {
        String color = getFillColor();
        CanvasUtils.setFillColor(g, color);
        
        double start = getStartTime().toEpochSecond(ZoneOffset.UTC) / 60;
        double end = getEndTime().toEpochSecond(ZoneOffset.UTC) / 60;
        double duration = end - start;
        
        CanvasUtils.drawCurvedRect(g, getLocalX(), getLocalY(), duration*view.getWidthPerMinute(), view.getGroupHeight());
        
        CanvasUtils.setFillColor(g, ColorUtils.getTextColor(color));
        
        String spots = (!data.getSpots().isEmpty())? data.getSpots().stream().reduce("",
                (a,b) -> (a.isEmpty())? b.getName() : a + "," + b.getName(), (a,b) -> (a.isEmpty())? b : a + "," + b) : "Unassigned";
        g.fillText(spots, getLocalX(), getLocalY() + view.getGroupHeight());
        
        if (view.getGlobalMouseX() >= getGlobalX() && view.getGlobalMouseX() <= getGlobalX() + view.getWidthPerMinute()*duration &&
                view.getGlobalMouseY() >= getGlobalY() && view.getGlobalMouseY() <= getGlobalY() + view.getGroupHeight() ) {
            view.preparePopup(this.toString());
            
        }
    }
    
    public String toString() {
        StringBuilder out = new StringBuilder(data.getEmployee().getName());
        out.append(' ');
        out.append(CommonUtils.pad(getStartTime().getHour() + "", 2));
        out.append(':');
        out.append(CommonUtils.pad(getStartTime().getMinute() + "", 2));
        out.append('-');
        out.append(CommonUtils.pad(getEndTime().getHour() + "", 2));
        out.append(':');
        out.append(CommonUtils.pad(getEndTime().getMinute() + "", 2));
        out.append(" -- ");
        String spots;
        if (data.getSpots().isEmpty()) {
            spots = "Nothing";
        }
        else {
            spots = "";
            for (Spot spot : data.getSpots()) {
                spots = (null != spot)? spots + "," + spot.getName() : spots;
            }
            spots = (spots.isEmpty())? "Nothing" : spots.substring(1);
        }
        out.append("Assigned to ");
        out.append(spots);
        out.append("; slot is ");
        out.append((data.getAvailability() != null)? data.getAvailability().getState().toString() : "Indifferent");
        return out.toString();
    }
    
    private String getFillColor() {
        if (null == data.getAvailability()) {
            return "#0000FF";
        }
        
        switch (data.getAvailability().getState()) {
            case UNDESIRED:
                return "#FF0000";
            case DESIRED:
                return "#00FF00";
            case UNAVAILABLE:
                return "#000000";
            default:
                return "#FFFFFF";
        }
    }

}
