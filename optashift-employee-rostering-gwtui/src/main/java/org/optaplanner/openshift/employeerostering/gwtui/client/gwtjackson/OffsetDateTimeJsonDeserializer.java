package org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson;

import java.time.OffsetDateTime;

import com.github.nmorel.gwtjackson.client.JsonDeserializationContext;
import com.github.nmorel.gwtjackson.client.JsonDeserializer;
import com.github.nmorel.gwtjackson.client.JsonDeserializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonReader;

public class OffsetDateTimeJsonDeserializer extends JsonDeserializer<OffsetDateTime> {

    @Override
    protected OffsetDateTime doDeserialize(JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params) {
        String text = reader.nextString();
        // TODO the super source of DateTimeFormatter is broken
//        return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return OffsetDateTime.parse(text);
    }

}
