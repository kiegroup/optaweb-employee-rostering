package org.optaweb.employeerostering.domain.employee;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.common.HighContrastColor;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.skill.Skill;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "tenantId", "name" }))
public class Employee extends AbstractPersistable {

    @NotNull
    @Size(min = 1, max = 120)
    @Pattern(regexp = "^(?!\\s).*(?<!\\s)$", message = "Name should not contain any leading or trailing whitespaces")
    private String name;

    @NotNull
    @Size(min = 1, max = 3)
    @Pattern(regexp = "^(?!\\s).*(?<!\\s)$", message = "Name should not contain any leading or trailing whitespaces")
    private String shortId;

    @NotNull
    @Size(min = 7, max = 7)
    @Pattern(regexp = "^#[0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f][0-9a-f]$")
    private String color;

    @NotNull
    @ManyToOne
    private Contract contract;

    @NotNull
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "EmployeeSkillProficiencySet",
            joinColumns = @JoinColumn(name = "employeeId", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "skillId", referencedColumnName = "id"))
    private Set<Skill> skillProficiencySet;

    @SuppressWarnings("unused")
    public Employee() {
    }

    public Employee(Integer tenantId, String name, Contract contract,
            Set<Skill> skillProficiencySet) {
        super(tenantId);
        this.name = name;
        this.shortId = generateShortIdFromName(name);
        this.color = HighContrastColor.generateColorFromHashcode(name);
        this.contract = contract;
        this.skillProficiencySet = skillProficiencySet;
    }

    public Employee(Integer tenantId, String name, Contract contract,
            Set<Skill> skillProficiencySet, String shortId, String color) {
        super(tenantId);
        this.name = name;
        this.shortId = shortId;
        this.color = color;
        this.contract = contract;
        this.skillProficiencySet = skillProficiencySet;
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Constructor default utils
    // ************************************************************************
    public static String generateShortIdFromName(String name) {
        return Arrays.stream(name.split(" ")) // Separate name where there spaces ("Amy Cole" -> ["Amy", "Cole])
                .limit(3) // Limit to the first three parts
                .map(s -> s.substring(0, 1)) // Get the first character of the part
                .collect(Collectors.joining("")); // Join the parts together
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

    public Set<Skill> getSkillProficiencySet() {
        return skillProficiencySet;
    }

    public void setSkillProficiencySet(Set<Skill> skillProficiencySet) {
        this.skillProficiencySet = skillProficiencySet;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public String getShortId() {
        return shortId;
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Employee) {
            Employee other = (Employee) o;
            return this.name.equals(other.getName()) &&
                    this.contract.equals(other.getContract()) &&
                    this.skillProficiencySet.equals(other.getSkillProficiencySet());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, contract, skillProficiencySet);
    }
}
