package org.optaweb.employeerostering.domain.employee.view;

import java.util.Set;

import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.common.HighContrastColor;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.skill.Skill;

public class EmployeeView extends AbstractPersistable {

    private String name;

    private String shortId;

    private String color;

    private Contract contract;

    private Set<Skill> skillProficiencySet;

    @SuppressWarnings("unused")
    public EmployeeView() {
    }

    public EmployeeView(Integer tenantId, String name, Contract contract,
            Set<Skill> skillProficiencySet) {
        super(tenantId);
        this.name = name;
        this.contract = contract;
        this.skillProficiencySet = skillProficiencySet;
        this.shortId = Employee.generateShortIdFromName(name);
        this.color = HighContrastColor.generateColorFromHashcode(name);
    }

    public EmployeeView(Integer tenantId, String name, Contract contract,
            Set<Skill> skillProficiencySet, String shortId, String color) {
        super(tenantId);
        this.name = name;
        this.contract = contract;
        this.skillProficiencySet = skillProficiencySet;
        this.shortId = shortId;
        this.color = color;
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
}
