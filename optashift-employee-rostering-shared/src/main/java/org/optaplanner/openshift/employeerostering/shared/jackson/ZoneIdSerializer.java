package org.optaplanner.openshift.employeerostering.shared.jackson;

import java.io.IOException;
import java.time.ZoneId;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ZoneIdSerializer extends JsonSerializer<ZoneId> {

    @Override
    public void serialize(ZoneId value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeString(value.toString());
    }

}
