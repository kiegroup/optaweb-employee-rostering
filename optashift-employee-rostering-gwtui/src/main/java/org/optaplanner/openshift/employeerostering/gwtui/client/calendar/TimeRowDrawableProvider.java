package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayViewPresenter;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;

public interface TimeRowDrawableProvider<G extends HasTitle, I extends HasTimeslot<G>, T extends TimeRowDrawable<G>> {

    T createDrawable(TwoDayViewPresenter<G, I, T> twoDayViewPresenter,
            I data,
            int index);
}
