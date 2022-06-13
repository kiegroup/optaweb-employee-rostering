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
