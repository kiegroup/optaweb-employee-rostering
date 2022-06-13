package org.optaweb.employeerostering.service.tenant;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.optaweb.employeerostering.domain.tenant.Tenant;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class TenantRepository implements PanacheRepositoryBase<Tenant, Integer> {

    // Deliberately order by id instead of name to use generated order
    public List<Tenant> findAllTenants() {
        return findAll(Sort.ascending("id")).list();
    }
}
