package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class SpotData implements HasTimeslot<SpotId> {

    private Shift shift;
    private SpotId spotId;

    public SpotData(Shift shift) {
        this.shift = shift;
        this.spotId = new SpotId(shift.getSpot());
    }

    public Spot getSpot() {
        return shift.getSpot();
    }

    public Employee getAssignedEmployee() {
        return shift.getEmployee();
    }

    public boolean isLocked() {
        return shift.isLockedByUser();
    }

    public Shift getShift() {
        return shift;
    }

    @Override
    public LocalDateTime getStartTime() {
        return shift.getTimeSlot().getStartDateTime();
    }

    @Override
    public LocalDateTime getEndTime() {
        return shift.getTimeSlot().getEndDateTime();
    }

    @Override
    public SpotId getGroupId() {
        return spotId;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SpotData) {
            SpotData other = (SpotData) o;
            return this.shift.equals(other.shift);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return shift.hashCode();
    }

}
