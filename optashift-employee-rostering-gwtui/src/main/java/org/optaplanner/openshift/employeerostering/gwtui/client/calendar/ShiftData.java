package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.util.Collection;

public class ShiftData {
    public LocalDateTime start;
    public LocalDateTime end;
    public Collection<String> spots;
    final long UID;
    private static long total;
    
    public ShiftData(LocalDateTime start, LocalDateTime end, Collection<String> spots) {
        this.start = start;
        this.end = end;
        this.spots = spots;
        UID = total;
        total++;
    }
    
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (String spot : spots) {
            out.append(spot);
            out.append(';');
        }
        out.deleteCharAt(out.length() - 1);
        out.append(':');
        
        out.append(start.toString());
        out.append("-");
        out.append(end.toString());
        
        out.append(':');
        out.append(UID);
        return out.toString();
    }
}