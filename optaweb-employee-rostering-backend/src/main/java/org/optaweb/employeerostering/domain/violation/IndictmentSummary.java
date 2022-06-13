package org.optaweb.employeerostering.domain.violation;

import java.util.Map;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;

public class IndictmentSummary {
    private Map<String, Integer> constraintToCountMap;

    private Map<String, HardMediumSoftLongScore> constraintToScoreImpactMap;

    public Map<String, Integer> getConstraintToCountMap() {
        return constraintToCountMap;
    }

    public void setConstraintToCountMap(Map<String, Integer> constraintToCountMap) {
        this.constraintToCountMap = constraintToCountMap;
    }

    public Map<String, HardMediumSoftLongScore> getConstraintToScoreImpactMap() {
        return constraintToScoreImpactMap;
    }

    public void setConstraintToScoreImpactMap(Map<String, HardMediumSoftLongScore> constraintToScoreImpactMap) {
        this.constraintToScoreImpactMap = constraintToScoreImpactMap;
    }
}
