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

    Identity id;
    private Shift shift;
    private SpotId spotId;

    private static Map<Identity, SpotData> dataMap = new HashMap<>();

    public SpotData(Shift shift) {
        this.id = new Identity(shift.getSpot(), shift.getTimeSlot().getStartDateTime(), shift.getTimeSlot()
                .getEndDateTime());
        this.shift = shift;
        this.spotId = new SpotId(shift.getSpot());
        dataMap.put(id, this);
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

    public static boolean update(Shift shift) {
        Identity id = new Identity(shift.getSpot(), shift.getTimeSlot().getStartDateTime(), shift.getTimeSlot()
                .getEndDateTime());
        SpotData data = dataMap.get(id);
        if (null != data) {
            data.shift = shift;
            data.spotId = new SpotId(shift.getSpot());
            return true;
        } else {
            return false;
        }
    }

    public static void resetData() {
        dataMap.clear();
    }

    private static final class Identity {

        final Spot spot;
        final LocalDateTime startTime;
        final LocalDateTime endTime;

        public Identity(Spot spot, LocalDateTime startTime, LocalDateTime endTime) {
            this.spot = spot;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Identity) {
                Identity other = (Identity) o;
                return spot.equals(other.spot) && startTime.equals(other.startTime) && endTime.equals(
                        other.endTime);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return spot.hashCode() ^ startTime.hashCode() ^ endTime.hashCode();
        }
    }

}
