package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.employeeroster;

import java.util.Map;

import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterState;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class EmployeeRosterMetadata {

    private final RosterState rosterState;
    private final Map<Long, Spot> spotIdToSpotMap;
    private final Map<Long, Employee> employeeIdToEmployeeMap;

    public EmployeeRosterMetadata(RosterState rosterState, Map<Long, Spot> spotIdToSpotMap, Map<Long, Employee> employeeIdToEmployeeMap) {
        this.rosterState = rosterState;
        this.spotIdToSpotMap = spotIdToSpotMap;
        this.employeeIdToEmployeeMap = employeeIdToEmployeeMap;
    }

    public RosterState getRosterState() {
        return rosterState;
    }

    public Map<Long, Spot> getSpotIdToSpotMap() {
        return spotIdToSpotMap;
    }

    public Map<Long, Employee> getEmployeeIdToEmployeeMap() {
        return employeeIdToEmployeeMap;
    }
}
