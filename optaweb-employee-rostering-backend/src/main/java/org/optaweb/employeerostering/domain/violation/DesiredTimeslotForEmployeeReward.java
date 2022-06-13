package org.optaweb.employeerostering.domain.violation;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.shift.Shift;

public class DesiredTimeslotForEmployeeReward implements ConstraintMatchView {

    private EmployeeAvailability employeeAvailability;
    private Shift shift;

    private HardMediumSoftLongScore score;

    public DesiredTimeslotForEmployeeReward() {

    }

    public DesiredTimeslotForEmployeeReward(Shift shift, EmployeeAvailability employeeAvailability,
            HardMediumSoftLongScore score) {
        this.shift = shift;
        this.employeeAvailability = employeeAvailability;
        this.score = score;
    }

    public EmployeeAvailability getEmployeeAvailability() {
        return employeeAvailability;
    }

    public void setEmployeeAvailability(EmployeeAvailability employeeAvailability) {
        this.employeeAvailability = employeeAvailability;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    @Override
    public HardMediumSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftLongScore score) {
        this.score = score;
    }
}
