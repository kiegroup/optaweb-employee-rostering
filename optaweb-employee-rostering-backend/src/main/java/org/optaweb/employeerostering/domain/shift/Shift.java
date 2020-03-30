/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.domain.shift;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;
import org.optaweb.employeerostering.domain.spot.Spot;

@Entity
@PlanningEntity(movableEntitySelectionFilter = MovableShiftFilter.class)
public class Shift extends AbstractPersistable {

    @ManyToOne
    private Employee rotationEmployee;

    @NotNull
    @ManyToOne
    private Spot spot;

    @NotNull
    private OffsetDateTime startDateTime;
    @NotNull
    private OffsetDateTime endDateTime;
    @Transient
    private Long length = null;

    @PlanningPin
    private boolean pinnedByUser = false;

    @ManyToOne
    @PlanningVariable(valueRangeProviderRefs = "employeeRange", nullable = true)
    private Employee employee = null;

    @SuppressWarnings("unused")
    public Shift() {
    }

    public Shift(Integer tenantId, Spot spot, OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        this(tenantId, spot, startDateTime, endDateTime, null);
    }

    public Shift(Integer tenantId, Spot spot, OffsetDateTime startDateTime, OffsetDateTime endDateTime,
            Employee rotationEmployee) {
        super(tenantId);
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.spot = spot;
        this.rotationEmployee = rotationEmployee;
    }

    public Shift(ZoneId zoneId, ShiftView shiftView, Spot spot) {
        this(zoneId, shiftView, spot, null);
    }

    public Shift(ZoneId zoneId, ShiftView shiftView, Spot spot, Employee rotationEmployee) {
        super(shiftView);
        this.startDateTime = OffsetDateTime.of(shiftView.getStartDateTime(),
                zoneId.getRules().getOffset(shiftView.getStartDateTime()));
        this.endDateTime = OffsetDateTime.of(shiftView.getEndDateTime(),
                zoneId.getRules().getOffset(shiftView.getEndDateTime()));
        this.spot = spot;
        this.pinnedByUser = shiftView.isPinnedByUser();
        this.rotationEmployee = rotationEmployee;
    }

    @Override
    public String toString() {
        return spot + " " + startDateTime + "-" + endDateTime;
    }

    public boolean follows(Shift other) {
        return !startDateTime.isBefore(other.endDateTime);
    }

    public boolean precedes(Shift other) {
        return !endDateTime.isAfter(other.startDateTime);
    }

    public long getLength(ChronoUnit unit) {
        if (length == null) {
            length = startDateTime.until(endDateTime, unit);
        }
        return length;
    }

    public static long calculateLoad(Collection<Integer> hourlyCounts) {
        long sumSquares = 0;
        for (int hourlyCount: hourlyCounts) {
            sumSquares += hourlyCount * hourlyCount;
        }
        long squareRootOfSums = Math.round(Math.sqrt(sumSquares) * 1000);
        return squareRootOfSums;
    }

    private void adjustHourlyCounts(Map<OffsetDateTime, Integer> hourlyCountsMap,
            Function<Integer, Integer> countAdjuster) {
        long hourCount = getLength(ChronoUnit.HOURS);
        OffsetDateTime baseStartDateTime = startDateTime.truncatedTo(ChronoUnit.HOURS);
        for (int hour = 0; hour < hourCount; hour++) {
            OffsetDateTime actualHour = baseStartDateTime.plusHours(hour);
            hourlyCountsMap.compute(actualHour, (k, count) -> countAdjuster.apply(count));
        }
    }

    public void increaseHourlyCounts(Map<OffsetDateTime, Integer> hourlyCountsMap) {
        adjustHourlyCounts(hourlyCountsMap, count -> {
            if (count == null) {
                return 1;
            } else {
                return count + 1;
            }
        });
    }

    public void decreaseHourlyCounts(Map<OffsetDateTime, Integer> hourlyCountsMap) {
        adjustHourlyCounts(hourlyCountsMap, count -> {
            if (count < 2) {
                return null;
            } else {
                return count - 1;
            }
        });
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Spot getSpot() {
        return spot;
    }

    public void setSpot(Spot spot) {
        this.spot = spot;
    }

    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(OffsetDateTime startDateTime) {
        this.startDateTime = startDateTime;
        this.length = null;
    }

    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(OffsetDateTime endDateTime) {
        this.endDateTime = endDateTime;
        this.length = null;
    }

    public boolean isPinnedByUser() {
        return pinnedByUser;
    }

    public void setPinnedByUser(boolean lockedByUser) {
        this.pinnedByUser = lockedByUser;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Employee getRotationEmployee() {
        return rotationEmployee;
    }

    public void setRotationEmployee(Employee rotationEmployee) {
        this.rotationEmployee = rotationEmployee;
    }

    public Shift inTimeZone(ZoneId zoneId) {
        Shift out = new Shift(zoneId, new ShiftView(zoneId, this), getSpot(), getRotationEmployee());
        out.setEmployee(getEmployee());
        return out;
    }
}
