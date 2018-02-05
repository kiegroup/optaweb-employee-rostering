package org.optaplanner.openshift.employeerostering.shared.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.github.nmorel.gwtjackson.client.JsonDeserializationContext;
import com.github.nmorel.gwtjackson.client.deser.map.key.KeyDeserializer;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;

public class ShiftKeyDeserializer extends KeyDeserializer {

    @Override
    protected Object doDeserialize(String key, JsonDeserializationContext ctx) {
        Shift out = new Shift();
        out.setId(Long.parseLong(key.substring(1)));
        return out;
    }
}
