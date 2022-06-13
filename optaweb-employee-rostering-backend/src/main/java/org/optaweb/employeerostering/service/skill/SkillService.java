package org.optaweb.employeerostering.service.skill;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.Validator;

import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.skill.view.SkillView;
import org.optaweb.employeerostering.service.common.AbstractRestService;

@ApplicationScoped
public class SkillService extends AbstractRestService {

    SkillRepository skillRepository;

    @Inject
    public SkillService(Validator validator, SkillRepository skillRepository) {
        super(validator);
        this.skillRepository = skillRepository;
    }

    public Skill convertFromView(Integer tenantId, SkillView skillView) {
        Skill skill = new Skill(skillView.getTenantId(), skillView.getName());
        skill.setId(skillView.getId());
        skill.setVersion(skillView.getVersion());
        validateBean(tenantId, skill);

        return skill;
    }

    @Transactional
    public List<Skill> getSkillList(Integer tenantId) {
        return skillRepository.findAllByTenantId(tenantId);
    }

    @Transactional
    public Skill getSkill(Integer tenantId, Long id) {
        Skill skill = skillRepository
                .findByIdOptional(id)
                .orElseThrow(() -> new EntityNotFoundException("No Skill entity found with ID (" + id + ")."));

        validateBean(tenantId, skill);
        return skill;
    }

    @Transactional
    public Boolean deleteSkill(Integer tenantId, Long id) {
        Optional<Skill> skillOptional = skillRepository.findByIdOptional(id);

        if (!skillOptional.isPresent()) {
            return false;
        }

        validateBean(tenantId, skillOptional.get());
        skillRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Skill createSkill(Integer tenantId, SkillView skillView) {
        Skill skill = convertFromView(tenantId, skillView);
        skillRepository.persist(skill);
        return skill;
    }

    @Transactional
    public Skill updateSkill(Integer tenantId, SkillView skillView) {
        Skill newSkill = convertFromView(tenantId, skillView);
        Skill oldSkill = skillRepository
                .findByIdOptional(newSkill.getId())
                .orElseThrow(() -> new EntityNotFoundException("Skill entity with ID (" + newSkill.getId() + ") not " +
                        "found."));

        if (!oldSkill.getTenantId().equals(newSkill.getTenantId())) {
            throw new IllegalStateException("Skill entity with tenantId (" + oldSkill.getTenantId() +
                    ") cannot change tenants.");
        }

        oldSkill.setName(newSkill.getName());
        skillRepository.persist(oldSkill);
        return oldSkill;
    }
}
