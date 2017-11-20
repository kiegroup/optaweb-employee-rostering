package org.optaplanner.openshift.employeerostering.gwtui.client.interfaces;

import java.time.LocalDateTime;

import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Calendar;

public interface DataProvider<I extends HasTimeslot> {
    void getInstance(Calendar<I> calendar, String groupId, LocalDateTime start, LocalDateTime end);
}
