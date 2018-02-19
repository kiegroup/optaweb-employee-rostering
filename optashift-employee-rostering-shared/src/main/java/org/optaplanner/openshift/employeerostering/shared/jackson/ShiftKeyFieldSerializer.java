package org.optaplanner.openshift.employeerostering.shared.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.nmorel.gwtjackson.client.JsonSerializationContext;
import com.github.nmorel.gwtjackson.client.ser.map.key.KeySerializer;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;

public class ShiftKeyFieldSerializer extends JsonSerializer<Shift> {

    @Override
    public void serialize(Shift value, JsonGenerator gen, SerializerProvider serializers) throws IOException,
            JsonProcessingException {
        gen.writeFieldName("S" + value.getId().toString());

    }
}
