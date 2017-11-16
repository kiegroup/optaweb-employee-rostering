package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.time.LocalDateTime;
import java.util.List;

import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class EmployeeData implements HasTimeslot{
    private LocalDateTime start;
    private LocalDateTime end;
    private Employee employee;
    private EmployeeAvailabilityView availabilityView;
    private List<Spot> spots;
    
    public EmployeeData(LocalDateTime start, LocalDateTime end, Employee employee,
            EmployeeAvailabilityView availabilityView, List<Spot> spots) {
        this.start = start;
        this.end = end;
        this.employee = employee;
        this.availabilityView = availabilityView;
        this.spots = spots;
    }

    @Override
    public String getGroupId() {
        return employee.getName();
    }
    
    public Employee getEmployee() {
        return employee;
    }
    
    public EmployeeAvailabilityView getAvailability() {
        return availabilityView;
    }
    
    public List<Spot> getSpots() {
        return spots;
    }

    @Override
    public LocalDateTime getStartTime() {
        return start;
    }

    @Override
    public LocalDateTime getEndTime() {
        return end;
    }
    
    
}
