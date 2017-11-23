package org.optaplanner.openshift.employeerostering.gwtui.client.interfaces;

import java.time.LocalDateTime;

import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.HasTitle;

public interface HasTimeslot<G extends HasTitle> {

    G getGroupId();

    LocalDateTime getStartTime();

    LocalDateTime getEndTime();
}
