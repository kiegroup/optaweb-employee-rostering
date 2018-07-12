package org.optaplanner.openshift.employeerostering.shared.violation;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;

public class ShiftEmployeeConflict implements ConstraintMatchView {

    private Shift leftShift;
    private Shift rightShift;

    private HardMediumSoftLongScore score;

    public ShiftEmployeeConflict() {

    }

    public ShiftEmployeeConflict(Shift leftShift, Shift rightShift, HardMediumSoftLongScore score) {
        this.leftShift = leftShift;
        this.rightShift = rightShift;
        this.score = score;
    }

    public Shift getLeftShift() {
        return leftShift;
    }

    public void setLeftShift(Shift leftShift) {
        this.leftShift = leftShift;
    }

    public Shift getRightShift() {
        return rightShift;
    }

    public void setRightShift(Shift rightShift) {
        this.rightShift = rightShift;
    }

    public HardMediumSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftLongScore score) {
        this.score = score;
    }
}
