package org.optaplanner.openshift.employeerostering.gwtui.gwtjackson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.github.nmorel.gwtjackson.client.JsonDeserializationContext;
import com.github.nmorel.gwtjackson.client.JsonDeserializer;
import com.github.nmorel.gwtjackson.client.JsonDeserializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonReader;

public class LocalDateTimeJsonDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    protected LocalDateTime doDeserialize(JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params) {
        return LocalDateTime.parse(reader.nextValue(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}
