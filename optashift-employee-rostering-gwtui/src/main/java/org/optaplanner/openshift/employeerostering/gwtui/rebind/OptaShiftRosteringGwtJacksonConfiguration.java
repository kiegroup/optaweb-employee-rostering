package org.optaplanner.openshift.employeerostering.gwtui.rebind;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;

import com.github.nmorel.gwtjackson.client.AbstractConfiguration;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.HardSoftScoreJsonDeserializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.HardSoftScoreJsonSerializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.LocalDateJsonDeserializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.LocalDateJsonSerializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.LocalDateTimeJsonDeserializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.LocalDateTimeJsonSerializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.LocalTimeJsonDeserializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.LocalTimeJsonSerializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.OffsetDateTimeJsonDeserializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.OffsetDateTimeJsonSerializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.OffsetTimeJsonDeserializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.OffsetTimeJsonSerializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.ZoneIdJsonDeserializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.ZoneIdJsonSerializer;

public class OptaShiftRosteringGwtJacksonConfiguration extends AbstractConfiguration {

    @Override
    protected void configure() {
        type(LocalDate.class).serializer(LocalDateJsonSerializer.class).deserializer(LocalDateJsonDeserializer.class);
        type(LocalDateTime.class).serializer(LocalDateTimeJsonSerializer.class).deserializer(LocalDateTimeJsonDeserializer.class);
        type(LocalTime.class).serializer(LocalTimeJsonSerializer.class).deserializer(LocalTimeJsonDeserializer.class);
        type(HardSoftScore.class).serializer(HardSoftScoreJsonSerializer.class).deserializer(HardSoftScoreJsonDeserializer.class);
        type(ZoneId.class).serializer(ZoneIdJsonSerializer.class).deserializer(ZoneIdJsonDeserializer.class);
        type(OffsetDateTime.class).serializer(OffsetDateTimeJsonSerializer.class).deserializer(OffsetDateTimeJsonDeserializer.class);
        type(OffsetTime.class).serializer(OffsetTimeJsonSerializer.class).deserializer(OffsetTimeJsonDeserializer.class);
    }

}
