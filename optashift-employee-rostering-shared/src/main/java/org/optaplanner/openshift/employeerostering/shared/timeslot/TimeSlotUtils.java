package org.optaplanner.openshift.employeerostering.shared.timeslot;

import java.time.LocalDateTime;

public class TimeSlotUtils {

    public static boolean doTimeslotsIntersect(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2,
            LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
}
