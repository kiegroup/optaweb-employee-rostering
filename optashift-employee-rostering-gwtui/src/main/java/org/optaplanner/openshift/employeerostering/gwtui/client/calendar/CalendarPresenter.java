package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import elemental2.dom.MouseEvent;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
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

    Set<G> getVisibleGroupSet();

    List<G> getGroupList();

    void setGroupList(List<G> groupList);

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
