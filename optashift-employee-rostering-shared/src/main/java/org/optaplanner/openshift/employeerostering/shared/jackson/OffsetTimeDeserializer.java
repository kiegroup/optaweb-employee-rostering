/*
 * Copyright 2013 FasterXML.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package org.optaplanner.openshift.employeerostering.shared.jackson;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

//TODO: Remove me when our minimum Wildfly version is 10.1.0.Final and
//the minimum  EAP version is (one which uses Wildfly 10.1.0.Final or higher)
//This class is a work around for the following issue:
//https://issues.jboss.org/browse/PLANNER-903
public class OffsetTimeDeserializer extends JsonDeserializer<OffsetTime> {

    @Override
    public OffsetTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (parser.hasTokenId(JsonTokenId.ID_STRING)) {
            String string = parser.getText().trim();
            if (string.length() == 0) {
                return null;
            }

            try {
                return OffsetTime.parse(string, DateTimeFormatter.ISO_OFFSET_TIME);
            } catch (DateTimeException e) {
                throw new IOException(e);
            }
        }
        if (parser.hasTokenId(JsonTokenId.ID_EMBEDDED_OBJECT)) {
            return (OffsetTime) parser.getEmbeddedObject();
        }
        throw context.wrongTokenException(parser, JsonToken.VALUE_STRING, "Expected string.");
    }
}
