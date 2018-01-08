package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;

public interface CalendarPresenter<G extends HasTitle, I extends HasTimeslot<G>> extends IsElement {

    void setDate(LocalDateTime date);

    LocalDateTime getViewStartDate();

    LocalDateTime getViewEndDate();

    LocalDateTime getHardStartDateBound();

    void setHardStartDateBound(LocalDateTime hardStartDateBound);

    LocalDateTime getHardEndDateBound();

    void setHardEndDateBound(LocalDateTime hardEndDateBound);

    void addShift(I shift);

    void updateShift(I oldShift, I newShift);

    void removeShift(I shift);

    void setShifts(Collection<I> shifts);

    Collection<G> getVisibleGroups();

    Collection<G> getGroups();

    void setGroups(List<G> groups);

    void draw();

    void onMouseDown(MouseEvent e);

    void onMouseMove(MouseEvent e);

    void onMouseUp(MouseEvent e);

    int getDaysShown();

    void setDaysShown(int daysShown);

    int getEditMinuteGradality();

    void setEditMinuteGradality(int editMinuteGradality);

    int getDisplayMinuteGradality();

    void setDisplayMinuteGradality(int displayMinuteGradality);

    void setViewSize(double screenWidth, double screenHeight);

    void setScreenHeight(double screenHeight);
}
