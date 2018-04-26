package org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson;

import java.time.ZoneId;

import com.github.nmorel.gwtjackson.client.JsonDeserializationContext;
import com.github.nmorel.gwtjackson.client.JsonDeserializer;
import com.github.nmorel.gwtjackson.client.JsonDeserializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonReader;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.DateTimeUtils;

public class ZoneIdJsonDeserializer extends JsonDeserializer<ZoneId> {

    private DateTimeUtils dateTimeUtils = new DateTimeUtils();

    @Override
    protected ZoneId doDeserialize(JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params) {
        String text = reader.nextString();
        return dateTimeUtils.getZoneIdForZoneName(text);
    }

}
