package org.optaweb.employeerostering.service.employee;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import org.optaweb.employeerostering.domain.employee.Employee;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class EmployeeRepository implements PanacheRepository<Employee> {

    public List<Employee> findAllByTenantId(Integer tenantId) {
        return find("tenantId", Sort.ascending("name"), tenantId).list();
    }

    public Optional<Employee> findEmployeeByName(Integer tenantId, String name) {
        return find("tenantId = ?1 and name = ?2",
                Sort.ascending("name"),
                tenantId, name).singleResultOptional();
    }

    public void deleteForTenant(Integer tenantId) {
        delete("tenantId", tenantId);
    }
}
