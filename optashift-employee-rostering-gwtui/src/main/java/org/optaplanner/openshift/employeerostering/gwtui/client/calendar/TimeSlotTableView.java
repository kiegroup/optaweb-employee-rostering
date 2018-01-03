package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayViewPresenter;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlotTable;

public class TimeSlotTableView<G extends HasTitle, I extends HasTimeslot<G>, T extends TimeRowDrawable<G>> {

    List<TimeSlotTable<I>> timeSlotTables;
    List<G> groups;
    TimeRowDrawableProvider<G, I, T> provider;
    TwoDayViewPresenter<G, I, T> twoDayViewPresenter;

    Map<UUID, T> uuidToDrawable;
    Map<T, UUID> drawableToUUID;
    Collection<T> timeSlots;
    Map<G, Integer> groupStartPos;
    Map<G, Integer> groupEndPos;
    Map<G, Integer> groupIndex;

    List<List<T>> visibleItems;
    List<List<T>> allItems;
    LocalDateTime startDate, endDate;
    Integer maxRow;

    public TimeSlotTableView(TwoDayViewPresenter<G, I, T> twoDayViewPresenter, List<G> groups, List<TimeSlotTable<
            I>> timeSlotTables, LocalDateTime startDate,
            LocalDateTime endDate, TimeRowDrawableProvider<G, I, T> provider) {
        this.twoDayViewPresenter = twoDayViewPresenter;
        this.startDate = startDate;
        this.endDate = endDate;
        this.groups = groups;
        this.provider = provider;
        this.timeSlotTables = timeSlotTables;

        uuidToDrawable = new HashMap<>();
        drawableToUUID = new HashMap<>();

        groupStartPos = new HashMap<>();
        groupEndPos = new HashMap<>();
        groupIndex = new HashMap<>();

        visibleItems = new ArrayList<>();
        allItems = new ArrayList<>();
        timeSlots = new HashSet<>();

        int startOffset = 0;
        for (int i = 0; i < groups.size(); i++) {
            groupStartPos.put(groups.get(i), startOffset);
            groupIndex.put(groups.get(i), i);

            List<List<TimeSlotTable.TimeSlot<I>>> timeSlotGrid = timeSlotTables.get(i).getTimeSlotsAsGrid();
            for (int y = 0; y < timeSlotGrid.size(); y++) {
                List<T> row = new ArrayList<>();
                for (TimeSlotTable.TimeSlot<I> t : timeSlotGrid.get(y)) {
                    T drawable = provider.createDrawable(twoDayViewPresenter, t.getData(), y);
                    row.add(drawable);
                    timeSlots.add(drawable);
                    uuidToDrawable.put(t.getUUID(), drawable);
                    drawableToUUID.put(drawable, t.getUUID());
                }
                allItems.add(row);
                visibleItems.add(new ArrayList<>());
            }
            startOffset += timeSlotGrid.size() + 1;
            allItems.add(new ArrayList<>());
            visibleItems.add(new ArrayList<>());
            groupEndPos.put(groups.get(i), startOffset - 1);
        }
        maxRow = startOffset;
        updateVisibleTimeSlots();
    }

    private void updateTimeSlotsFor(G group) {
        int startPos = groupStartPos.get(group);
        int endPos = groupEndPos.get(group);
        int index = groupIndex.get(group);
        allItems.subList(startPos, endPos + 1).clear();
        visibleItems.subList(startPos, endPos + 1).clear();

        List<List<TimeSlotTable.TimeSlot<I>>> timeSlotGrid = timeSlotTables.get(index).getTimeSlotsAsGrid();
        for (int y = 0; y < timeSlotGrid.size(); y++) {
            List<T> row = new ArrayList<>();
            for (TimeSlotTable.TimeSlot<I> t : timeSlotGrid.get(y)) {
                T drawable = uuidToDrawable.get(t.getUUID());
                drawable.setIndex(y);
                row.add(drawable);
            }
            allItems.add(y + startPos, row);
            visibleItems.add(y + startPos, new ArrayList<>());
        }
        allItems.add(timeSlotGrid.size() + startPos, new ArrayList<>());
        visibleItems.add(timeSlotGrid.size() + startPos, new ArrayList<>());
        int newEndPos = startPos + timeSlotGrid.size();

        if (endPos != newEndPos) {
            groupEndPos.put(group, newEndPos);
            final int diff = newEndPos - endPos;
            for (int i = index + 1; i < groups.size(); i++) {
                G myGroup = groups.get(i);
                groupStartPos.compute(myGroup, (g, pos) -> pos + diff);
                groupEndPos.compute(myGroup, (g, pos) -> pos + diff);
            }
            maxRow += diff;
        }
        twoDayViewPresenter.getState().setAllDirty(true);
        updateVisibleTimeSlotsForGroup(group);
        twoDayViewPresenter.getPager().notifyCollectionChange();
    }

