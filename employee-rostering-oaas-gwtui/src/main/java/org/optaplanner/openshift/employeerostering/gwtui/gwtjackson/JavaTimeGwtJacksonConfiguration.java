package org.optaplanner.openshift.employeerostering.gwtui.gwtjackson;

import java.time.LocalDateTime;

import com.github.nmorel.gwtjackson.client.AbstractConfiguration;
import org.optaplanner.openshift.employeerostering.gwtui.gwtjackson.LocalDateTimeJsonDeserializer;
import org.optaplanner.openshift.employeerostering.gwtui.gwtjackson.LocalDateTimeJsonSerializer;

public class JavaTimeGwtJacksonConfiguration extends AbstractConfiguration {

    @Override
    protected void configure() {
        type(LocalDateTime.class).serializer(LocalDateTimeJsonSerializer.class).deserializer(LocalDateTimeJsonDeserializer.class);
    }

}
