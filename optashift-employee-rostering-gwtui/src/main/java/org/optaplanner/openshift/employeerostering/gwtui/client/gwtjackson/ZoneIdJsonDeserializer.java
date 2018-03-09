package org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson;

import java.time.ZoneId;

import com.github.nmorel.gwtjackson.client.JsonDeserializationContext;
import com.github.nmorel.gwtjackson.client.JsonDeserializer;
import com.github.nmorel.gwtjackson.client.JsonDeserializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonReader;

public class ZoneIdJsonDeserializer extends JsonDeserializer<ZoneId> {

    @Override
    protected ZoneId doDeserialize(JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params) {
        String text = reader.nextString();
        return ZoneId.of(text);
    }

}
