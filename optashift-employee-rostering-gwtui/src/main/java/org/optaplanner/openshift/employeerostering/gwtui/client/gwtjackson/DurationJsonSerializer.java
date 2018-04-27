package org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson;

import java.time.Duration;

import com.github.nmorel.gwtjackson.client.JsonSerializationContext;
import com.github.nmorel.gwtjackson.client.JsonSerializer;
import com.github.nmorel.gwtjackson.client.JsonSerializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonWriter;

public class DurationJsonSerializer extends JsonSerializer<Duration> {

    @Override
    protected void doSerialize(JsonWriter writer, Duration value, JsonSerializationContext ctx, JsonSerializerParameters params) {
        // TODO the super source of DateTimeFormatter is broken
        // writer.value(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        writer.value(value.toString());
    }

}
