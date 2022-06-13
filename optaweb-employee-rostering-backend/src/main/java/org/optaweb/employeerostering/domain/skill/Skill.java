package org.optaweb.employeerostering.domain.skill;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.optaweb.employeerostering.domain.common.AbstractPersistable;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "tenantId", "name" }))
public class Skill extends AbstractPersistable {

    @NotNull
    @Size(min = 1, max = 120)
    @Pattern(regexp = "^(?!\\s).*(?<!\\s)$", message = "Name should not contain any leading or trailing whitespaces")
    private String name;

    @SuppressWarnings("unused")
    public Skill() {

    }

    public Skill(Integer tenantId, String name) {
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
