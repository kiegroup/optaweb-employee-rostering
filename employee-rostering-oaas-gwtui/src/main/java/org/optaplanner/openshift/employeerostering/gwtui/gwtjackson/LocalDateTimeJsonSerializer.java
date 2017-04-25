package org.optaplanner.openshift.employeerostering.gwtui.gwtjackson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.github.nmorel.gwtjackson.client.JsonSerializationContext;
import com.github.nmorel.gwtjackson.client.JsonSerializer;
import com.github.nmorel.gwtjackson.client.JsonSerializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonWriter;

public class LocalDateTimeJsonSerializer extends JsonSerializer<LocalDateTime> {

    @Override
    protected void doSerialize(JsonWriter writer, LocalDateTime value, JsonSerializationContext ctx, JsonSerializerParameters params) {
        writer.value(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

}
