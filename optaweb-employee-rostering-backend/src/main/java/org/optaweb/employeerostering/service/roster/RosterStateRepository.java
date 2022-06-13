package org.optaweb.employeerostering.service.roster;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.optaweb.employeerostering.domain.roster.RosterState;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class RosterStateRepository implements PanacheRepository<RosterState> {

    public Optional<RosterState> findByTenantId(Integer tenantId) {
        return find("tenantId", tenantId).singleResultOptional();
    }

    public void deleteForTenant(Integer tenantId) {
        delete("tenantId", tenantId);
    }
}
