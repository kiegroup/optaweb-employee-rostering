package org.optaplanner.openshift.employeerostering.server.common;

import java.time.OffsetDateTime;
import java.util.Objects;

import javax.persistence.EntityManager;

import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;

public class AbstractRestServiceImpl {

    protected void validateTenantIdParameter(Integer tenantId, AbstractPersistable persistable) {
        if (!Objects.equals(persistable.getTenantId(), tenantId)) {
            throw new IllegalStateException("The tenantId (" + tenantId + ") does not match the persistable (" + persistable + ")'s tenantId (" + persistable.getTenantId() + ").");
        }
    }

    protected OffsetDateTime getLastUpdateDateTime(Integer tenantId, EntityManager entityManager, Class<? extends AbstractPersistable> entityClass) {
        final String queryString = "SELECT MAX(lastUpdateDateTime) FROM " + entityClass.getSimpleName() + " row WHERE row.tenantId = " + tenantId;
        return entityManager.createQuery(queryString, OffsetDateTime.class).getSingleResult();
    }

}
