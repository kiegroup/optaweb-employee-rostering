package org.optaplanner.openshift.employeerostering.shared.rotation.view;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;

public class RotationView {

    @NotNull
    private Integer tenantId;
    @NotNull
    private Integer rotationLength;
    @NotNull
    private List<Spot> spotList;
    @NotNull
    private List<Employee> employeeList;
    @NotNull
    private Map<Long, List<ShiftTemplateView>> spotIdToShiftTemplateViewListMap;

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getRotationLength() {
        return rotationLength;
    }

    public void setRotationLength(Integer rotationLength) {
        this.rotationLength = rotationLength;
    }

    public List<Spot> getSpotList() {
        return spotList;
    }

    public void setSpotList(List<Spot> spotList) {
        this.spotList = spotList;
    }

    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    public Map<Long, List<ShiftTemplateView>> getSpotIdToShiftTemplateViewListMap() {
        return spotIdToShiftTemplateViewListMap;
    }

    public void setSpotIdToShiftTemplateViewListMap(Map<Long, List<ShiftTemplateView>> spotIdToShiftTemplateViewListMap) {
        this.spotIdToShiftTemplateViewListMap = spotIdToShiftTemplateViewListMap;
    }
}
