package org.optaweb.employeerostering.domain.spot;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.skill.Skill;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "tenantId", "name" }),
        @UniqueConstraint(columnNames = { "id" }) })
public class Spot extends AbstractPersistable {

    @NotNull
    @Size(min = 1, max = 120)
    @Pattern(regexp = "^(?!\\s).*(?<!\\s)$", message = "Name should not contain any leading or trailing whitespaces")
    private String name;

    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "SpotRequiredSkillSet",
            joinColumns = @JoinColumn(name = "spotId", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "skillId", referencedColumnName = "id"))
    private Set<Skill> requiredSkillSet;

    @SuppressWarnings("unused")
    public Spot() {
    }

    public Spot(Integer tenantId, String name, Set<Skill> requiredSkillSet) {
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
