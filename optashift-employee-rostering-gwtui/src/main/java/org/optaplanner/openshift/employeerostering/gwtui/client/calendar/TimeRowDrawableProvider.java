package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;

public interface TimeRowDrawableProvider<I extends HasTimeslot, T extends TimeRowDrawable> {
    T createDrawable(TwoDayView<I,T> view,
            I data,
            int index);
}
