package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;

import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;

public class ShiftData implements HasTimeslot {
    public LocalDateTime start;
    public LocalDateTime end;
    String spot;
    
    public ShiftData(LocalDateTime start, LocalDateTime end, String spot) {
        this.start = start;
        this.end = end;
        this.spot = spot;
    }
    
    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(spot);
        out.append(':');
        
        out.append(start.toString());
        out.append("-");
        out.append(end.toString());
        
        out.append(':');
        return out.toString();
    }

    @Override
    public String getGroupId() {
        return spot;
    }

    @Override
    public LocalDateTime getStartTime() {
        return start;
    }

    @Override
    public LocalDateTime getEndTime() {
        return end;
    }
}