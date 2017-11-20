package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import java.time.LocalDateTime;

import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class SpotData implements HasTimeslot {
    private Shift shift;
    
    public SpotData(Shift shift) {
        this.shift = shift;
    }
    
    @Override
    public String getGroupId() {
        return shift.getSpot().getName();
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

}
