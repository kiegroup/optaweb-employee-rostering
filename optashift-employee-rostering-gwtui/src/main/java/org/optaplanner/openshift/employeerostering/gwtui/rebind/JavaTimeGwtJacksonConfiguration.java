package org.optaplanner.openshift.employeerostering.gwtui.rebind;

import java.time.LocalDateTime;

import com.github.nmorel.gwtjackson.client.AbstractConfiguration;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.LocalDateTimeJsonDeserializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.LocalDateTimeJsonSerializer;

public class JavaTimeGwtJacksonConfiguration extends AbstractConfiguration {

    @Override
    protected void configure() {
        type(LocalDateTime.class).serializer(LocalDateTimeJsonSerializer.class).deserializer(LocalDateTimeJsonDeserializer.class);
    }

}
