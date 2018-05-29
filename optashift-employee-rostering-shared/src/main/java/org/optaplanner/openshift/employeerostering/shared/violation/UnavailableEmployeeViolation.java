package org.optaplanner.openshift.employeerostering.shared.violation;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;

public class UnavailableEmployeeViolation implements ConstraintMatchView {

    private EmployeeAvailability employeeAvailability;
    private Shift shift;

    private HardMediumSoftLongScore score;

    public UnavailableEmployeeViolation() {

    }

    public UnavailableEmployeeViolation(Shift shift, EmployeeAvailability employeeAvaliability, HardMediumSoftLongScore score) {
        this.shift = shift;
        this.employeeAvailability = employeeAvaliability;
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

    public HardMediumSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftLongScore score) {
        this.score = score;
    }
}
