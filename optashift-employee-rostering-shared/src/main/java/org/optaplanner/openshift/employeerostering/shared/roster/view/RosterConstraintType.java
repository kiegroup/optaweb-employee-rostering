package org.optaplanner.openshift.employeerostering.shared.roster.view;

import java.util.Arrays;
import java.util.Optional;

import org.optaplanner.core.api.score.constraint.ConstraintMatch;

public enum RosterConstraintType {
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

    private RosterConstraintType(String constraintName) {
        this(DEFAULT_PACKAGE, constraintName);
    }

    private RosterConstraintType(String constraintPackage, String constraintName) {
        this.constraintPackage = constraintPackage;
        this.constraintName = constraintName;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public String getConstraintPackage() {
        return constraintPackage;
    }

    public static RosterConstraintType forConstraintMatch(ConstraintMatch cm) {
        Optional<RosterConstraintType> constraint = Arrays.asList(RosterConstraintType.values()).stream()
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
