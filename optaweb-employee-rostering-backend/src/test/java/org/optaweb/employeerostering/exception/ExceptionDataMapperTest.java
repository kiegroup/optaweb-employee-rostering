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

import static org.assertj.core.api.Assertions.*;

import javax.persistence.EntityNotFoundException;
import javax.persistence.RollbackException;

import org.junit.jupiter.api.Test;
import org.optaweb.employeerostering.ExceptionDataMapper;
import org.optaweb.employeerostering.domain.exception.ConstraintViolatedException;

public class ExceptionDataMapperTest {

    private ExceptionDataMapper tested;

    @Test
    public void testGetExceptionDataForExceptionClass() {
        tested = new ExceptionDataMapper();
        assertThat(tested.getExceptionDataForExceptionClass(Throwable.class))
                .isEqualTo(ExceptionDataMapper.ExceptionData.GENERIC_EXCEPTION);
        assertThat(tested.getExceptionDataForExceptionClass(ConstraintViolatedException.class))
                .isEqualTo(ExceptionDataMapper.ExceptionData.ENTITY_CONSTRAINT_VIOLATION);
        assertThat(tested.getExceptionDataForExceptionClass(IllegalStateException.class))
                .isEqualTo(ExceptionDataMapper.ExceptionData.GENERIC_EXCEPTION);
        assertThat(tested.getExceptionDataForExceptionClass(IllegalArgumentException.class))
                .isEqualTo(ExceptionDataMapper.ExceptionData.ILLEGAL_ARGUMENT);
        assertThat(tested.getExceptionDataForExceptionClass(NullPointerException.class))
                .isEqualTo(ExceptionDataMapper.ExceptionData.NULL_POINTER);
        assertThat(tested.getExceptionDataForExceptionClass(EntityNotFoundException.class))
                .isEqualTo(ExceptionDataMapper.ExceptionData.ENTITY_NOT_FOUND);
        assertThat(tested.getExceptionDataForExceptionClass(RollbackException.class))
                .isEqualTo(ExceptionDataMapper.ExceptionData.TRANSACTION_ROLLBACK);
    }
}
