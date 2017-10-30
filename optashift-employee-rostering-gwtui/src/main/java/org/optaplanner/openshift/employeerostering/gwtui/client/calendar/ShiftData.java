package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.util.Collection;

public class ShiftData {
    LocalDateTime start;
    LocalDateTime end;
    Collection<String> spots;
    final long UID;
    private static long total;
    
    public ShiftData(LocalDateTime start, LocalDateTime end, Collection<String> spots) {
        this.start = start;
        this.end = end;
        this.spots = spots;
        UID = total;
        total++;
    }
}