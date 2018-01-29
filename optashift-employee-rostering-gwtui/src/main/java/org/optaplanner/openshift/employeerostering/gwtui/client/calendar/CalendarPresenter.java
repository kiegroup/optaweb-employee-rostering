package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;

/**
 * Presents shifts in a calendar.
 *
 * @param <G> Type of Group.
 * @param <I> Type of Shift.
 */
public interface CalendarPresenter<G extends HasTitle, I extends HasTimeslot<G>> extends IsElement {

    /**
     * Sets the first date visible in the Calendar.
     * @param date The date to set the first date visible in the Calendar to.
     */
    void setDate(LocalDateTime date);

    /**
     * Gets the first date visible in the Calendar.
     * @return The first date visible in the Calendar.
     */
    LocalDateTime getViewStartDate();

    /**
     * Gets the last date visible in the Calendar.
     * @return The last date visible in the Calendar.
     */
    LocalDateTime getViewEndDate();

    /**
     * Gets the earliest date that can be seen in the Calendar.
     * @return The earliest date that can be seen in the Calendar.
     */
    LocalDateTime getHardStartDateBound();

    /**
     * Sets the earliest date that can be seen in the Calendar.
     * @param hardStartDateBound The new earliest date that can be seen on the Calendar.
     */
    void setHardStartDateBound(LocalDateTime hardStartDateBound);

    /**
     * Gets the latest date that can be seen in the Calendar.
     * @return The latest date that can be seen in the Calendar.
     */
    LocalDateTime getHardEndDateBound();

    /**
     * Sets the latest date that can be seen in the Calendar.
     * @param hardEndDateBound The new latest date that can be seen on the Calendar.
     */
    void setHardEndDateBound(LocalDateTime hardEndDateBound);

    /**
     * Adds a shift.
     * @param shift The shift to add.
     */
    void addShift(I shift);

    /**
     * Updates a shift.
     * @param oldShift The old shift. Must already exist.
     * @param newShift The new shift. Must have the same ID as oldShift.
     */
    void updateShift(I oldShift, I newShift);

    /**
     * Removes a shift.
     * @param shift The shift to remove.
     */
    void removeShift(I shift);

    /**
     * Sets the shifts (remove the current shifts and replace them with this collection).
     * @param shifts Shifts to replace the current shifts with.
     */
    void setShifts(Collection<I> shifts);

    /**
     * Returns the groups that are visible.
     * @return The groups that are visible.
     */
    Set<G> getVisibleGroupSet();

    /**
     * Returns all the groups.
     * @return All the groups.
     */
    List<G> getGroupList();

    /**
     * Sets the groups.
     * @param groupList What to set the group list to.
     */
    void setGroupList(List<G> groupList);

    /**
     * Draw the shifts.
     */
    void draw();

    /**
     * Called on mouse down events.
     * @param e The source of the event.
     */
    void onMouseDown(MouseEvent e);

    /**
     * Called on mouse move events.
     * @param e The source of the event.
     */
    void onMouseMove(MouseEvent e);

    /**
     * Called on mouse up events.
     * @param e The source of the event.
     */
    void onMouseUp(MouseEvent e);

    /**
     * Returns the number of days shown by this presenter.
     * @return The number of days shown by this presenter.
     */
    int getDaysShown();

    /**
     * Sets the number of days shown by this presenter.
     * @param daysShown The number of days to show.
     */
    void setDaysShown(int daysShown);

    /**
     * Returns the smallest length a shift can be, in minutes.
     * @return  The smallest length a shift can be, in minutes.
     */
    int getEditMinuteGradality();

    /**
     * Sets the smallest length a shift can be, in minutes.
     * @param editMinuteGradality The smallest length a shift can be, in minutes.
     */
    void setEditMinuteGradality(int editMinuteGradality);

    /**
     * Returns the minutes between successive stripe bars, if applicable.
     * @return The minutes between successive stripe bars, if applicable.
     */
    int getDisplayMinuteGradality();

    /**
     * Sets the minutes between successive stripe bars, if applicable.
     * param displayMinuteGradality The minutes between successive stripe bars, if applicable.
     */
    void setDisplayMinuteGradality(int displayMinuteGradality);

    /**
     * Sets the size of the widget.
     * @param screenWidth The new width of the widget.
     * @param screenHeight The new height of the widget.
     */
    void setViewSize(double screenWidth, double screenHeight);
}
