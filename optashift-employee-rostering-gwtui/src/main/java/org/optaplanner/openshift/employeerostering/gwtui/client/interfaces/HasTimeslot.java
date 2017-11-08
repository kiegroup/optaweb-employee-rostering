package org.optaplanner.openshift.employeerostering.gwtui.client.interfaces;

import java.time.LocalDateTime;

public interface HasTimeslot {
    String getGroupId();
    LocalDateTime getStartTime();
    LocalDateTime getEndTime();
}
