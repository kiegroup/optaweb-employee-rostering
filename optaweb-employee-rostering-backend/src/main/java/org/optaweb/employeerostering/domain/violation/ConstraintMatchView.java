package org.optaweb.employeerostering.domain.violation;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;

public interface ConstraintMatchView {

    HardMediumSoftLongScore getScore();
}
