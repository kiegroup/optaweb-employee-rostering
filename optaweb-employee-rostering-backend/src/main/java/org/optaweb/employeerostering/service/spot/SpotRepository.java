package org.optaweb.employeerostering.service.spot;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.optaweb.employeerostering.domain.spot.Spot;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class SpotRepository implements PanacheRepository<Spot> {

    public List<Spot> findAllByTenantId(Integer tenantId) {
        return find("tenantId", Sort.ascending("name"), tenantId).list();
    }

    public void deleteForTenant(Integer tenantId) {
        delete("tenantId", tenantId);
    }
}
