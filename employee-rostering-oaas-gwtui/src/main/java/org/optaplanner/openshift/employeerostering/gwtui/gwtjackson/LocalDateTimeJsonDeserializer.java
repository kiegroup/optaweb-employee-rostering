package org.optaplanner.openshift.employeerostering.gwtui.gwtjackson;

import java.time.LocalDateTime;

import com.github.nmorel.gwtjackson.client.JsonDeserializationContext;
import com.github.nmorel.gwtjackson.client.JsonDeserializer;
import com.github.nmorel.gwtjackson.client.JsonDeserializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonReader;

public class LocalDateTimeJsonDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    protected LocalDateTime doDeserialize(JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params) {
        String text = reader.nextValue();
        // Remove quotes
        text = text.substring(1, text.length() - 1);

        // TODO the super source of DateTimeFormatter is broken
//        return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return LocalDateTime.parse(text);
    }

}
