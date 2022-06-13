package org.optaweb.employeerostering.domain.contract.view;

import org.optaweb.employeerostering.domain.common.AbstractPersistable;

public class ContractView extends AbstractPersistable {

    private String name;

    private Integer maximumMinutesPerDay;

    private Integer maximumMinutesPerWeek;

    private Integer maximumMinutesPerMonth;

    private Integer maximumMinutesPerYear;

    @SuppressWarnings("unused")
    public ContractView() {
    }

    public ContractView(Integer tenantId, String name) {
        this(tenantId, name, null, null, null, null);
    }

    public ContractView(Integer tenantId, String name, Integer maximumMinutesPerDay, Integer maximumMinutesPerWeek,
            Integer maximumMinutesPerMonth, Integer maximumMinutesPerYear) {
        super(tenantId);
        this.name = name;
        this.maximumMinutesPerDay = maximumMinutesPerDay;
        this.maximumMinutesPerWeek = maximumMinutesPerWeek;
        this.maximumMinutesPerMonth = maximumMinutesPerMonth;
        this.maximumMinutesPerYear = maximumMinutesPerYear;
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaximumMinutesPerDay() {
        return maximumMinutesPerDay;
    }

    public void setMaximumMinutesPerDay(Integer maximumMinutesPerDay) {
        this.maximumMinutesPerDay = maximumMinutesPerDay;
    }

    public Integer getMaximumMinutesPerWeek() {
        return maximumMinutesPerWeek;
    }

    public void setMaximumMinutesPerWeek(Integer maximumMinutesPerWeek) {
        this.maximumMinutesPerWeek = maximumMinutesPerWeek;
    }

    public Integer getMaximumMinutesPerMonth() {
        return maximumMinutesPerMonth;
    }

    public void setMaximumMinutesPerMonth(Integer maximumMinutesPerMonth) {
        this.maximumMinutesPerMonth = maximumMinutesPerMonth;
    }

    public Integer getMaximumMinutesPerYear() {
        return maximumMinutesPerYear;
    }

    public void setMaximumMinutesPerYear(Integer maximumMinutesPerYear) {
        this.maximumMinutesPerYear = maximumMinutesPerYear;
    }
}
