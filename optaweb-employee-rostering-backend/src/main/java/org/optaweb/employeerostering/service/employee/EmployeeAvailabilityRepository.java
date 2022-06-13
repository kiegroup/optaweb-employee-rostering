package org.optaweb.employeerostering.service.employee;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class EmployeeAvailabilityRepository implements PanacheRepository<EmployeeAvailability> {

    public List<EmployeeAvailability> findAllByTenantId(Integer tenantId) {
        return find("tenantId", Sort.ascending("employee.name", "startDateTime"), tenantId).list();
    }

    public void deleteForTenant(Integer tenantId) {
        delete("tenantId", tenantId);
    }

    public List<EmployeeAvailability> filterWithEmployee(Integer tenantId,
            Set<Employee> employeeSet,
            OffsetDateTime startDateTime,
            OffsetDateTime endDateTime) {
        // Panache doesn't like empty parameters
        if (employeeSet.isEmpty()) {
            return Collections.emptyList();
        }
        return find("tenantId = ?1 and employee in ?2 and endDateTime >= ?3 and startDateTime < ?4",
                Sort.ascending("employee.name", "startDateTime"),
                tenantId, employeeSet, startDateTime, endDateTime).list();
    }
}
