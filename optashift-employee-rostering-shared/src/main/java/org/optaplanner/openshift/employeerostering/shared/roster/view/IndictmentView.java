package org.optaplanner.openshift.employeerostering.shared.roster.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.persistence.jackson.api.score.ScoreJacksonJsonSerializer;
import org.optaplanner.persistence.jackson.api.score.buildin.hardsoft.HardSoftScoreJacksonJsonDeserializer;

public class IndictmentView {

    private List<Constraint> constraintMatchedList;
    private List<HardSoftScore> scoreList;

    public IndictmentView() {

    }

    public IndictmentView(Indictment indictment) {
        constraintMatchedList = new ArrayList<>(indictment.getConstraintMatchCount());
        scoreList = new ArrayList<>(indictment.getConstraintMatchCount());

        for (ConstraintMatch cm : indictment.getConstraintMatchSet()) {
            constraintMatchedList.add(Constraint.forConstraintMatch(cm));
            scoreList.add((HardSoftScore) cm.getScore());
        }
    }

    public IndictmentView(List<Constraint> ruleMatched, List<HardSoftScore> score) {
        this.constraintMatchedList = ruleMatched;
        this.scoreList = score;
    }

    public List<Constraint> getConstraintMatchedList() {
        return constraintMatchedList;
    }

    @JsonSerialize(contentUsing = ScoreJacksonJsonSerializer.class)
    @JsonDeserialize(contentUsing = HardSoftScoreJacksonJsonDeserializer.class)
    public List<HardSoftScore> getScoreList() {
        return scoreList;
    }

    public void setConstraintMatchedList(List<Constraint> constraintMatchedList) {
        this.constraintMatchedList = constraintMatchedList;
    }

    public void setScoreList(List<HardSoftScore> scoreList) {
        this.scoreList = scoreList;
    }

    public static enum Constraint {
        REQUIRED_SKILL_FOR_A_SHIFT("Required skill for a shift"),
        AT_MOST_ONE_SHIFT_ASSIGNMENT_PER_DAY_PER_EMPLOYEE("At most one shift assignment per day per employee"),
        NO_2_SHIFTS_WITHIN_10_HOURS_FROM_EACH_OTHER("No 2 shifts within 10 hours from each other"),
        UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE("Undesired time slot for an employee"),
        DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE("Desired time slot for an employee"),
        UNAVALIABLE_TIMESLOT_FOR_EMPLOYEE("Unavailable time slot for an employee"),
        EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE("Employee is not rotation employee");

        private final String constraintPackage;
        private final String constraintName;
        private static final String DEFAULT_PACKAGE = "org.optaplanner.openshift.employeerostering.server.solver";

        private Constraint(String constraintName) {
            this(DEFAULT_PACKAGE, constraintName);
        }

        private Constraint(String constraintPackage, String constraintName) {
            this.constraintPackage = constraintPackage;
            this.constraintName = constraintName;
        }

        public String getConstraintName() {
            return constraintName;
        }

        public String getConstraintPackage() {
            return constraintPackage;
        }

        public static Constraint forConstraintMatch(ConstraintMatch cm) {
            Optional<Constraint> constraint = Arrays.asList(Constraint.values()).stream()
                    .filter((c) -> c.getConstraintPackage().equals(cm.getConstraintPackage()) &&
                            c.getConstraintName().equals(cm.getConstraintName()))
                    .findAny();
            if (constraint.isPresent()) {
                return constraint.get();
            } else {
                throw new IllegalArgumentException("No constraint has constraintPackage [" + cm.getConstraintPackage() + "]" + " and constraintName [" + cm.getConstraintName() + "]");
            }
        }
    }
}
