package org.optaplanner.openshift.employeerostering.shared.common;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public interface HasTimeslot {

    public static LocalDateTime EPOCH = LocalDateTime.ofEpochSecond(0, 0, ZoneOffset.UTC);

    /**
     * If the HasTimeslot is based on LocalDateTime, its reference will be HasTimeslot.EPOCH
     */
    Duration getDurationBetweenReferenceAndStart();

    Duration getDurationOfTimeslot();
}
