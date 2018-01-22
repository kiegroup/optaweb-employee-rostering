package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotData;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotId;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.IdOrGroup;
import org.optaplanner.openshift.employeerostering.shared.lang.tokens.ShiftInfo;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;

public class ShiftData extends ShiftInfo implements HasTimeslot<SpotId> {

    SpotData shift;

    public ShiftData(SpotData shift) {
        super();
        this.setStartTime(shift.getStartTime());
        this.setEndTime(shift.getEndTime());
        this.setSpotList(Arrays.asList(new IdOrGroup(shift.getSpot().getTenantId(), false, shift.getSpot().getId())));
        this.shift = shift;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        out.append(shift.getGroupId());
        out.append(':');

        out.append(shift.getStartTime().toString());
        out.append("-");
        out.append(shift.getEndTime().toString());

        out.append(':');
        return out.toString();
    }

    @Override
    public SpotId getGroupId() {
        return shift.getGroupId();
    }

    @Override
    public LocalDateTime getStartTime() {
        return shift.getStartTime();
    }

    @Override
    public LocalDateTime getEndTime() {
        return shift.getEndTime();
    }

    public Shift getShift() {
        return shift.getShift();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ShiftData) {
            ShiftData other = (ShiftData) o;
            return this.shift.equals(other.shift);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return shift.hashCode();
    }
}