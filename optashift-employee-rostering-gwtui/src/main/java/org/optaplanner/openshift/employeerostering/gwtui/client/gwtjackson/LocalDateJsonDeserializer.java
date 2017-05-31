package org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.github.nmorel.gwtjackson.client.JsonDeserializationContext;
import com.github.nmorel.gwtjackson.client.JsonDeserializer;
import com.github.nmorel.gwtjackson.client.JsonDeserializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonReader;

public class LocalDateJsonDeserializer extends JsonDeserializer<LocalDate> {

    @Override
    protected LocalDate doDeserialize(JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params) {
        String text = reader.nextString();
        // TODO the super source of DateTimeFormatter is broken
//        return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return LocalDate.parse(text);
    }

}
