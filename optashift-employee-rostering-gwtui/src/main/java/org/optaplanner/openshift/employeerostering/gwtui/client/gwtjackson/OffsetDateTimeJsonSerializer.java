package org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson;

import java.time.OffsetDateTime;

import com.github.nmorel.gwtjackson.client.JsonSerializationContext;
import com.github.nmorel.gwtjackson.client.JsonSerializer;
import com.github.nmorel.gwtjackson.client.JsonSerializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonWriter;

public class OffsetDateTimeJsonSerializer extends JsonSerializer<OffsetDateTime> {

    @Override
    protected void doSerialize(JsonWriter writer, OffsetDateTime value, JsonSerializationContext ctx, JsonSerializerParameters params) {
        // TODO the super source of DateTimeFormatter is broken
        // writer.value(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        writer.value(value.toString());
    }

}
