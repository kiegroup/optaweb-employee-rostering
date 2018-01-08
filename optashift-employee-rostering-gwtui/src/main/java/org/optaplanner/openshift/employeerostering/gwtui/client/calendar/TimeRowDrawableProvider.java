package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayViewPresenter;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlotTable;

public interface TimeRowDrawableProvider<G extends HasTitle, I extends HasTimeslot<G>, T extends TimeRowDrawable<G,
        I>> {

    T createDrawable(TwoDayViewPresenter<G, I, T> twoDayViewPresenter,
            I data, int index);
}
