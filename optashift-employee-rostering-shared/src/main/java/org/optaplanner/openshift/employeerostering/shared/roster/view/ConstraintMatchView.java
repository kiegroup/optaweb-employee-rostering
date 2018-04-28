package org.optaplanner.openshift.employeerostering.shared.roster.view;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;

public class ConstraintMatchView {

    private RosterConstraintType constraintMatched;
    private HardSoftScore score;

    public ConstraintMatchView() {}

    public ConstraintMatchView(ConstraintMatch cm) {
        constraintMatched = RosterConstraintType.forConstraintMatch(cm);
        score = (HardSoftScore) cm.getScore();
    }

    public RosterConstraintType getConstraintMatched() {
        return constraintMatched;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setConstraintMatched(RosterConstraintType constraintMatched) {
        this.constraintMatched = constraintMatched;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }
}
