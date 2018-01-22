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

public class TimeSlotTableView<G extends HasTitle, I extends HasTimeslot<G>, T extends TimeRowDrawable<G, I>> {

    List<TimeSlotTable<I>> timeSlotTableList;
    List<G> groupList;
    TimeRowDrawableProvider<G, I, T> provider;
    TwoDayViewPresenter<G, I, T> twoDayViewPresenter;

    // TODO: Combine uuidToDrawableMap and drawableToUUIDMap into a BiMap
    Map<UUID, T> uuidToDrawableMap;
    Map<T, UUID> drawableToUUIDMap;
    Map<I, UUID> shiftToUUIDMap;
    Collection<T> timeSlots;
    Map<G, Integer> groupStartPosMap;
    Map<G, Integer> groupEndPosMap;
    Map<G, Integer> groupIndexMap;

    List<List<T>> visibleItemList;
    List<List<T>> allItemList;
    LocalDateTime startDate, endDate;
    Integer maxRow;

    public TimeSlotTableView(TwoDayViewPresenter<G, I, T> twoDayViewPresenter, List<G> groups, List<TimeSlotTable<
            I>> timeSlotTables, LocalDateTime startDate,
            LocalDateTime endDate, TimeRowDrawableProvider<G, I, T> provider) {
        this.twoDayViewPresenter = twoDayViewPresenter;
        this.startDate = startDate;
        this.endDate = endDate;
        this.groupList = groups;
        this.provider = provider;
        this.timeSlotTableList = timeSlotTables;

        uuidToDrawableMap = new HashMap<>();
        drawableToUUIDMap = new HashMap<>();
        shiftToUUIDMap = new HashMap<>();

        groupStartPosMap = new HashMap<>();
        groupEndPosMap = new HashMap<>();
        groupIndexMap = new HashMap<>();

        visibleItemList = new ArrayList<>();
        allItemList = new ArrayList<>();
        timeSlots = new HashSet<>();

        int startOffset = 0;
        for (int i = 0; i < groups.size(); i++) {
            groupStartPosMap.put(groups.get(i), startOffset);
            groupIndexMap.put(groups.get(i), i);

            List<List<TimeSlotTable.TimeSlot<I>>> timeSlotGrid = timeSlotTables.get(i).getTimeSlotsAsGrid();
            for (int y = 0; y < timeSlotGrid.size(); y++) {
                List<T> row = new ArrayList<>();
                for (TimeSlotTable.TimeSlot<I> t : timeSlotGrid.get(y)) {
                    T drawable = provider.createDrawable(twoDayViewPresenter, t.getData(), y);
                    row.add(drawable);
                    timeSlots.add(drawable);
                    uuidToDrawableMap.put(t.getUUID(), drawable);
                    shiftToUUIDMap.put(t.getData(), t.getUUID());
                    drawableToUUIDMap.put(drawable, t.getUUID());
                }
                allItemList.add(row);
                visibleItemList.add(new ArrayList<>());
            }
            startOffset += timeSlotGrid.size() + 1;
            allItemList.add(new ArrayList<>());
            visibleItemList.add(new ArrayList<>());
            groupEndPosMap.put(groups.get(i), startOffset - 1);
        }
        maxRow = startOffset;
        updateVisibleTimeSlots();
    }

    private void updateTimeSlotsFor(G group) {
        int startPos = groupStartPosMap.get(group);
        int endPos = groupEndPosMap.get(group);
        int index = groupIndexMap.get(group);
        allItemList.subList(startPos, endPos + 1).clear();
        visibleItemList.subList(startPos, endPos + 1).clear();

        List<List<TimeSlotTable.TimeSlot<I>>> timeSlotGrid = timeSlotTableList.get(index).getTimeSlotsAsGrid();
        for (int y = 0; y < timeSlotGrid.size(); y++) {
            List<T> row = new ArrayList<>();
            for (TimeSlotTable.TimeSlot<I> t : timeSlotGrid.get(y)) {
                T drawable = uuidToDrawableMap.get(t.getUUID());
                drawable.setIndex(y);
                row.add(drawable);
            }
            allItemList.add(y + startPos, row);
            visibleItemList.add(y + startPos, new ArrayList<>());
        }
        allItemList.add(timeSlotGrid.size() + startPos, new ArrayList<>());
        visibleItemList.add(timeSlotGrid.size() + startPos, new ArrayList<>());
        int newEndPos = startPos + timeSlotGrid.size();

        if (endPos != newEndPos) {
            groupEndPosMap.put(group, newEndPos);
            final int diff = newEndPos - endPos;
            for (int i = index + 1; i < groupList.size(); i++) {
                G myGroup = groupList.get(i);
                groupStartPosMap.compute(myGroup, (g, pos) -> pos + diff);
                groupEndPosMap.compute(myGroup, (g, pos) -> pos + diff);
            }
            maxRow += diff;
        }
        twoDayViewPresenter.getState().setAllDirty(true);
        updateVisibleTimeSlotsForGroup(group);
        twoDayViewPresenter.getPager().notifyCollectionChange();
    }

