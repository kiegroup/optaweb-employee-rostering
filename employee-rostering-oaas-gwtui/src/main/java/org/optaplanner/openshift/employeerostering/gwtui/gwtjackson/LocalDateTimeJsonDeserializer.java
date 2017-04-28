package org.optaplanner.openshift.employeerostering.gwtui.gwtjackson;

import java.time.LocalDateTime;

import com.github.nmorel.gwtjackson.client.JsonDeserializationContext;
import com.github.nmorel.gwtjackson.client.JsonDeserializer;
import com.github.nmorel.gwtjackson.client.JsonDeserializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonReader;

public class LocalDateTimeJsonDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    protected LocalDateTime doDeserialize(JsonReader reader, JsonDeserializationContext ctx, JsonDeserializerParameters params) {
        // TODO the super source of DateTimeFormatter is broken
//        return LocalDateTime.parse(reader.nextValue(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // Manual parse of "yyyy-mm-ddThh:mm:ss"
        String text = reader.nextValue();
        // Remove quotes
        text = text.substring(1, text.length() - 1);
        int year = Integer.parseInt(text.substring(0, 4));
        int month = Integer.parseInt(text.substring(5, 7));
        int dayOfMonth = Integer.parseInt(text.substring(8, 10));
        int hour = Integer.parseInt(text.substring(11, 13));
        int minute = Integer.parseInt(text.substring(14, 16));
        int second = Integer.parseInt(text.substring(17, 19));
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
    }

}
