package org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson;

import java.time.OffsetTime;

import com.github.nmorel.gwtjackson.client.JsonDeserializationContext;
import com.github.nmorel.gwtjackson.client.JsonDeserializer;
import com.github.nmorel.gwtjackson.client.JsonDeserializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonReader;

public class OffsetTimeJsonDeserializer extends JsonDeserializer<OffsetTime> {

    @Override
    protected OffsetTime doDeserialize(JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params) {
        String text = reader.nextString();
        // TODO the super source of DateTimeFormatter is broken
//        return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return OffsetTime.parse(text);
    }

}
