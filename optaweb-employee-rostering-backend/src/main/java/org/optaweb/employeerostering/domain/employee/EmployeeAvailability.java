package org.optaweb.employeerostering.domain.employee;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.employee.view.EmployeeAvailabilityView;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "tenantId", "employee_id", "startDateTime", "endDateTime" }))
// TODO: Single Responsibility Principle - acts as both domain entity and JSON-serializable entity
public class EmployeeAvailability extends AbstractPersistable {

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    private Employee employee;

    @NotNull
    private OffsetDateTime startDateTime;
    @NotNull
    private OffsetDateTime endDateTime;

    @NotNull
    private EmployeeAvailabilityState state;

    @SuppressWarnings("unused")
    public EmployeeAvailability() {
    }

    public EmployeeAvailability(Integer tenantId, Employee employee, OffsetDateTime startDateTime,
            OffsetDateTime endDateTime) {
        super(tenantId);
        this.employee = employee;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public EmployeeAvailability(ZoneId zoneId, EmployeeAvailabilityView employeeAvailabilityView, Employee employee) {
        super(employeeAvailabilityView);
        this.employee = employee;
        this.startDateTime = OffsetDateTime.of(employeeAvailabilityView.getStartDateTime(),
                zoneId.getRules().getOffset(employeeAvailabilityView.getStartDateTime()));
        this.endDateTime = OffsetDateTime.of(employeeAvailabilityView.getEndDateTime(),
                zoneId.getRules().getOffset(employeeAvailabilityView.getEndDateTime()));
        this.state = employeeAvailabilityView.getState();
    }

    @AssertTrue
    @JsonIgnore
    public boolean isValid() {
        return getDuration().getSeconds() / (60 * 60) < 28;
    }

    @JsonIgnore
    public Duration getDuration() {
        return Duration.between(startDateTime, endDateTime);
    }

    @Override
    public String toString() {
        return employee + ":" + startDateTime + "-" + endDateTime;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(OffsetDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(OffsetDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public EmployeeAvailabilityState getState() {
        return state;
    }

    public void setState(EmployeeAvailabilityState state) {
        this.state = state;
    }

    public EmployeeAvailability inTimeZone(ZoneId zoneId) {
        return new EmployeeAvailability(zoneId, new EmployeeAvailabilityView(zoneId, this), getEmployee());
    }
}
