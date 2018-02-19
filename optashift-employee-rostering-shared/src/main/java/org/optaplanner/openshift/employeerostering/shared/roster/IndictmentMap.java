package org.optaplanner.openshift.employeerostering.shared.roster;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.openshift.employeerostering.shared.jackson.ShiftKeyFieldDeserializer;
import org.optaplanner.openshift.employeerostering.shared.jackson.ShiftKeyFieldSerializer;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;

// TODO: Find out how to tell jackson how to serizalize a Map key without creating a new class
public class IndictmentMap {

    private Map<Shift, Object> indictmentMap;

    public IndictmentMap() {
        indictmentMap = new HashMap<>();
    }

    public IndictmentMap(Map<Shift, Indictment> indictmentMap) {
        this.indictmentMap = new HashMap<>();
        this.indictmentMap.putAll(indictmentMap);
    }

    @JsonSerialize(keyUsing = ShiftKeyFieldSerializer.class)
    @JsonDeserialize(keyUsing = ShiftKeyFieldDeserializer.class)
    public Map<Shift, Object> getIndictmentMap() {
        return indictmentMap;
    }

    public void setIndictmentMap(Map<Shift, Object> indictmentMap) {
        this.indictmentMap = indictmentMap;
    }

}
