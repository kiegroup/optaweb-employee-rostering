/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.gwtui.rebind;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;

import com.github.nmorel.gwtjackson.client.AbstractConfiguration;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.gwtui.client.gwtjackson.DurationJsonDeserializer;
import org.optaweb.employeerostering.gwtui.client.gwtjackson.DurationJsonSerializer;
import org.optaweb.employeerostering.gwtui.client.gwtjackson.HardMediumSoftLongScoreJsonDeserializer;
import org.optaweb.employeerostering.gwtui.client.gwtjackson.HardMediumSoftLongScoreJsonSerializer;
import org.optaweb.employeerostering.gwtui.client.gwtjackson.LocalDateJsonDeserializer;
import org.optaweb.employeerostering.gwtui.client.gwtjackson.LocalDateJsonSerializer;
import org.optaweb.employeerostering.gwtui.client.gwtjackson.LocalDateTimeJsonDeserializer;
import org.optaweb.employeerostering.gwtui.client.gwtjackson.LocalDateTimeJsonSerializer;
import org.optaweb.employeerostering.gwtui.client.gwtjackson.LocalTimeJsonDeserializer;
import org.optaweb.employeerostering.gwtui.client.gwtjackson.LocalTimeJsonSerializer;
import org.optaweb.employeerostering.gwtui.client.gwtjackson.OffsetDateTimeJsonDeserializer;
import org.optaweb.employeerostering.gwtui.client.gwtjackson.OffsetDateTimeJsonSerializer;
import org.optaweb.employeerostering.gwtui.client.gwtjackson.OffsetTimeJsonDeserializer;
import org.optaweb.employeerostering.gwtui.client.gwtjackson.OffsetTimeJsonSerializer;
import org.optaweb.employeerostering.gwtui.client.gwtjackson.ZoneIdJsonDeserializer;
import org.optaweb.employeerostering.gwtui.client.gwtjackson.ZoneIdJsonSerializer;

public class OptaWebGwtJacksonConfiguration extends AbstractConfiguration {

    @Override
    protected void configure() {
        type(LocalDate.class).serializer(LocalDateJsonSerializer.class).deserializer(LocalDateJsonDeserializer.class);
        type(LocalDateTime.class).serializer(LocalDateTimeJsonSerializer.class).deserializer(LocalDateTimeJsonDeserializer.class);
        type(LocalTime.class).serializer(LocalTimeJsonSerializer.class).deserializer(LocalTimeJsonDeserializer.class);
        type(HardMediumSoftLongScore.class).serializer(HardMediumSoftLongScoreJsonSerializer.class).deserializer(HardMediumSoftLongScoreJsonDeserializer.class);
        type(ZoneId.class).serializer(ZoneIdJsonSerializer.class).deserializer(ZoneIdJsonDeserializer.class);
        type(OffsetDateTime.class).serializer(OffsetDateTimeJsonSerializer.class).deserializer(OffsetDateTimeJsonDeserializer.class);
        type(OffsetTime.class).serializer(OffsetTimeJsonSerializer.class).deserializer(OffsetTimeJsonDeserializer.class);
        type(Duration.class).serializer(DurationJsonSerializer.class).deserializer(DurationJsonDeserializer.class);
    }

}
