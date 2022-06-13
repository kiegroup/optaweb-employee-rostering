package org.optaweb.employeerostering.domain.spot.view;

import java.util.Set;

import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.skill.Skill;

public class SpotView extends AbstractPersistable {

    private String name;

    private Set<Skill> requiredSkillSet;

    @SuppressWarnings("unused")
    public SpotView() {
    }

    public SpotView(Integer tenantId, String name, Set<Skill> requiredSkillSet) {
        super(tenantId);
        this.name = name;
        this.requiredSkillSet = requiredSkillSet;
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Skill> getRequiredSkillSet() {
        return requiredSkillSet;
    }

    public void setRequiredSkillSet(Set<Skill> requiredSkillSet) {
        this.requiredSkillSet = requiredSkillSet;
    }
}
