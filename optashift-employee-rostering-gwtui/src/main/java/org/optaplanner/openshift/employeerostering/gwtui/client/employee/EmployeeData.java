package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.time.LocalDateTime;

import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class EmployeeData implements HasTimeslot{
    Shift shift;
    private EmployeeAvailabilityView availabilityView;
    
    public EmployeeData(Shift shift, EmployeeAvailabilityView availabilityView) {
        this.shift = shift;
        this.availabilityView = availabilityView;
    }

    @Override
    public String getGroupId() {
        return shift.getEmployee().getName();
    }
    
    public Employee getEmployee() {
        return shift.getEmployee();
    }
    
    public EmployeeAvailabilityView getAvailability() {
        return availabilityView;
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
    
    
}