    private void updateVisibleTimeSlotsForGroup(G group) {
        int startPos = groupStartPos.get(group);
        int endPos = groupEndPos.get(group);
        int index = groupIndex.get(group);

        visibleItems.subList(startPos, endPos).forEach((v) -> v.clear());
        List<List<TimeSlotTable.TimeSlot<I>>> timeSlotGrid = timeSlotTables.get(index)
                .getTimeSlotsAsGrid(startDate.toEpochSecond(ZoneOffset.UTC),
                        endDate.toEpochSecond(ZoneOffset.UTC));
        for (int y = 0; y < timeSlotGrid.size(); y++) {
            for (TimeSlotTable.TimeSlot<I> t : timeSlotGrid.get(y)) {
                T drawable = uuidToDrawable.get(t.getUUID());
                visibleItems.get(getRowIndexOf(drawable)).add(drawable);
            }
        }
        twoDayViewPresenter.getState().setVisibleDirty(true);
    }

    private void updateVisibleTimeSlots() {
        visibleItems.forEach((v) -> v.clear());
        for (int i = 0; i < groups.size(); i++) {
            List<List<TimeSlotTable.TimeSlot<I>>> timeSlotGrid = timeSlotTables.get(i)
                    .getTimeSlotsAsGrid(startDate.toEpochSecond(ZoneOffset.UTC),
                            endDate.toEpochSecond(ZoneOffset.UTC));
            for (int y = 0; y < timeSlotGrid.size(); y++) {
                for (TimeSlotTable.TimeSlot<I> t : timeSlotGrid.get(y)) {
                    T drawable = uuidToDrawable.get(t.getUUID());
                    visibleItems.get(getRowIndexOf(drawable)).add(drawable);
                }
            }
        }
        twoDayViewPresenter.getState().setVisibleDirty(true);
    }

    public void addTimeSlot(I shift) {
        UUID uuid = timeSlotTables.get(groupIndex.get(shift.getGroupId()))
                .add(shift.getStartTime().toEpochSecond(ZoneOffset.UTC),
                        shift.getEndTime().toEpochSecond(ZoneOffset.UTC), shift);
        T drawable = provider.createDrawable(twoDayViewPresenter, shift, 0);
        uuidToDrawable.put(uuid, drawable);
        drawableToUUID.put(drawable, uuid);
        timeSlots.add(drawable);
        updateTimeSlotsFor(shift.getGroupId());

    }

    public void removeTimeSlot(I shift) {
        UUID uuid = timeSlotTables.get(groupIndex.get(shift.getGroupId()))
                .remove(shift.getStartTime().toEpochSecond(ZoneOffset.UTC),
                        shift.getEndTime().toEpochSecond(ZoneOffset.UTC));
        T drawable = uuidToDrawable.get(uuid);
        uuidToDrawable.remove(uuid);
        drawableToUUID.remove(drawable);
        timeSlots.remove(drawable);
        updateTimeSlotsFor(shift.getGroupId());
    }

    public void removeTimeSlot(T drawable) {
        UUID uuid = drawableToUUID.get(drawable);
        timeSlotTables.get(groupIndex.get(drawable.getGroupId()))
                .remove(uuid);
        uuidToDrawable.remove(uuid);
        drawableToUUID.remove(drawable);
        timeSlots.remove(drawable);
        updateTimeSlotsFor(drawable.getGroupId());
        twoDayViewPresenter.getState().setAllDirty(true);
        twoDayViewPresenter.getState().setVisibleDirty(true);
    }

    public int getNumberOfRows() {
        return maxRow;
    }

    public Integer getRowIndexOf(T timeslot) {
        return timeslot.getIndex() + groupStartPos.get(timeslot.getGroupId());
    }

    public Collection<T> getRowOf(T timeslot) {
        return visibleItems.get(getRowIndexOf(timeslot));
    }

    public Collection<T> getVisibleRow(int index) {
        return visibleItems.get(index);
    }

    public Collection<T> getRow(int index) {
        return allItems.get(index);
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
        updateVisibleTimeSlots();
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
        updateVisibleTimeSlots();
    }

    public Collection<T> getTimeSlots() {
        return timeSlots;
    }

    public Map<G, Integer> getGroupStartPos() {
        return groupStartPos;
    }

    public Map<G, Integer> getGroupEndPos() {
        return groupEndPos;
    }

    public Map<G, Integer> getGroupIndex() {
        return groupIndex;
    }

}
