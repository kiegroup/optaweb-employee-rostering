package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import java.time.LocalDateTime;
import java.util.List;

import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class SpotData implements HasTimeslot {
    private LocalDateTime start;
    private LocalDateTime end;
    private Spot spot;
    private List<Employee> employees;
    
    public SpotData(LocalDateTime start, LocalDateTime end, Spot spot, List<Employee> employees) {
        this.start = start;
        this.end = end;
        this.spot = spot;
        this.employees = employees;
    }
    
    @Override
    public String getGroupId() {
        return spot.getName();
    }
    
    public Spot getSpot() {
        return spot;
    }
    
    public List<Employee> getAssignedEmployees() {
        return employees;
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
