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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.optaweb.employeerostering.server.exception.ExceptionDataMapper;
import org.optaweb.employeerostering.server.exception.ExceptionDataMapper.ExceptionData;
import org.optaweb.employeerostering.shared.exception.ServerSideExceptionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class OptaWebExceptionMapper implements ExceptionMapper<Exception> {

    @Inject
    private OptaWebObjectMapperResolver objectMapperProvider;

    @Inject
    private ExceptionDataMapper exceptionDataMapper;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Response toResponse(Exception exception) {
        try {
            ExceptionData exceptionData = exceptionDataMapper.getExceptionDataForExceptionClass(exception.getClass());
            return Response.status(exceptionData.getStatusCode())
                    .type(MediaType.APPLICATION_JSON)
                    .entity(getEntity(exceptionData, exception))
                    .build();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("There was an issue with retrieving the root cause: " + e.getMessage() +
                                    "\nMore information can be found in the server log.")
                    .build();
        }
    }

    private String getEntity(final ExceptionData exceptionData, final Throwable exception) throws JsonProcessingException {
        ObjectMapper objectMapper = objectMapperProvider.getContext(ServerSideExceptionInfo.class);
        ServerSideExceptionInfo serverSideException = exceptionData.getServerSideExceptionInfoFromException(exception);
        return objectMapper.writeValueAsString(serverSideException);
    }
}