    private void updateVisibleTimeSlotsForGroup(G group) {
        int startPos = groupStartPosMap.get(group);
        int endPos = groupEndPosMap.get(group);
        int index = groupIndexMap.get(group);

        visibleItemList.subList(startPos, endPos).forEach((v) -> v.clear());
        List<List<TimeSlotTable.TimeSlot<I>>> timeSlotGrid = timeSlotTableList.get(index)
                .getTimeSlotsAsGrid(startDate.toEpochSecond(ZoneOffset.UTC),
                        endDate.toEpochSecond(ZoneOffset.UTC));
        for (int y = 0; y < timeSlotGrid.size(); y++) {
            for (TimeSlotTable.TimeSlot<I> t : timeSlotGrid.get(y)) {
                T drawable = uuidToDrawableMap.get(t.getUUID());
                visibleItemList.get(getRowIndexOf(drawable)).add(drawable);
            }
        }
        twoDayViewPresenter.getState().setVisibleDirty(true);
    }

    private void updateVisibleTimeSlots() {
        visibleItemList.forEach((v) -> v.clear());
        for (int i = 0; i < groupList.size(); i++) {
            List<List<TimeSlotTable.TimeSlot<I>>> timeSlotGrid = timeSlotTableList.get(i)
                    .getTimeSlotsAsGrid(startDate.toEpochSecond(ZoneOffset.UTC),
                            endDate.toEpochSecond(ZoneOffset.UTC));
            for (int y = 0; y < timeSlotGrid.size(); y++) {
                for (TimeSlotTable.TimeSlot<I> t : timeSlotGrid.get(y)) {
                    T drawable = uuidToDrawableMap.get(t.getUUID());
                    visibleItemList.get(getRowIndexOf(drawable)).add(drawable);
                }
            }
        }
        twoDayViewPresenter.getState().setVisibleDirty(true);
    }

    public void addTimeSlot(I shift) {
        UUID uuid = timeSlotTableList.get(groupIndexMap.get(shift.getGroupId()))
                .add(shift.getStartTime().toEpochSecond(ZoneOffset.UTC),
                        shift.getEndTime().toEpochSecond(ZoneOffset.UTC), shift);
        T drawable = provider.createDrawable(twoDayViewPresenter, shift, 0);
        uuidToDrawableMap.put(uuid, drawable);
        drawableToUUIDMap.put(drawable, uuid);
        shiftToUUIDMap.put(shift, uuid);
        timeSlots.add(drawable);
        updateTimeSlotsFor(shift.getGroupId());

    }

    public void updateTimeSlot(I oldShift, I newShift) {
        if (!oldShift.getGroupId().equals(newShift.getGroupId()) ||
                !oldShift.getStartTime().equals(newShift.getStartTime()) ||
                !oldShift.getEndTime().equals(newShift.getEndTime())) {
            throw new RuntimeException("Old Shift does not exist in the same time slot as New Shift");
        }

        UUID uuid = shiftToUUIDMap.get(oldShift);
        timeSlotTableList.get(groupIndexMap.get(oldShift.getGroupId()))
                .update(uuid, newShift);
        uuidToDrawableMap.get(uuid).updateData(newShift);
        shiftToUUIDMap.remove(oldShift);
        shiftToUUIDMap.put(newShift, uuid);
    }

    public void removeTimeSlot(I shift) {
        UUID uuid = timeSlotTableList.get(groupIndexMap.get(shift.getGroupId()))
                .remove(shift.getStartTime().toEpochSecond(ZoneOffset.UTC),
                        shift.getEndTime().toEpochSecond(ZoneOffset.UTC));
        T drawable = uuidToDrawableMap.get(uuid);
        uuidToDrawableMap.remove(uuid);
        drawableToUUIDMap.remove(drawable);
        timeSlots.remove(drawable);
        shiftToUUIDMap.remove(shift);
        updateTimeSlotsFor(shift.getGroupId());
    }

    public void removeTimeSlot(T drawable) {
        UUID uuid = drawableToUUIDMap.get(drawable);
        timeSlotTableList.get(groupIndexMap.get(drawable.getGroupId()))
                .remove(uuid);
        uuidToDrawableMap.remove(uuid);
        drawableToUUIDMap.remove(drawable);
        timeSlots.remove(drawable);
        updateTimeSlotsFor(drawable.getGroupId());
        twoDayViewPresenter.getState().setAllDirty(true);
        twoDayViewPresenter.getState().setVisibleDirty(true);
    }

    public int getNumberOfRows() {
        return maxRow;
    }

    public Integer getRowIndexOf(T timeslot) {
        return timeslot.getIndex() + groupStartPosMap.get(timeslot.getGroupId());
    }

    public Collection<T> getRowOf(T timeslot) {
        return visibleItemList.get(getRowIndexOf(timeslot));
    }

    public Collection<T> getVisibleRow(int index) {
        return visibleItemList.get(index);
    }

    public Collection<T> getRow(int index) {
        return allItemList.get(index);
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

    public Map<G, Integer> getGroupStartPosMap() {
        return groupStartPosMap;
    }

    public Map<G, Integer> getGroupEndPosMap() {
        return groupEndPosMap;
    }

    public Map<G, Integer> getGroupIndexMap() {
        return groupIndexMap;
    }

}
