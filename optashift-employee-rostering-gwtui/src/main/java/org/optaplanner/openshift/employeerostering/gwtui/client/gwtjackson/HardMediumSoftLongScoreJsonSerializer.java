package org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson;

import com.github.nmorel.gwtjackson.client.JsonSerializationContext;
import com.github.nmorel.gwtjackson.client.JsonSerializer;
import com.github.nmorel.gwtjackson.client.JsonSerializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonWriter;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

// TODO fix https://github.com/nmorel/gwt-jackson/issues/113 or move into upstream module org.optaplanner:optaplanner-gwtjackson
public class HardMediumSoftLongScoreJsonSerializer extends JsonSerializer<HardMediumSoftLongScore> {

    @Override
    protected void doSerialize(JsonWriter writer, HardMediumSoftLongScore value, JsonSerializationContext ctx, JsonSerializerParameters params) {
        writer.value(value.toString());
    }

}
