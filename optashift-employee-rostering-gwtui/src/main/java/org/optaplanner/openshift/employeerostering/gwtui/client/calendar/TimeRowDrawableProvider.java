package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;

public interface TimeRowDrawableProvider<G extends HasTitle, I extends HasTimeslot<G>, T extends TimeRowDrawable<G>> {

    T createDrawable(TwoDayView<G, I, T> view,
            I data,
            int index);
}
