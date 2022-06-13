package org.optaweb.employeerostering.domain.violation;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.domain.shift.Shift;

public class PublishedShiftReassignedPenalty implements ConstraintMatchView {

    private Shift shift;

    private HardMediumSoftLongScore score;

    public PublishedShiftReassignedPenalty() {

    }

    public PublishedShiftReassignedPenalty(Shift shift, HardMediumSoftLongScore score) {
        this.shift = shift;
        this.score = score;
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
