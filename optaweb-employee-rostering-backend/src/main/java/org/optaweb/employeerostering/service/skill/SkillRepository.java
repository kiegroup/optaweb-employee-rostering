package org.optaweb.employeerostering.service.skill;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.optaweb.employeerostering.domain.skill.Skill;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

@ApplicationScoped
public class SkillRepository implements PanacheRepository<Skill> {

    public List<Skill> findAllByTenantId(Integer tenantId) {
        return find("tenantId", Sort.ascending("name"), tenantId).list();
    }

    public void deleteForTenant(Integer tenantId) {
        delete("tenantId", tenantId);
    }
}
