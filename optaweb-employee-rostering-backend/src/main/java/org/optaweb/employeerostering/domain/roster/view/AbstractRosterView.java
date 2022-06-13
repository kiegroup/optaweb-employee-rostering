package org.optaweb.employeerostering.domain.roster.view;

import java.time.LocalDate;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.roster.RosterState;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.violation.IndictmentSummary;

public class AbstractRosterView {

    @NotNull
    protected Integer tenantId;
    @NotNull
    protected LocalDate startDate; // inclusive
    @NotNull
    protected LocalDate endDate; // inclusive
    @NotNull
    protected List<Spot> spotList;
    @NotNull
    protected List<Employee> employeeList;
    @NotNull
    protected RosterState rosterState;

    private IndictmentSummary indictmentSummary;

    private HardMediumSoftLongScore score = null;

    @Override
    public String toString() {
        return startDate + " to " + endDate;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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

    public HardMediumSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftLongScore score) {
        this.score = score;
    }

    public IndictmentSummary getIndictmentSummary() {
        return indictmentSummary;
    }

    public void setIndictmentSummary(IndictmentSummary indictmentSummary) {
        this.indictmentSummary = indictmentSummary;
    }

    public RosterState getRosterState() {
        return rosterState;
    }

    public void setRosterState(RosterState rosterState) {
        this.rosterState = rosterState;
    }
}
