package org.optaplanner.openshift.employeerostering.server.common;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Objects;

import javax.persistence.EntityManager;

import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;

public class AbstractRestServiceImpl {

    protected void validateTenantIdParameter(Integer tenantId, AbstractPersistable persistable) {
        if (!Objects.equals(persistable.getTenantId(), tenantId)) {
            throw new IllegalStateException("The tenantId (" + tenantId
                    + ") does not match the persistable (" + persistable + ")'s tenantId (" + persistable.getTenantId() + ").");
        }
    }

}
