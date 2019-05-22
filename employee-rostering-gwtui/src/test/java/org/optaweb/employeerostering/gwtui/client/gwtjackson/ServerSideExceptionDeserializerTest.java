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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.optaweb.employeerostering.shared.exception.ServerSideExceptionInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ServerSideExceptionDeserializerTest {

    @Test
    public void testDeserializer() {
        ServerSideExceptionDeserializer deserializer = new ServerSideExceptionDeserializer(json -> new TestJsonReaderImpl(json));
        ObjectMapper objectMapper = new ObjectMapper();

        Exception exception1 = new Exception("Test");
        IllegalStateException exception2 = new IllegalStateException(exception1);
        IllegalArgumentException exception3 = new IllegalArgumentException(exception2);

        ServerSideExceptionInfo exceptionInfo = new ServerSideExceptionInfo(exception3, "ServerSideExceptionInfo.test",
                                                                            "Hello", "World");

        try {
            String serializedException = objectMapper.writeValueAsString(exceptionInfo);
            ServerSideExceptionInfo deserializedExceptionInfo = deserializer.deserializeFromJsonString(serializedException);
            assertThat(deserializedExceptionInfo).isEqualToComparingFieldByFieldRecursively(exceptionInfo);
        } catch (JsonProcessingException e) {
            fail("JSON serialization failed: " + e.toString());
        }
    }
}
