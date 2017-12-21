package org.optaplanner.openshift.employeerostering.shared.timeslot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TimeSlotTable<T> {

    /**
     * List of interval start and end points, in sorted order
     * (a < b if a.getPosition() < b.getPosition(), with start points
     * before end points in ties)
     */
    List<BoundaryPoint> intervalPoints;

    /**
     * Maps an end point to an interval
     */
    List<UUID> pairs;

    /**
     * Holds data belonging to an interval
     */
    Map<UUID, T> intervalData;

    public TimeSlotTable() {
        intervalPoints = new ArrayList<>();
        pairs = new ArrayList<>();
        intervalData = new HashMap<>();
    }

    private int getFirstIndexOf(int index, BoundaryPoint o) {
        if (index < 0) {
            return -(index + 1);
        }

        while (index > 0 && intervalPoints.get(index - 1).equals(o)) {
            index--;
        }
        return index;
    }

    private int getLastIndexOf(int index, BoundaryPoint o) {
        if (index < 0) {
            return -(index + 1);
        }

        while (index < intervalPoints.size() - 1 && intervalPoints.get(index + 1).equals(o)) {
            index++;
        }
        return index;
    }

    public void add(long start, long end, T data) {
        BoundaryPoint startPoint = new BoundaryPoint(start, true);
        BoundaryPoint endPoint = new BoundaryPoint(end, false);

        UUID uuid = UUID.randomUUID();
        int insertionPoint = getFirstIndexOf(Collections.binarySearch(intervalPoints, startPoint), startPoint);
        intervalPoints.add(insertionPoint, startPoint);
        pairs.add(insertionPoint, uuid);
        insertionPoint = getLastIndexOf(Collections.binarySearch(intervalPoints, endPoint), endPoint);
        intervalPoints.add(insertionPoint, endPoint);
        pairs.add(insertionPoint, uuid);
        intervalData.put(uuid, data);
    }

    public void remove(long start, long end) {
        BoundaryPoint startPoint = new BoundaryPoint(start, true);
        BoundaryPoint endPoint = new BoundaryPoint(end, false);

        int startIndex = getLastIndexOf(Collections.binarySearch(intervalPoints, startPoint), startPoint);
        final int endIndex = getLastIndexOf(Collections.binarySearch(intervalPoints, endPoint), endPoint);

        int pairIndex = endIndex;

        UUID uuid = pairs.get(startIndex);

        // Linearly searches the end points for one with the same UUID as the start point,
        // If none can be found, the next start point is checked
        for (; pairs.get(startIndex) != pairs.get(pairIndex); startIndex--) {
            uuid = pairs.get(startIndex);
            boolean found = false;
            for (pairIndex = endIndex - 1; intervalPoints.get(pairIndex).equals(endPoint); pairIndex--) {
                if (pairs.get(pairIndex).equals(uuid)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }

        }

        intervalData.remove(uuid);
        intervalPoints.remove(startIndex);
        intervalPoints.remove(pairIndex - 1);
        pairs.remove(startIndex);
        pairs.remove(pairIndex - 1);
    }

    public Iterable<TimeSlot<T>> getTimeSlots() {
        return new Iterable<TimeSlot<T>>() {

            @Override
            public Iterator<TimeSlot<T>> iterator() {
                return new TimeSlotIterator<T>(intervalPoints, pairs, intervalData);
            }
        };
    }

    public List<TimeSlot<T>> getTimeSlotsAsList() {
        List<TimeSlot<T>> out = new ArrayList<>(intervalData.size());
        for (TimeSlot<T> t : getTimeSlots()) {
            out.add(t);
        }
        return out;
    }

    public List<List<TimeSlot<T>>> getTimeSlotsAsGrid() {
        List<List<TimeSlot<T>>> grid = new ArrayList<>();
        for (TimeSlot<T> t : getTimeSlots()) {
            if (t.getSlot() >= grid.size()) {
                for (int i = grid.size(); i <= t.getSlot(); i++) {
                    grid.add(new ArrayList<>());
                }
            }
            grid.get(t.getSlot()).add(t);
        }
        return grid;
    }

    private static final class TimeSlotIterator<T> implements Iterator<TimeSlot<T>> {

        List<BoundaryPoint> intervalPoints;
        List<UUID> pairs;
        Map<UUID, T> intervalData;

        int index;
        Map<UUID, BoundaryPoint> startPoints;
        List<TimeSlot<T>> prev;
        TimeSlot<T> next;

        public TimeSlotIterator(List<BoundaryPoint> intervalPoints, List<UUID> pairs, Map<UUID, T> intervalData) {
            this.intervalPoints = intervalPoints;
            this.pairs = pairs;
            this.intervalData = intervalData;

            index = 0;
            startPoints = new HashMap<>();
            prev = new ArrayList<>();
            next();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public TimeSlot<T> next() {
            TimeSlot<T> out = next;
            if (intervalPoints.size() > index) {
                while (intervalPoints.get(index).isStartPoint()) {
                    startPoints.put(pairs.get(index), intervalPoints.get(index));
                    index++;
                }
                BoundaryPoint startPoint = startPoints.remove(pairs.get(index));
                int depth;

                for (depth = 0; depth < prev.size() &&
                        startPoint.getPosition() < prev.get(depth).getEndPoint().getPosition() &&
                        intervalPoints.get(index).getPosition() > prev.get(depth).getStartPoint()
                                .getPosition(); depth++) {
                }

                next = new TimeSlot<>(startPoint, intervalPoints.get(index), depth, intervalData.get(pairs
                        .get(index)));
                if (prev.size() <= depth) {
                    prev.add(next);
                } else {
                    prev.set(depth, next);
                }
                index++;
            } else {
                next = null;
            }
            return out;
        }
    }

    public static final class TimeSlot<T> {

        final BoundaryPoint startPoint;
        final BoundaryPoint endPoint;
        final int slot;
        final T data;

        public TimeSlot(BoundaryPoint start, BoundaryPoint end, int slot, T data) {
            this.startPoint = start;
            this.endPoint = end;
            this.slot = slot;
            this.data = data;
        }

        public BoundaryPoint getStartPoint() {
            return startPoint;
        }

        public BoundaryPoint getEndPoint() {
            return endPoint;
        }

        public long getLength() {
            return endPoint.getPosition() - startPoint.getPosition();
        }

        public int getSlot() {
            return slot;
        }

        public T getData() {
            return data;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + endPoint.hashCode();
            result = prime * result + slot;
            result = prime * result + startPoint.hashCode();
            result = prime * result + ((null != data) ? data.hashCode() : 0);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof TimeSlot))
                return false;
            TimeSlot other = (TimeSlot) obj;

            if (null == data) {
                if (null != other.data) {
                    return false;
                }
            } else if (!data.equals(other.data)) {
                return false;
            }

            return startPoint.equals(other.startPoint) && endPoint.equals(other.endPoint) && slot == other.slot;
        }

    }

    public static final class BoundaryPoint implements Comparable<BoundaryPoint> {

        final long position;
        final boolean isStartOfBoundary;

        public BoundaryPoint(long pos, boolean isStart) {
            this.position = pos;
            this.isStartOfBoundary = isStart;
        }

        public boolean isStartPoint() {
            return isStartOfBoundary;
        }

        public boolean isEndPoint() {
            return !isStartOfBoundary;
        }

        public long getPosition() {
            return position;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof BoundaryPoint) {
                BoundaryPoint other = (BoundaryPoint) o;
                return this.position == other.position && this.isStartOfBoundary == other.isStartOfBoundary;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(position) ^ Boolean.hashCode(isStartOfBoundary);
        }

        @Override
        public int compareTo(BoundaryPoint o) {
            int compareToResult = Long.compare(this.position, o.position);
            if (compareToResult != 0) {
                return compareToResult;
            }

            if (this.isStartOfBoundary == o.isStartOfBoundary) {
                return 0;
            } else if (this.isStartOfBoundary) {
                return -1;
            } else {
                return 1;
            }
        }
    }
}