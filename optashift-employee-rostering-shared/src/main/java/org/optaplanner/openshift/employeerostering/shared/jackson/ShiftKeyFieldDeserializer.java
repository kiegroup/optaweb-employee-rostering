package org.optaplanner.openshift.employeerostering.shared.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.github.nmorel.gwtjackson.client.JsonDeserializationContext;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;

public class ShiftKeyFieldDeserializer extends KeyDeserializer {

    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        Shift out = new Shift();
        out.setId(Long.parseLong(key.substring(1)));
        return out;
    }
}
