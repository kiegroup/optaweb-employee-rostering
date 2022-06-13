package org.optaweb.employeerostering.service.contract;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.optaweb.employeerostering.domain.contract.Contract;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class ContractRepository implements PanacheRepository<Contract> {
    public List<Contract> findAllByTenantId(Integer tenantId) {
        return find("tenantId", Sort.ascending("name"), tenantId).list();
    }
}
