/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.openshift.employeerostering.server.common.jaxrs;

import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class OptaShiftExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        StringBuilder builder = new StringBuilder();
        builder.append(exception.getMessage());
        for (StackTraceElement trace : exception.getStackTrace()) {
            builder.append(trace.toString()).append('\n');
        }
        return Response.status(resolveStatus(exception))
                .type(MediaType.TEXT_PLAIN)
                .entity(builder.toString())
                .build();
    }

    private Status resolveStatus(final Exception exception) {
        if (exception instanceof EntityNotFoundException) {
            return Status.NOT_FOUND;
        } else {
            return Status.INTERNAL_SERVER_ERROR;
        }
    }

}
