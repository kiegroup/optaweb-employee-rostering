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

package org.optaweb.employeerostering.exception;

import javax.persistence.EntityNotFoundException;
import javax.persistence.RollbackException;

import org.junit.Test;
import org.optaweb.employeerostering.ExceptionDataMapper;

import static org.junit.Assert.assertEquals;

public class ExceptionDataMapperTest {

    private ExceptionDataMapper tested;

    @Test
    public void testGetExceptionDataForExceptionClass() {
        tested = new ExceptionDataMapper();
        assertEquals(ExceptionDataMapper.ExceptionData.GENERIC_EXCEPTION,
                     tested.getExceptionDataForExceptionClass(Throwable.class));
        assertEquals(ExceptionDataMapper.ExceptionData.GENERIC_EXCEPTION,
                     tested.getExceptionDataForExceptionClass(IllegalStateException.class));
        assertEquals(ExceptionDataMapper.ExceptionData.ILLEGAL_ARGUMENT,
                     tested.getExceptionDataForExceptionClass(IllegalArgumentException.class));
        assertEquals(ExceptionDataMapper.ExceptionData.NULL_POINTER,
                     tested.getExceptionDataForExceptionClass(NullPointerException.class));
        assertEquals(ExceptionDataMapper.ExceptionData.ENTITY_NOT_FOUND,
                     tested.getExceptionDataForExceptionClass(EntityNotFoundException.class));
        assertEquals(ExceptionDataMapper.ExceptionData.GENERIC_EXCEPTION,
                     tested.getExceptionDataForExceptionClass(RollbackException.class));
    }
}
