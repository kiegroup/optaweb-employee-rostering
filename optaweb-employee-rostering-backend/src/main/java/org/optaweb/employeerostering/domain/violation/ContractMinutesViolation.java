package org.optaweb.employeerostering.domain.violation;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.domain.employee.Employee;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ContractMinutesViolation implements ConstraintMatchView {

    private Employee employee;
    private Type type;
    private Long minutesWorked;

    private HardMediumSoftLongScore score;

    public ContractMinutesViolation() {

    }

    public ContractMinutesViolation(Employee employee, Type type, Long minutesWorked,
            HardMediumSoftLongScore score) {
        this.employee = employee;
        this.type = type;
        this.minutesWorked = minutesWorked;
        this.score = score;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getMinutesWorked() {
        return minutesWorked;
    }

    public void setMinutesWorked(Long minutesWorked) {
        this.minutesWorked = minutesWorked;
    }

    @JsonIgnore
    public Integer getMaximumMinutesWorked() {
        switch (type) {
            case DAY:
                return employee.getContract().getMaximumMinutesPerDay();
            case MONTH:
                return employee.getContract().getMaximumMinutesPerMonth();
            case WEEK:
                return employee.getContract().getMaximumMinutesPerWeek();
            case YEAR:
                return employee.getContract().getMaximumMinutesPerYear();
            default:
                throw new IllegalStateException("No case in getMaximumMinutesWorked() for \"" + type.name() + "\".");
        }
    }

    public HardMediumSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftLongScore score) {
        this.score = score;
    }

    public enum Type {
        DAY("daily"),
        WEEK("weekly"),
        MONTH("monthly"),
        YEAR("yearly");

        private String typeString;

        private Type(String typeString) {
            this.typeString = typeString;
        }

        public static Type getTypeForViolation(String constraintName) {
            if (constraintName.equals("Daily minutes must not exceed contract maximum")) {
                return DAY;
            } else if (constraintName.equals("Weekly minutes must not exceed contract maximum")) {
                return WEEK;
            } else if (constraintName.equals("Monthly minutes must not exceed contract maximum")) {
                return MONTH;
            } else if (constraintName.equals("Yearly minutes must not exceed contract maximum")) {
                return YEAR;
            } else {
                throw new IllegalArgumentException("No ContractMinutesViolation.Type correspond to " + constraintName);
            }
        }

        @Override
        public String toString() {
            return typeString;
        }
    }
}
