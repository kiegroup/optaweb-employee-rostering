package org.optaweb.employeerostering.domain.skill.view;

import org.optaweb.employeerostering.domain.common.AbstractPersistable;

public class SkillView extends AbstractPersistable {

    private String name;

    @SuppressWarnings("unused")
    public SkillView() {
    }

    public SkillView(Integer tenantId, String name) {
        super(tenantId);
        this.name = name;
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
}
