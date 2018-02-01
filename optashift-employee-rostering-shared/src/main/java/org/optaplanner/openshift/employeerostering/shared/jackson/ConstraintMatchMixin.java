package org.optaplanner.openshift.employeerostering.shared.jackson;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.optaplanner.core.api.score.Score;

public abstract class ConstraintMatchMixin {

    @JsonCreator
    public ConstraintMatchMixin(@JsonProperty("constraintPackage") String constraintPackage,
            @JsonProperty("constraintName") String constraintName,
            @JsonProperty("justificationList") List<Object> justificationList, @JsonProperty("score") Score score) {
    }

    @JsonIgnore
    abstract public List<Object> getJustificationList();
}
