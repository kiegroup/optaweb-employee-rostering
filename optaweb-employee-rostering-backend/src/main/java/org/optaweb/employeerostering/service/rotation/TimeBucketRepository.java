package org.optaweb.employeerostering.service.rotation;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.optaweb.employeerostering.domain.rotation.TimeBucket;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class TimeBucketRepository implements PanacheRepository<TimeBucket> {

    public List<TimeBucket> findAllByTenantId(Integer tenantId) {
        return find("tenantId", Sort.ascending("startTime", "endTime", "spot.name"),
                tenantId).list();
    }

    public void deleteForTenant(Integer tenantId) {
        delete("tenantId", tenantId);
    }
}
