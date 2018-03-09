package org.optaplanner.openshift.employeerostering.shared.jackson;

import java.io.IOException;
import java.time.ZoneId;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class ZoneIdDeserializer extends JsonDeserializer<ZoneId> {

    @Override
    public ZoneId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        // TODO Auto-generated method stub
        return ZoneId.of(p.nextTextValue());
    }

}
