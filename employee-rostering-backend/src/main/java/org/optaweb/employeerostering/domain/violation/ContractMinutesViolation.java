/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaweb.employeerostering.domain.violation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.domain.employee.Employee;

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
