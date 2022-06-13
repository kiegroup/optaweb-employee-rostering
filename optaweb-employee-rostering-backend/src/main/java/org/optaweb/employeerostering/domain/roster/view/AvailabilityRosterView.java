package org.optaweb.employeerostering.domain.roster.view;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.optaweb.employeerostering.domain.employee.view.EmployeeAvailabilityView;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;

public class AvailabilityRosterView extends AbstractRosterView {

    @NotNull
    // The list in each entry is sorted by startTime
    private Map<Long, List<ShiftView>> employeeIdToShiftViewListMap;
    @NotNull
    // The list in each entry is sorted by startTime
    private Map<Long, List<EmployeeAvailabilityView>> employeeIdToAvailabilityViewListMap;
    @NotNull
    // The list in each entry is sorted by startTime
    private List<ShiftView> unassignedShiftViewList;

    @SuppressWarnings("unused")
    public AvailabilityRosterView() {
    }

    public AvailabilityRosterView(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public AvailabilityRosterView(Integer tenantId, LocalDate startDate, LocalDate endDate) {
        this(tenantId);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Map<Long, List<ShiftView>> getEmployeeIdToShiftViewListMap() {
        return employeeIdToShiftViewListMap;
    }

    public void setEmployeeIdToShiftViewListMap(Map<Long, List<ShiftView>> employeeIdToShiftViewListMap) {
        this.employeeIdToShiftViewListMap = employeeIdToShiftViewListMap;
    }

    public Map<Long, List<EmployeeAvailabilityView>> getEmployeeIdToAvailabilityViewListMap() {
        return employeeIdToAvailabilityViewListMap;
    }

    public void setEmployeeIdToAvailabilityViewListMap(
            Map<Long, List<EmployeeAvailabilityView>> employeeIdToAvailabilityViewListMap) {
        this.employeeIdToAvailabilityViewListMap = employeeIdToAvailabilityViewListMap;
    }

    public List<ShiftView> getUnassignedShiftViewList() {
        return unassignedShiftViewList;
    }

    public void setUnassignedShiftViewList(List<ShiftView> unassignedShiftViewList) {
        this.unassignedShiftViewList = unassignedShiftViewList;
    }

}
