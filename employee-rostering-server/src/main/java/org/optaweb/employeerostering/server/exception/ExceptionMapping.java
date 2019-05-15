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
package org.optaweb.employeerostering.server.exception;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.persistence.EntityNotFoundException;
import javax.transaction.RollbackException;

import org.optaweb.employeerostering.shared.exception.ServerSideExceptionInfo;

public enum ExceptionMapping {
    ILLEGAL_ARGUMENT("ServerSideException.illegalArgument", IllegalArgumentException.class,
                     t -> Collections.singletonList(t.getMessage())),
    NULL_POINTER("ServerSideException.nullPointer", NullPointerException.class, t -> Collections.emptyList()),
    ENTITY_NOT_FOUND("ServerSideException.entityNotFound", EntityNotFoundException.class,
                     t -> Collections.singletonList(t.getMessage())),
    TRANSACTION_ROLLBACK("ServerSideException.rollback", RollbackException.class, t -> Collections.emptyList());

    private String i18nKey;
    private Class<? extends Throwable> exceptionClass;
    private Function<Throwable, List<String>> parameterMapping;

    private ExceptionMapping(String i18nKey, Class<? extends Throwable> exceptionClass,
                             Function<Throwable, List<String>> parameterMapping) {
        this.i18nKey = i18nKey;
        this.exceptionClass = exceptionClass;
        this.parameterMapping = parameterMapping;
    }

    public static ServerSideExceptionInfo getServerSideExceptionFromException(Throwable t) {
        for (ExceptionMapping exceptionMapping : ExceptionMapping.values()) {
            if (exceptionMapping.exceptionClass.isInstance(t)) {
                return new ServerSideExceptionInfo(t, exceptionMapping.i18nKey,
                                                   exceptionMapping.parameterMapping.apply(t).toArray(new String[0]));
            }
        }
        return new ServerSideExceptionInfo(t, "ServerSideException.unknown", t.toString());
    }

}
