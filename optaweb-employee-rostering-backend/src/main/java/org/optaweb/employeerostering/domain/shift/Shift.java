package org.optaweb.employeerostering.domain.shift;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@PlanningEntity(pinningFilter = PinningShiftFilter.class)
public class Shift extends AbstractPersistable {
    @Transient
    private final AtomicLong lengthInMinutes = new AtomicLong(-1);
    @ManyToOne
    private Employee rotationEmployee;
    @NotNull
    @ManyToOne
    private Spot spot;
    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "ShiftRequiredSkillSet",
            joinColumns = @JoinColumn(name = "shiftId", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "skillId", referencedColumnName = "id"))
    private Set<Skill> requiredSkillSet;
    @NotNull
    private OffsetDateTime startDateTime;
    @NotNull
    private OffsetDateTime endDateTime;
    @PlanningPin
    private boolean pinnedByUser = false;

    @ManyToOne
    @PlanningVariable(valueRangeProviderRefs = "employeeRange", nullable = true)
    private Employee employee = null;

    @ManyToOne
    private Employee originalEmployee = null;

    @SuppressWarnings("unused")
    public Shift() {
    }

    public Shift(Integer tenantId, Spot spot, OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        this(tenantId, spot, startDateTime, endDateTime, null);
    }

    public Shift(Integer tenantId, Spot spot, OffsetDateTime startDateTime, OffsetDateTime endDateTime,
            Employee rotationEmployee) {
        this(tenantId, spot, startDateTime, endDateTime, rotationEmployee, new HashSet<>(), null);
    }

    public Shift(Integer tenantId, Spot spot, OffsetDateTime startDateTime, OffsetDateTime endDateTime,
            Employee rotationEmployee, Set<Skill> requiredSkillSet, Employee originalEmployee) {
        super(tenantId);
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.spot = spot;
        this.rotationEmployee = rotationEmployee;
        this.requiredSkillSet = requiredSkillSet;
        this.originalEmployee = originalEmployee;
    }

    public Shift(ZoneId zoneId, ShiftView shiftView, Spot spot) {
        this(zoneId, shiftView, spot, null);
    }

    public Shift(ZoneId zoneId, ShiftView shiftView, Spot spot, Employee rotationEmployee) {
        this(zoneId, shiftView, spot, rotationEmployee, new HashSet<>(), null);
    }

    public Shift(ZoneId zoneId, ShiftView shiftView, Spot spot, Employee rotationEmployee,
            Set<Skill> requiredSkillSet, Employee originalEmployee) {
        super(shiftView);
        this.startDateTime = OffsetDateTime.of(shiftView.getStartDateTime(),
                zoneId.getRules().getOffset(shiftView.getStartDateTime()));
        this.endDateTime = OffsetDateTime.of(shiftView.getEndDateTime(),
                zoneId.getRules().getOffset(shiftView.getEndDateTime()));
        this.spot = spot;
        this.pinnedByUser = shiftView.isPinnedByUser();
        this.rotationEmployee = rotationEmployee;
        this.requiredSkillSet = requiredSkillSet;
        this.originalEmployee = originalEmployee;
    }

    @AssertTrue(message = "Shift's end date time is not at least 30 minutes" +
            " after shift's start date time")
    @JsonIgnore
    public boolean isValid() {
        return startDateTime != null && endDateTime != null &&
                (Duration.between(startDateTime, endDateTime).getSeconds() / 60) >= 30;
    }

    @Override
    public String toString() {
        return spot + " " + startDateTime + "-" + endDateTime;
    }

    public boolean precedes(Shift other) {
        return !endDateTime.isAfter(other.startDateTime);
    }

    public long getLengthInMinutes() { // Thread-safe cache.
        long currentLengthInMinutes = lengthInMinutes.get();
        if (currentLengthInMinutes >= 0) {
            return currentLengthInMinutes;
        }
        long newLengthInMinutes = startDateTime.until(endDateTime, ChronoUnit.MINUTES);
        lengthInMinutes.set(newLengthInMinutes);
        return newLengthInMinutes;
    }

    @JsonIgnore
    public boolean isMoved() {
        return originalEmployee != null && originalEmployee != employee;
    }

    public boolean hasRequiredSkills() {
        return employee.getSkillProficiencySet().containsAll(spot.getRequiredSkillSet()) &&
                employee.getSkillProficiencySet().containsAll(requiredSkillSet);
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Spot getSpot() {
        return spot;
    }

    public void setSpot(Spot spot) {
        this.spot = spot;
    }

    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(OffsetDateTime startDateTime) {
        this.startDateTime = startDateTime;
        this.lengthInMinutes.set(-1);
    }

    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(OffsetDateTime endDateTime) {
        this.endDateTime = endDateTime;
        this.lengthInMinutes.set(-1);
    }

    public boolean isPinnedByUser() {
        return pinnedByUser;
    }

    public void setPinnedByUser(boolean lockedByUser) {
        this.pinnedByUser = lockedByUser;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Employee getRotationEmployee() {
        return rotationEmployee;
    }

    public void setRotationEmployee(Employee rotationEmployee) {
        this.rotationEmployee = rotationEmployee;
    }

    public Employee getOriginalEmployee() {
        return originalEmployee;
    }

    public void setOriginalEmployee(Employee originalEmployee) {
        this.originalEmployee = originalEmployee;
    }

    public Set<Skill> getRequiredSkillSet() {
        return requiredSkillSet;
    }

    public void setRequiredSkillSet(Set<Skill> requiredSkillSet) {
        this.requiredSkillSet = requiredSkillSet;
    }

    public Shift inTimeZone(ZoneId zoneId) {
        Shift out = new Shift(zoneId, new ShiftView(zoneId, this), getSpot(), getRotationEmployee(),
                getRequiredSkillSet(), getOriginalEmployee());
        out.setEmployee(getEmployee());
        return out;
    }
}
