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
package org.optaweb.employeerostering.server.common.jaxrs;

import java.io.IOException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.optaweb.employeerostering.server.exception.ExceptionDataMapper;
import org.optaweb.employeerostering.server.exception.ExceptionDataMapper.ExceptionData;
import org.optaweb.employeerostering.shared.exception.ServerSideExceptionInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OptaWebExceptionMapperTest {

    @InjectMocks
    private OptaWebExceptionMapper tested;

    @Mock
    private OptaWebObjectMapperResolver optaWebObjectMapperResolver;

    @Mock
    private ExceptionDataMapper exceptionDataMapper;

    @Test
    public void testToResponse() {
        ObjectMapper objectMapper = new ObjectMapper();
        when(exceptionDataMapper.getExceptionDataForExceptionClass(IllegalArgumentException.class))
                .thenReturn(ExceptionData.ILLEGAL_ARGUMENT);
        when(optaWebObjectMapperResolver.getContext(any())).thenReturn(objectMapper);

        final String EXCEPTION_MESSAGE = "TEST";
        Response response = tested.toResponse(new IllegalArgumentException(EXCEPTION_MESSAGE));

        assertEquals(ExceptionData.ILLEGAL_ARGUMENT.getStatusCode(), response.getStatusInfo());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType());

        try {
            ServerSideExceptionInfo serverSideException = objectMapper.readValue(response.getEntity().toString(), ServerSideExceptionInfo.class);
            assertEquals(EXCEPTION_MESSAGE, serverSideException.getExceptionMessage());
            assertEquals(IllegalArgumentException.class.getName(), serverSideException.getExceptionClass());
            assertEquals(ExceptionData.ILLEGAL_ARGUMENT.getI18nKey(), serverSideException.getI18nKey());
        } catch (IOException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testToResponseError() {
        final String ERROR_MSG = "BAD STATE";
        final String EXPECTED_MSG = "There was an issue with retrieving the root cause: " + ERROR_MSG +
                "\nMore information can be found in the server log.";
        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        JsonProcessingException jsonProcessingException = mock(JsonProcessingException.class);
        when(exceptionDataMapper.getExceptionDataForExceptionClass(IllegalArgumentException.class))
                .thenReturn(ExceptionData.ENTITY_NOT_FOUND);
        try {
            when(jsonProcessingException.getMessage()).thenReturn(ERROR_MSG);
            when(mockObjectMapper.writeValueAsString(any())).thenThrow(jsonProcessingException);
        } catch (Exception e) {
            fail(e.toString());
        }
        when(optaWebObjectMapperResolver.getContext(any())).thenReturn(mockObjectMapper);

        Response response = tested.toResponse(new IllegalArgumentException());

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatusInfo());
        assertEquals(MediaType.TEXT_PLAIN_TYPE, response.getMediaType());
        assertEquals(EXPECTED_MSG, response.getEntity().toString());
    }
}
