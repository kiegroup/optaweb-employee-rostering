package org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson;

import java.time.ZoneId;

import com.github.nmorel.gwtjackson.client.JsonSerializationContext;
import com.github.nmorel.gwtjackson.client.JsonSerializer;
import com.github.nmorel.gwtjackson.client.JsonSerializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonWriter;

public class ZoneIdJsonSerializer extends JsonSerializer<ZoneId> {

    @Override
    protected void doSerialize(JsonWriter writer, ZoneId value, JsonSerializationContext ctx, JsonSerializerParameters params) {
        writer.value(value.toString());
    }

}
