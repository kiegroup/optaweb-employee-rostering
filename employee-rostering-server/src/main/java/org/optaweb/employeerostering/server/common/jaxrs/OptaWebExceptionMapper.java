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

import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.optaweb.employeerostering.server.exception.ExceptionMapping;
import org.optaweb.employeerostering.shared.exception.ServerSideExceptionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class OptaWebExceptionMapper implements ExceptionMapper<Exception> {

    @Inject
    private OptaWebObjectMapperResolver objectMapperProvider;
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Response toResponse(Exception exception) {
        try {
            return Response.status(resolveStatus(exception))
                    .type(MediaType.APPLICATION_JSON)
                    .entity(getEntity(exception))
                    .build();
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("There was an issue with retrieving the root cause: " + e.getMessage() +
                                    "\nMore information can be found in the server log.")
                    .build();
        }
    }

    private String getEntity(final Exception exception) throws JsonProcessingException {
        ObjectMapper objectMapper = objectMapperProvider.getContext(ServerSideExceptionInfo.class);
        ServerSideExceptionInfo serverSideException = ExceptionMapping.getServerSideExceptionFromException(exception);
        return objectMapper.writeValueAsString(serverSideException);
    }

    private Status resolveStatus(final Exception exception) {
        if (exception instanceof EntityNotFoundException) {
            return Status.NOT_FOUND;
        } else {
            return Status.INTERNAL_SERVER_ERROR;
        }
    }
}
