package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotData;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

public class EmployeeData implements HasTimeslot<EmployeeId> {

    Identity id;
    Shift shift;
    EmployeeId employeeId;
    private EmployeeAvailabilityView availabilityView;

    public EmployeeData(Shift shift, EmployeeAvailabilityView availabilityView) {
        this.id = new Identity(shift.getEmployee(), shift.getTimeSlot().getStartDateTime(), shift.getTimeSlot()
                .getEndDateTime());
        this.shift = shift;
        this.availabilityView = availabilityView;
        this.employeeId = new EmployeeId(shift.getEmployee());
    }

    public Employee getEmployee() {
        return shift.getEmployee();
    }

    public EmployeeAvailabilityView getAvailability() {
        return availabilityView;
    }

    public void setAvailability(EmployeeAvailabilityView availabilityView) {
        this.availabilityView = availabilityView;
    }

    public Spot getSpot() {
        return shift.getSpot();
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
    public EmployeeId getGroupId() {
        return employeeId;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof EmployeeData) {
            EmployeeData other = (EmployeeData) o;
            return this.id.equals(other.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private static final class Identity {

        final Employee employee;
        final LocalDateTime startTime;
        final LocalDateTime endTime;

        public Identity(Employee employee, LocalDateTime startTime, LocalDateTime endTime) {
            this.employee = employee;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Identity) {
                Identity other = (Identity) o;
                return employee.equals(other.employee) && startTime.equals(other.startTime) && endTime.equals(
                        other.endTime);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return employee.hashCode() ^ startTime.hashCode() ^ endTime.hashCode();
        }
    }

}
