/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.shared.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TimeSlotTable<T> {

    /**
     * List of interval start points, in sorted order
     */
    List<BoundaryPoint> startPointList;

    /**
     * List of interval end points, in sorted order
     */
    List<BoundaryPoint> endPointList;

    /**
     * Holds data belonging to an interval
     */
    Map<UUID, TimeSlot<T>> intervalDataMap;

    public TimeSlotTable() {
        startPointList = new ArrayList<>();
        endPointList = new ArrayList<>();
        intervalDataMap = new HashMap<>();
    }

    private int getFirstIndexOf(int index, BoundaryPoint o) {
        if (index < 0) {
            return -(index + 1);
        }
        List<BoundaryPoint> intervalPoints = (o.isStartPoint()) ? startPointList : endPointList;

        while (index > 0 && intervalPoints.get(index - 1).equals(o)) {
            index--;
        }
        return index;
    }

    private int getLastIndexOf(int index, BoundaryPoint o) {
        if (index < 0) {
            return -(index + 1);
        }

        List<BoundaryPoint> intervalPoints = (o.isStartPoint()) ? startPointList : endPointList;
        while (index < intervalPoints.size() - 1 && intervalPoints.get(index + 1).equals(o)) {
            index++;
        }
        return index;
    }

    public UUID add(long start, long end, T data) {
        UUID uuid = UUID.randomUUID();
        BoundaryPoint startPoint = new BoundaryPoint(start, true, uuid);
        BoundaryPoint endPoint = new BoundaryPoint(end, false, uuid);

        int insertionPoint = getFirstIndexOf(Collections.binarySearch(startPointList, startPoint), startPoint);
        startPointList.add(insertionPoint, startPoint);
        insertionPoint = getLastIndexOf(Collections.binarySearch(endPointList, endPoint), endPoint);
        endPointList.add(insertionPoint, endPoint);

        intervalDataMap.put(uuid, new TimeSlot<T>(startPoint, endPoint, data));
        return uuid;
    }

    public UUID get(T data) {
        for (Entry<UUID, TimeSlot<T>> e : intervalDataMap.entrySet()) {
            if (e.getValue().getData().equals(data)) {
                return e.getKey();
            }
        }
        return null;
    }

    public void update(UUID uuid, T newData) {
        TimeSlot<T> orig = intervalDataMap.get(uuid);
        intervalDataMap.put(uuid, new TimeSlot<>(orig.getStartPoint(), orig.getEndPoint(), newData));
    }

    public void remove(TimeSlot<T> timeSlot) {
        BoundaryPoint startPoint = timeSlot.getStartPoint();
        BoundaryPoint endPoint = timeSlot.getEndPoint();

        int startIndex = getLastIndexOf(Collections.binarySearch(startPointList, startPoint), startPoint);
        int endIndex = getLastIndexOf(Collections.binarySearch(endPointList, endPoint), endPoint);

        while (!startPointList.get(startIndex).getUUID().equals(startPoint.getUUID())) {
            startIndex--;
        }

        while (!endPointList.get(endIndex).getUUID().equals(endPoint.getUUID())) {
            endIndex--;
        }

        intervalDataMap.remove(timeSlot.getUUID());
        startPointList.remove(startIndex);
        endPointList.remove(endIndex);
    }

    public void remove(UUID uuid) {
        if (!intervalDataMap.keySet().contains(uuid)) {
            StringBuilder errorMsg = new StringBuilder("UUID \"" + uuid + "\" was not found:\nUUIDS: {");
            intervalDataMap.keySet().forEach((e) -> errorMsg.append(e.toString() + ";"));
            errorMsg.append("}");
            throw new RuntimeException(errorMsg.toString());
        }
        remove(intervalDataMap.get(uuid));
    }

    public UUID remove(long start, long end) {
        BoundaryPoint startPoint = new BoundaryPoint(start, true, null);
        BoundaryPoint endPoint = new BoundaryPoint(end, false, null);

        int startIndex = getLastIndexOf(Collections.binarySearch(startPointList, startPoint), startPoint);
        final int endIndex = getLastIndexOf(Collections.binarySearch(endPointList, endPoint), endPoint);

        int pairIndex = endIndex;

        UUID uuid = startPointList.get(startIndex).getUUID();

        // Linearly searches the end points for one with the same UUID as the start point,
        // If none can be found, the next start point is checked
        while (!startPointList.get(startIndex).getUUID().equals(endPointList.get(pairIndex).getUUID())) {
            uuid = startPointList.get(startIndex).getUUID();
            boolean found = false;
            while (pairIndex >= 0 && endPointList.get(pairIndex).equals(endPoint)) {
                if (endPointList.get(pairIndex).getUUID().equals(uuid)) {
                    found = true;
                    break;
                }
                pairIndex--;
            }
            if (found) {
                break;
            }
            pairIndex = endIndex;
            startIndex--;

        }

        uuid = startPointList.get(startIndex).getUUID();
        intervalDataMap.remove(uuid);
        startPointList.remove(startIndex);
        endPointList.remove(pairIndex);

        return uuid;
    }

    public List<List<TimeSlot<T>>> getTimeSlotsAsGrid() {
        return new TimeSlotIterator<T>(startPointList, intervalDataMap).getTimeSlotsAsGrid();
    }

    public List<List<TimeSlot<T>>> getTimeSlotsAsGrid(long start, long end) {
        BoundaryPoint startPoint = new BoundaryPoint(end, true, null);
        BoundaryPoint endPoint = new BoundaryPoint(start, false, null);
        int indexOfFirstEndPointAfterStart = getFirstIndexOf(Collections.binarySearch(endPointList, endPoint), endPoint);
        int indexOfLastStartPointBeforeEnd = getLastIndexOf(Collections.binarySearch(startPointList, startPoint),
                startPoint);

        Set<UUID> endPointsAfterStartUUID = endPointList.subList(indexOfFirstEndPointAfterStart, endPointList.size())
                .stream().map((e) -> e.getUUID()).collect(Collectors.toSet());
        List<BoundaryPoint> startPointsBeforeEnd = startPointList.subList(0, indexOfLastStartPointBeforeEnd).stream()
                .filter((s) -> endPointsAfterStartUUID.contains(s.getUUID())).collect(Collectors.toList());
        return new TimeSlotIterator<T>(startPointsBeforeEnd, intervalDataMap).getTimeSlotsAsGrid();
    }

    private static final class TimeSlotIterator<T> implements Iterator<TimeSlot<T>> {

        List<BoundaryPoint> startPoints;
        Map<UUID, TimeSlot<T>> intervalData;

        int index;
        int nextDepth;

        List<TimeSlot<T>> prev;
        TimeSlot<T> next;

        public TimeSlotIterator(List<BoundaryPoint> startPoints, Map<UUID, TimeSlot<T>> intervalData) {
            this.startPoints = startPoints;
            this.intervalData = intervalData;

            index = 0;
            nextDepth = 0;
            prev = new ArrayList<>();
            next();
        }

        public List<List<TimeSlot<T>>> getTimeSlotsAsGrid() {
            List<List<TimeSlot<T>>> out = new ArrayList<>();
            while (hasNext()) {
                int depth = nextDepth;
                while (out.size() <= depth) {
                    out.add(new ArrayList<>());
                }

                TimeSlot<T> timeSlot = next();
                out.get(depth).add(timeSlot);
            }
            return out;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public TimeSlot<T> next() {
            TimeSlot<T> out = next;
            if (index < startPoints.size()) {
                BoundaryPoint startPoint = startPoints.get(index);
                next = intervalData.get(startPoint.getUUID());
                BoundaryPoint endPoint = next.getEndPoint();

                int depth;

                for (depth = 0; depth < prev.size() &&
                        startPoint.getPosition() < prev.get(depth).getEndPoint().getPosition() &&
                        endPoint.getPosition() > prev.get(depth).getStartPoint()
                                .getPosition(); depth++) {
                }

                nextDepth = depth;
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
        final T data;

        public TimeSlot(BoundaryPoint start, BoundaryPoint end, T data) {
            this.startPoint = start;
            this.endPoint = end;
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

        public UUID getUUID() {
            return startPoint.getUUID();
        }

        public T getData() {
            return data;
        }

        @Override
        public int hashCode() {
            return getUUID().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof TimeSlot)) {
                return false;
            }
            TimeSlot other = (TimeSlot) obj;

            return this.getUUID().equals(other.getUUID());
        }

    }

    public static final class BoundaryPoint implements Comparable<BoundaryPoint> {

        final long position;
        final boolean isStartOfBoundary;
        final UUID uuid;

        public BoundaryPoint(long pos, boolean isStart, UUID uuid) {
            this.position = pos;
            this.isStartOfBoundary = isStart;
            this.uuid = uuid;
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

        public UUID getUUID() {
            return uuid;
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
