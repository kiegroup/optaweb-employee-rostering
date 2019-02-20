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

package org.optaweb.employeerostering.gwtui.client.gwtjackson;

import com.github.nmorel.gwtjackson.client.JsonSerializationContext;
import com.github.nmorel.gwtjackson.client.JsonSerializer;
import com.github.nmorel.gwtjackson.client.JsonSerializerParameters;
import com.github.nmorel.gwtjackson.client.stream.JsonWriter;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;

// TODO fix https://github.com/nmorel/gwt-jackson/issues/113 or move into upstream module org.optaplanner:optaplanner-gwtjackson
public class HardMediumSoftLongScoreJsonSerializer extends JsonSerializer<HardMediumSoftLongScore> {

    @Override
    protected void doSerialize(JsonWriter writer, HardMediumSoftLongScore value, JsonSerializationContext ctx, JsonSerializerParameters params) {
        writer.value(value.toString());
    }

}
