package org.optaplanner.openshift.employeerostering.gwtui.rebind;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.github.nmorel.gwtjackson.client.AbstractConfiguration;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.HardSoftScoreJsonDeserializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.HardSoftScoreJsonSerializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.LocalDateJsonDeserializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.LocalDateJsonSerializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.LocalDateTimeJsonDeserializer;
import org.optaplanner.openshift.employeerostering.gwtui.client.gwtjackson.LocalDateTimeJsonSerializer;
import org.optaplanner.openshift.employeerostering.shared.jackson.ShiftKeyDeserializer;
import org.optaplanner.openshift.employeerostering.shared.jackson.ShiftKeySerializer;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.persistence.jackson.api.score.constraint.ConstraintMatchJacksonJsonSerializer;
import org.optaplanner.persistence.jackson.api.score.constraint.IndictmentJacksonJsonDeserializer;
import org.optaplanner.persistence.jackson.api.score.constraint.IndictmentJacksonJsonSerializer;

public class OptaShiftRosteringGwtJacksonConfiguration extends AbstractConfiguration {

    @Override
    protected void configure() {
        type(LocalDate.class).serializer(LocalDateJsonSerializer.class).deserializer(LocalDateJsonDeserializer.class);
        type(LocalDateTime.class).serializer(LocalDateTimeJsonSerializer.class).deserializer(
                LocalDateTimeJsonDeserializer.class);
        type(HardSoftScore.class).serializer(HardSoftScoreJsonSerializer.class).deserializer(
                HardSoftScoreJsonDeserializer.class);
        type(Indictment.class).serializer(IndictmentJsonSerializer.class)
        .deserializer(IndictmentJsonDeserializer.class);
        type(ConstraintMatch.class).serializer(ConstraintMatchJacksonJsonSerializer.class).d;
        key(Shift.class).serializer(ShiftKeySerializer.class).deserializer(ShiftKeyDeserializer.class);
    }

}
