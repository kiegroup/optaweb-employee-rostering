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

package org.optaweb.employeerostering.server.common.jaxrs;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.transaction.RollbackException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.optaweb.employeerostering.shared.exception.ServerSideException;

@Provider
public class OptaWebExceptionMapper implements ExceptionMapper<Exception> {

    @Inject
    private OptaWebObjectMapperResolver objectMapperProvider;

    @Override
    public Response toResponse(Exception exception) {
        StringWriter exceptionStackTrace = new StringWriter();
        PrintWriter exceptionStackTraceWriter = new PrintWriter(exceptionStackTrace);
        exception.printStackTrace(exceptionStackTraceWriter);
        return Response.status(resolveStatus(exception))
                .type(MediaType.APPLICATION_JSON)
                .entity(getEntity(exception))
                .build();
    }

    private String getEntity(final Exception exception) {
        ObjectMapper objectMapper = objectMapperProvider.getContext(ServerSideException.class);
        ServerSideException serverSideException;
        if (exception instanceof IllegalArgumentException) {
            serverSideException = new ServerSideException(exception, "ServerSideException.illegalArgument",
                                                          exception.getMessage());
        } else if (exception instanceof NullPointerException) {
            serverSideException = new ServerSideException(exception, "ServerSideException.nullPointer");
        } else if (exception instanceof EntityNotFoundException) {
            serverSideException = new ServerSideException(exception, "ServerSideException.entityNotFound",
                                                          exception.getMessage());
        } else if (exception instanceof RollbackException) {
            serverSideException = new ServerSideException(exception, "ServerSideException.rollback");
        } else {
            serverSideException = new ServerSideException(exception, "ServerSideException.unknown",
                                                          exception.getMessage());
        }
        try {
            return objectMapper.writeValueAsString(serverSideException);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Status resolveStatus(final Exception exception) {
        if (exception instanceof EntityNotFoundException) {
            return Status.NOT_FOUND;
        } else {
            return Status.INTERNAL_SERVER_ERROR;
        }
    }
}
