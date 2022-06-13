package org.optaweb.employeerostering.domain.violation;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.domain.shift.Shift;

public class NoBreakViolation implements ConstraintMatchView {

    private Shift firstShift;
    private Shift secondShift;
    private Shift thirdShift;

    private HardMediumSoftLongScore score;

    public NoBreakViolation() {

    }

    public NoBreakViolation(Shift firstShift, Shift secondShift, Shift thirdShift, HardMediumSoftLongScore score) {
        this.firstShift = firstShift;
        this.secondShift = secondShift;
        this.thirdShift = thirdShift;
        this.score = score;
    }

    public Shift getFirstShift() {
        return firstShift;
    }

    public void setFirstShift(Shift firstShift) {
        this.firstShift = firstShift;
    }

    public Shift getSecondShift() {
        return secondShift;
    }

    public void setSecondShift(Shift secondShift) {
        this.secondShift = secondShift;
    }

    public Shift getThirdShift() {
        return thirdShift;
    }

    public void setThirdShift(Shift thirdShift) {
        this.thirdShift = thirdShift;
    }

    @Override
    public HardMediumSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftLongScore score) {
        this.score = score;
    }

}
