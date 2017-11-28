package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlotUtils;

public class TimeSlotTable<T extends TimeRowDrawable<G>, G extends HasTitle> {

    Collection<T> timeslots;
    Map<G, Integer> groupIndex;

    Map<Integer, Collection<T>> visableItems;
    Map<Integer, Collection<T>> allItems;
    LocalDateTime startDate, endDate;

    public TimeSlotTable(Collection<T> timeslots, Map<G, Integer> groupIndex, LocalDateTime startDate,
            LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.groupIndex = groupIndex;

        this.timeslots = timeslots;

        allItems = new HashMap<>();
        visableItems = new HashMap<>();
        for (T timeslot : timeslots) {
            visableItems.compute(getRowIndexOf(timeslot),
                    (i, v) -> {
                        if (null == v) {
                            v = new HashSet<>();
                            if (TimeSlotUtils.doTimeslotsIntersect(timeslot.getStartTime(), timeslot
                                    .getEndTime(), startDate, endDate)) {
                                v.add(timeslot);
                            }
                            return v;
                        } else {
                            if (TimeSlotUtils.doTimeslotsIntersect(timeslot.getStartTime(), timeslot
                                    .getEndTime(), startDate, endDate)) {
                                v.add(timeslot);
                            }
                            return v;
                        }
                    });
            allItems.compute(getRowIndexOf(timeslot),
                    (i, v) -> {
                        if (null == v) {
                            v = new HashSet<>();
                            v.add(timeslot);
                            return v;
                        } else {
                            v.add(timeslot);
                            return v;
                        }
                    });
        }
    }

    public Integer getRowIndexOf(T timeslot) {
        return timeslot.getIndex() + groupIndex.get(timeslot.getGroupId());
    }

    public Collection<T> getRowOf(T timeslot) {
        return visableItems.get(getRowIndexOf(timeslot));
    }

    public Collection<T> getVisableRow(int index) {
        return visableItems.getOrDefault(index, Collections.emptyList());
    }

    public Collection<T> getRow(int index) {
        return allItems.getOrDefault(index, Collections.emptyList());
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
        visableItems.clear();
        for (T timeslot : timeslots) {
            visableItems.compute(getRowIndexOf(timeslot),
                    (i, v) -> {
                        if (null == v) {
                            v = new HashSet<>();
                            if (TimeSlotUtils.doTimeslotsIntersect(timeslot.getStartTime(), timeslot
                                    .getEndTime(), startDate, endDate)) {
                                v.add(timeslot);
                            }
                            return v;
                        } else {
                            if (TimeSlotUtils.doTimeslotsIntersect(timeslot.getStartTime(), timeslot
                                    .getEndTime(), startDate, endDate)) {
                                v.add(timeslot);
                            }
                            return v;
                        }
                    });
        }
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
        visableItems.clear();
        for (T timeslot : timeslots) {
            visableItems.compute(getRowIndexOf(timeslot),
                    (i, v) -> {
                        if (null == v) {
                            v = new HashSet<>();
                            if (TimeSlotUtils.doTimeslotsIntersect(timeslot.getStartTime(), timeslot
                                    .getEndTime(), startDate, endDate)) {
                                v.add(timeslot);
                            }
                            return v;
                        } else {
                            if (TimeSlotUtils.doTimeslotsIntersect(timeslot.getStartTime(), timeslot
                                    .getEndTime(), startDate, endDate)) {
                                v.add(timeslot);
                            }
                            return v;
                        }
                    });
        }
    }
}
