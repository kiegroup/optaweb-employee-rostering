package org.optaweb.employeerostering.service.tenant;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class RosterConstraintConfigurationRepository implements PanacheRepository<RosterConstraintConfiguration> {

    public Optional<RosterConstraintConfiguration> findByTenantId(Integer tenantId) {
        return find("tenantId", tenantId).singleResultOptional();
    }

    public void deleteForTenant(Integer tenantId) {
        delete("tenantId", tenantId);
    }
}
