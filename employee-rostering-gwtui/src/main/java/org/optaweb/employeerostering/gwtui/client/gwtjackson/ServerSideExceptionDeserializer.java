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
import org.optaweb.employeerostering.shared.exception.ServerSideExceptionInfo;
import org.optaweb.employeerostering.shared.exception.ServerSideExceptionInfo.ServerSideExceptionInfoFields;

@Singleton
public class ServerSideExceptionDeserializer {

    // Work around for https://github.com/nmorel/gwt-jackson/issues/79
    // (Cannot use the gwt-jackson ObjectMapper due to a compilation error)
    public ServerSideExceptionInfo deserializeFromJsonString(String json) {
        JsonReader reader = new DefaultJsonReader(new StringReader(json));
        try {
            return deserialize(reader, json);
        } finally {
            reader.close();
        }
    }

    public ServerSideExceptionInfo deserialize(JsonReader reader, String json) {
        Set<ServerSideExceptionInfoFields> fields = new HashSet<>(Arrays.asList(ServerSideExceptionInfoFields.values()));

        String i18nKey = null;
        String exceptionMessage = null;
        List<String> messageParameters = null;
        String exceptionClass = null;
        List<String> stackTrace = null;
        ServerSideExceptionInfo exceptionCause = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String fieldName = reader.nextName();
            ServerSideExceptionInfoFields field = ServerSideExceptionInfoFields.getFieldForName(fieldName);
            fields.remove(field);

            switch (field) {
                case I18N_KEY:
                    i18nKey = reader.nextString();
                    break;
                case EXCEPTION_MESSAGE:
                    exceptionMessage = reader.nextString();
                    break;
                case MESSAGE_PARAMETERS:
                    reader.beginArray();
                    messageParameters = new ArrayList<>();
                    while (reader.hasNext()) {
                        messageParameters.add(reader.nextString());
                    }
                    reader.endArray();
                    break;
                case EXCEPTION_CLASS:
                    exceptionClass = reader.nextString();
                    break;
                case STACK_TRACE:
                    reader.beginArray();
                    stackTrace = new ArrayList<>();
                    while (reader.hasNext()) {
                        stackTrace.add(reader.nextString());
                    }
                    reader.endArray();
                    break;

                case EXCEPTION_CAUSE:
                    if (reader.peek() != JsonToken.NULL) {
                        exceptionCause = deserialize(reader, json);
                    } else {
                        reader.nextNull();
                        exceptionCause = null;
                    }
                    break;
                default:
                    raiseError(json);
            }
        }
        reader.endObject();
        if (!fields.isEmpty()) {
            raiseError(json);
        }
        return new ServerSideExceptionInfo(i18nKey, exceptionMessage, messageParameters, exceptionClass, stackTrace, exceptionCause);
    }

    private static void raiseError(String json) {
        throw new IllegalArgumentException("Invalid Json: (" + json + ").");
    }
}
