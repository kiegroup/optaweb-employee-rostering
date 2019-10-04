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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.persistence.EntityNotFoundException;

import org.optaweb.employeerostering.domain.exception.ServerSideExceptionInfo;
import org.optaweb.employeerostering.util.HierarchyTree;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ExceptionDataMapper {

    private HierarchyTree<Class<? extends Throwable>, ExceptionData> exceptionHierarchyTree;

    public ExceptionDataMapper() {
        exceptionHierarchyTree = new HierarchyTree<>((a, b) -> {
            if (a.equals(b)) {
                return HierarchyTree.HierarchyRelationship.IS_THE_SAME_AS;
            } else if (a.isAssignableFrom(b)) {
                return HierarchyTree.HierarchyRelationship.IS_ABOVE;
            } else if (b.isAssignableFrom(a)) {
                return HierarchyTree.HierarchyRelationship.IS_BELOW;
            } else {
                return HierarchyTree.HierarchyRelationship.HAS_NO_DIRECT_RELATION;
            }
        });

        for (ExceptionData exceptionData : ExceptionData.values()) {
            exceptionHierarchyTree.putInHierarchy(exceptionData.getExceptionClass(), exceptionData);
        }
    }

    public ExceptionData getExceptionDataForExceptionClass(Class<? extends Throwable> clazz) {
        Optional<ExceptionData> exceptionData = exceptionHierarchyTree.getHierarchyClassValue(clazz);
        if (exceptionData.isPresent()) {
            return exceptionData.get();
        } else {
            throw new IllegalStateException("No ExceptionData for exception class (" + clazz + ").");
        }
    }

    public enum ExceptionData {
        GENERIC_EXCEPTION("ServerSideException.generic", HttpStatus.INTERNAL_SERVER_ERROR, Throwable.class,
                          t -> Collections.emptyList()),
        ILLEGAL_ARGUMENT("ServerSideException.illegalArgument", HttpStatus.INTERNAL_SERVER_ERROR,
                         IllegalArgumentException.class,
                         t -> Collections.singletonList(t.getMessage())),
        NULL_POINTER("ServerSideException.nullPointer", HttpStatus.INTERNAL_SERVER_ERROR,
                     NullPointerException.class, t -> Collections.emptyList()),
        ENTITY_NOT_FOUND("ServerSideException.entityNotFound", HttpStatus.NOT_FOUND,
                         EntityNotFoundException.class,
                         t -> Collections.singletonList(t.getMessage())),
        TRANSACTION_ROLLBACK("ServerSideException.rollback", HttpStatus.CONFLICT, DataIntegrityViolationException.class,
                             t -> Collections.emptyList());

        private String i18nKey;
        private HttpStatus statusCode;
        private Class<? extends Throwable> exceptionClass;
        private Function<Throwable, List<String>> parameterMapping;

        ExceptionData(String i18nKey, HttpStatus statusCode, Class<? extends Throwable> exceptionClass,
                      Function<Throwable, List<String>> parameterMapping) {
            this.i18nKey = i18nKey;
            this.statusCode = statusCode;
            this.exceptionClass = exceptionClass;
            this.parameterMapping = parameterMapping;
        }

        public Class<? extends Throwable> getExceptionClass() {
            return exceptionClass;
        }

        public HttpStatus getStatusCode() {
            return statusCode;
        }

        public String getI18nKey() {
            return i18nKey;
        }

        public ServerSideExceptionInfo getServerSideExceptionInfoFromException(Throwable exception) {
            return new ServerSideExceptionInfo(exception, i18nKey,
                                               parameterMapping.apply(exception).toArray(new String[0]));
        }
    }
}
