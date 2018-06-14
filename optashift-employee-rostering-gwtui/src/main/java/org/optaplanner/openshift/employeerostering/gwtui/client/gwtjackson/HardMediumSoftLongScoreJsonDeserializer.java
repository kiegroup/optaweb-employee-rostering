package org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson;

import java.time.LocalDateTime;

import com.github.nmorel.gwtjackson.client.JsonDeserializationContext;
import com.github.nmorel.gwtjackson.client.JsonDeserializer;
import com.github.nmorel.gwtjackson.client.JsonDeserializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonReader;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

// TODO fix https://github.com/nmorel/gwt-jackson/issues/113 or move into upstream module org.optaplanner:optaplanner-gwtjackson
public class HardMediumSoftLongScoreJsonDeserializer extends JsonDeserializer<HardMediumSoftLongScore> {

    @Override
    protected HardMediumSoftLongScore doDeserialize(JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params) {
        String text = reader.nextString();
        return HardMediumSoftLongScore.parseScore(text);
    }

}
