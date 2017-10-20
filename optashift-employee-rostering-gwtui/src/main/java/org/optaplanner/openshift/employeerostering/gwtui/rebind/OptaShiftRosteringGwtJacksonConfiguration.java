package org.optaplanner.openshift.employeerostering.gwtui.rebind;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.github.nmorel.gwtjackson.client.AbstractConfiguration;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.DurationJsonDeserializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.DurationJsonSerializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.HardSoftScoreJsonDeserializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.HardSoftScoreJsonSerializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.LocalDateJsonDeserializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.LocalDateJsonSerializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.LocalDateTimeJsonDeserializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.LocalDateTimeJsonSerializer;

public class OptaShiftRosteringGwtJacksonConfiguration extends AbstractConfiguration {

    @Override
    protected void configure() {
        type(Duration.class).serializer(DurationJsonSerializer.class).deserializer(DurationJsonDeserializer.class);
        type(LocalDate.class).serializer(LocalDateJsonSerializer.class).deserializer(LocalDateJsonDeserializer.class);
        type(LocalDateTime.class).serializer(LocalDateTimeJsonSerializer.class).deserializer(LocalDateTimeJsonDeserializer.class);
        type(HardSoftScore.class).serializer(HardSoftScoreJsonSerializer.class).deserializer(HardSoftScoreJsonDeserializer.class);
    }

}
