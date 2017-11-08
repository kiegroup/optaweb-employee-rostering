package org.optaplanner.openshift.employeerostering.gwtui.client.interfaces;

import java.time.LocalDateTime;

public interface DataProvider<I extends HasTimeslot> {
    I getInstance(String groupId, LocalDateTime start, LocalDateTime end);
}
