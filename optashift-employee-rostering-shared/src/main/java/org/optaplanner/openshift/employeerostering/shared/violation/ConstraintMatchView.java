package org.optaplanner.openshift.employeerostering.shared.violation;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;

public interface ConstraintMatchView {

    HardMediumSoftLongScore getScore();
}
