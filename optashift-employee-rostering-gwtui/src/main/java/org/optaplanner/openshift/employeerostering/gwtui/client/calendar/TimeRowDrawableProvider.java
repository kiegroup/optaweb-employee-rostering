package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayViewPresenter;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;

/**
 * Provider of {@link TimeRowDrawable}.
 *
 * @param <G> Type of Group.
 * @param <I> Type of Shift.
 * @param <T> Type of {@link TimeRowDrawable} this Provider provides.
 */
public interface TimeRowDrawableProvider<G extends HasTitle, I extends HasTimeslot<G>, T extends TimeRowDrawable<G, I>> {

    /**
     * Creates a {@link TimeRowDrawable}.
     * @param twoDayViewPresenter Where to put the {@link TimeRowDrawable}.
     * @param data Shift this {@link TimeRowDrawable} represents.
     * @param index The initial row index of {@link TimeRowDrawable}.
     * @return A new {@link TimeRowDrawable} representing the specified shift
     * in the specified presenter. 
     */
    T createDrawable(TwoDayViewPresenter<G, I, T> twoDayViewPresenter,
                     I data,
                     int index);
}
