package org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson;

import java.time.LocalTime;

import com.github.nmorel.gwtjackson.client.JsonDeserializationContext;
import com.github.nmorel.gwtjackson.client.JsonDeserializer;
import com.github.nmorel.gwtjackson.client.JsonDeserializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonReader;

public class LocalTimeJsonDeserializer extends JsonDeserializer<LocalTime> {

    @Override
    protected LocalTime doDeserialize(JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params) {
        String text = reader.nextString();
        // TODO the super source of DateTimeFormatter is broken
        //        return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return LocalTime.parse(text);
    }

}
