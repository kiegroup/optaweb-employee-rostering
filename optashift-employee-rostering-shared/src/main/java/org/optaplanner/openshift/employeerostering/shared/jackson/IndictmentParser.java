package org.optaplanner.openshift.employeerostering.shared.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.Indictment;

public class IndictmentParser {

    public static Indictment parse(String jsonString) throws JsonProcessingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(jsonString);
        
        //Properties: justification, constraintMatchSet, scoreTotal
        Object justification
    }

    private static Score parseScore(String jsonString) {

    }

    private static ConstraintMatch parseConstraintMatch(String jsonString) {

    }
}
