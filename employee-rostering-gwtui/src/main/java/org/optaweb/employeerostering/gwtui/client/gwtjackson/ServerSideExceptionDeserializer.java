/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Singleton;

import com.github.nmorel.gwtjackson.client.stream.JsonReader;
import com.github.nmorel.gwtjackson.client.stream.JsonToken;
import com.github.nmorel.gwtjackson.client.stream.impl.DefaultJsonReader;
import com.github.nmorel.gwtjackson.client.stream.impl.StringReader;
import org.optaweb.employeerostering.shared.exception.ServerSideException;

@Singleton
public class ServerSideExceptionDeserializer {

    // Work around for https://github.com/nmorel/gwt-jackson/issues/79
    // (Cannot use the gwt-jackson ObjectMapper due to a compilation error)
    public ServerSideException deserializeFromJsonString(String json) {
        JsonReader reader = new DefaultJsonReader(new StringReader(json));
        try {
            return deserialize(reader, json);
        } finally {
            reader.close();
        }
    }

    public ServerSideException deserialize(JsonReader reader, String json) {
        Set<String> fields = new HashSet<>(Arrays.asList("i18nKey", "messageParameters", "exceptionClass", "stackTrace", "exceptionMessage", "exceptionCause"));

        String i18nKey = null;
        String exceptionMessage = null;
        List<String> messageParameters = null;
        String exceptionClass = null;
        List<String> stackTrace = null;
        ServerSideException exceptionCause = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String fieldName = reader.nextName();
            if (!fields.contains(fieldName)) {
                throw new IllegalArgumentException("Invalid Json: (" + json + ").");
            }
            fields.remove(fieldName);

            switch (fieldName) {
                case "i18nKey":
                    i18nKey = reader.nextString();
                    break;
                case "exceptionMessage":
                    exceptionMessage = reader.nextString();
                    break;
                case "messageParameters":
                    reader.beginArray();
                    messageParameters = new ArrayList<>();
                    while (reader.hasNext()) {
                        messageParameters.add(reader.nextString());
                    }
                    reader.endArray();
                    break;
                case "exceptionClass":
                    exceptionClass = reader.nextString();
                    break;
                case "stackTrace":
                    reader.beginArray();
                    stackTrace = new ArrayList<>();
                    while (reader.hasNext()) {
                        stackTrace.add(reader.nextString());
                    }
                    reader.endArray();
                    break;

                case "exceptionCause":
                    if (reader.peek() != JsonToken.NULL) {
                        exceptionCause = deserialize(reader, json);
                    } else {
                        reader.nextNull();
                        exceptionCause = null;
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Invalid Json: (" + json + ").");
            }
        }
        reader.endObject();
        if (!fields.isEmpty()) {
            throw new IllegalArgumentException("Invalid Json: (" + json + ").");
        }
        return new ServerSideException(i18nKey, exceptionMessage, messageParameters, exceptionClass, stackTrace, exceptionCause);
    }
}
