package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.time.LocalDateTime;

import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class EmployeeData implements HasTimeslot<EmployeeId> {

    Shift shift;
    EmployeeId employeeId;
    private EmployeeAvailabilityView availabilityView;

    public EmployeeData(Shift shift, EmployeeAvailabilityView availabilityView) {
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

}
