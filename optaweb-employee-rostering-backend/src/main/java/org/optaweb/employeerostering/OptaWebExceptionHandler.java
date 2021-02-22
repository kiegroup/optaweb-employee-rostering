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

package org.optaweb.employeerostering;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@ApplicationScoped
public class OptaWebExceptionHandler implements ExceptionMapper<Throwable> {
    private final ExceptionDataMapper exceptionDataMapper;

    public OptaWebExceptionHandler() {
        this.exceptionDataMapper = new ExceptionDataMapper();
    }

    @Override
    public Response toResponse(Throwable e) {
        final ExceptionDataMapper.ExceptionData exceptionData =
                exceptionDataMapper.getExceptionDataForExceptionClass(e.getClass());
        return Response.status(exceptionData.getStatusCode())
                .entity(exceptionData.getServerSideExceptionInfoFromException(e))
                .build();
    }
}
