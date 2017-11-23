package org.optaplanner.openshift.employeerostering.gwtui.client.interfaces;

import java.time.LocalDateTime;

import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Calendar;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.HasTitle;

public interface DataProvider<G extends HasTitle, I extends HasTimeslot<G>> {

    void getInstance(Calendar<G, I> calendar, G group, LocalDateTime start, LocalDateTime end);
}
