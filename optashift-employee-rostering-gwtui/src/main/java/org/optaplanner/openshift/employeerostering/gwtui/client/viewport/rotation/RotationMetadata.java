package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.rotation;

import java.util.Map;

import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class RotationMetadata {

    private final Map<Long, Spot> spotIdToSpotMap;
    private final Map<Long, Employee> employeeIdToEmployeeMap;

    public RotationMetadata(Map<Long, Spot> spotIdToSpotMap, Map<Long, Employee> employeeIdToEmployeeMap) {
        super();
        this.spotIdToSpotMap = spotIdToSpotMap;
        this.employeeIdToEmployeeMap = employeeIdToEmployeeMap;
    }

    public Map<Long, Spot> getSpotIdToSpotMap() {
        return spotIdToSpotMap;
    }

    public Map<Long, Employee> getEmployeeIdToEmployeeMap() {
        return employeeIdToEmployeeMap;
    }
}
