package org.optaweb.employeerostering.service.common;

import java.util.Objects;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.exception.ConstraintViolatedException;

public class AbstractRestService {

    private Validator validator;

    public AbstractRestService() {
        this(null);
    }

    public AbstractRestService(Validator validator) {
        this.validator = validator;
    }

    private void validateTenantIdParameter(Integer tenantId, AbstractPersistable persistable) {
        if (!Objects.equals(persistable.getTenantId(), tenantId)) {
            throw new IllegalStateException("The tenantId (" + tenantId + ") does not match the persistable ("
                    + persistable + ")'s tenantId ("
                    + persistable.getTenantId() + ").");
        }
    }

    protected void validateBean(Integer tenantId, AbstractPersistable persistable) {
        Set<ConstraintViolation<Object>> violationSet = validator.validate(persistable);
        if (!violationSet.isEmpty()) {
            throw new ConstraintViolatedException(persistable, persistable.getClass(), violationSet);
        }
        validateTenantIdParameter(tenantId, persistable);
    }
}
