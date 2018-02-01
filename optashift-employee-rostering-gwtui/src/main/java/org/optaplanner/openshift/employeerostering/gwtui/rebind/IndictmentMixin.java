package org.optaplanner.openshift.employeerostering.gwtui.rebind;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.optaplanner.core.api.score.Score;

public abstract class IndictmentMixin {

    @JsonCreator
    public IndictmentMixin(@JsonProperty("justification") Object justification,
            @JsonProperty("scoreTotal") Score zeroScore) {
    }

    @JsonIgnore
    public abstract int getConstraintMatchCount();

    @JsonIgnore
    public abstract Object getJustification();
}
