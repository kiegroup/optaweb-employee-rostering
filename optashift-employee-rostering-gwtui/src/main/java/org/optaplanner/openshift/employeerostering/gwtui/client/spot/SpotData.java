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

    private static Map<Long, SpotData> dataMap = new HashMap<>();

    public SpotData(Shift shift) {
        this.shift = shift;
        this.spotId = new SpotId(shift.getSpot());
        dataMap.put(shift.getId(), this);
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

    public static SpotData update(Shift shift) {
        SpotData data = dataMap.get(shift.getId());
        if (null != data) {
            data.shift = shift;
            data.spotId = new SpotId(shift.getSpot());
            return data;
        } else {
            return null;
        }
    }

    public static void remove(Shift shift) {
        dataMap.remove(shift.getId());
    }

    public static void resetData() {
        dataMap.clear();
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
