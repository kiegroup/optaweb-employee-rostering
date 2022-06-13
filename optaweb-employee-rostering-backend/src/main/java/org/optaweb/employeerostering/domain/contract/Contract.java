package org.optaweb.employeerostering.domain.contract;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.optaweb.employeerostering.domain.common.AbstractPersistable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "tenantId", "name" }),
        @UniqueConstraint(columnNames = { "id" }) })
// TODO: Single Responsibility Principle - acts as both domain entity and JSON-serializable entity
public class Contract extends AbstractPersistable {

    @NotNull
    @Size(min = 1, max = 120)
    @Pattern(regexp = "^(?!\\s).*(?<!\\s)$", message = "Name should not contain any leading or trailing whitespaces")
    private String name;

    // Can be null
    @Min(0)
    @Max(60 * 24)
    private Integer maximumMinutesPerDay;

    // Can be null
    @Min(0)
    @Max(60 * 24 * 7)
    private Integer maximumMinutesPerWeek;

    // Can be null
    // 31 days in longest month
    @Min(0)
    @Max(60 * 24 * 31)
    private Integer maximumMinutesPerMonth;

    // Can be null
    // 366 days in leap year
    @Min(0)
    @Max(60 * 24 * 366)
    private Integer maximumMinutesPerYear;

    @SuppressWarnings("unused")
    public Contract() {
    }

    public Contract(Integer tenantId, String name) {
        this(tenantId, name, null, null, null, null);
    }

    public Contract(Integer tenantId, String name, Integer maximumMinutesPerDay, Integer maximumMinutesPerWeek,
            Integer maximumMinutesPerMonth, Integer maximumMinutesPerYear) {

        super(tenantId);
        this.name = name;
        this.maximumMinutesPerDay = maximumMinutesPerDay;
        this.maximumMinutesPerWeek = maximumMinutesPerWeek;
        this.maximumMinutesPerMonth = maximumMinutesPerMonth;
        this.maximumMinutesPerYear = maximumMinutesPerYear;
    }

    @AssertTrue
    @JsonIgnore
    public boolean isValid() {
        if (maximumMinutesPerDay != null && maximumMinutesPerDay <= 0) {
            return false;
        }
        if (maximumMinutesPerWeek != null && maximumMinutesPerWeek <= 0) {
            return false;
        }
        if (maximumMinutesPerMonth != null && maximumMinutesPerMonth <= 0) {
            return false;
        }
        if (maximumMinutesPerYear != null && maximumMinutesPerYear <= 0) {
            return false;
        }
        return true;
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
