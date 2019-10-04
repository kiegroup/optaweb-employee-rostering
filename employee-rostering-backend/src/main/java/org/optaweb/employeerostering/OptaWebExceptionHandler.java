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

import org.optaweb.employeerostering.domain.exception.ServerSideExceptionInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class OptaWebExceptionHandler extends ResponseEntityExceptionHandler {

    private ExceptionDataMapper exceptionDataMapper;

    public OptaWebExceptionHandler(ExceptionDataMapper exceptionDataMapper) {
        this.exceptionDataMapper = exceptionDataMapper;
    }

    @ExceptionHandler(value = {Exception.class, RuntimeException.class})
    protected ResponseEntity<ServerSideExceptionInfo> handleException(Throwable t, WebRequest request) {
        final ExceptionDataMapper.ExceptionData exceptionData =
                exceptionDataMapper.getExceptionDataForExceptionClass(t.getClass());
        return new ResponseEntity<>(exceptionData.getServerSideExceptionInfoFromException(t),
                                    exceptionData.getStatusCode());
    }
}
