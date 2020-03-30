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

package org.optaweb.employeerostering.service.common;

import java.time.Duration;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.roster.Roster;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;
import org.optaweb.employeerostering.domain.violation.ContractMinutesViolation;
import org.optaweb.employeerostering.domain.violation.DesiredTimeslotForEmployeeReward;
import org.optaweb.employeerostering.domain.violation.InoculatedEmployeeAssignedOutsideOfCovidWardViolation;
import org.optaweb.employeerostering.domain.violation.MaximizeInoculatedEmployeeHoursReward;
import org.optaweb.employeerostering.domain.violation.MigrationBetweenCovidAndNonCovidWardsViolation;
import org.optaweb.employeerostering.domain.violation.NonCovidShiftSoonAfterCovidShiftViolation;
import org.optaweb.employeerostering.domain.violation.NonInoculatedEmployeeAssignedToCovidWardViolation;
import org.optaweb.employeerostering.domain.violation.RequiredSkillViolation;
import org.optaweb.employeerostering.domain.violation.RotationViolationPenalty;
import org.optaweb.employeerostering.domain.violation.ShiftEmployeeConflict;
import org.optaweb.employeerostering.domain.violation.UnassignedShiftPenalty;
import org.optaweb.employeerostering.domain.violation.UnavailableEmployeeViolation;
import org.optaweb.employeerostering.domain.violation.UndesiredTimeslotForEmployeePenalty;
import org.optaweb.employeerostering.service.solver.WannabeSolverManager;
import org.springframework.stereotype.Component;

import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_ASSIGN_EVERY_SHIFT;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_AT_MOST_ONE_SHIFT_ASSIGNMENT_PER_DAY_PER_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_NO_2_SHIFTS_WITHIN_10_HOURS_FROM_EACH_OTHER;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_REQUIRED_SKILL_FOR_A_SHIFT;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM;

@Component
public class IndictmentUtils {

    private WannabeSolverManager solverManager;

    private static final String CONSTRAINT_MATCH_PACKAGE = "org.optaweb.employeerostering.service.solver";

    public IndictmentUtils(WannabeSolverManager solverManager) {
        this.solverManager = solverManager;
    }

    public Map<Object, Indictment> getIndictmentMapForRoster(Roster roster) {
        try (ScoreDirector<Roster> scoreDirector = solverManager.getScoreDirector()) {
            scoreDirector.setWorkingSolution(roster);
            scoreDirector.calculateScore();
            return scoreDirector.getIndictmentMap();
        }
    }

    public ShiftView getShiftViewWithIndictment(ZoneId zoneId, Shift shift, Indictment indictment) {
        return new ShiftView(zoneId, shift,
                             getRequiredSkillViolationList(indictment),
                             getUnavailableEmployeeViolationList(indictment),
                             getShiftEmployeeConflictList(indictment),
                             getDesiredTimeslotForEmployeeRewardList(indictment),
                             getUndesiredTimeslotForEmployeePenaltyList(indictment),
                             getRotationViolationPenaltyList(indictment),
                             getUnassignedShiftPenaltyList(indictment),
                             getContractMinutesViolationList(indictment),
                             getNonInoculatedEmployeeAssignedToCovidWardViolationList(indictment),
                             getInoculatedEmployeeAssignedOutsideOfCovidWardViolationList(indictment),
                             getMaximizeInoculatedEmployeeHoursRewardList(indictment),
                             getMigrationBetweenCovidAndNonCovidWardsViolationList(indictment),
                             getNonCovidShiftSoonAfterCovidShiftViolationList(indictment),
                             (indictment != null) ?
                                     (HardMediumSoftLongScore) indictment.getScore() : HardMediumSoftLongScore.ZERO);
    }

    public List<NonInoculatedEmployeeAssignedToCovidWardViolation>
    getNonInoculatedEmployeeAssignedToCovidWardViolationList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        (cm.getConstraintName().equals("Low-risk employee assigned to a COVID ward") ||
                                cm.getConstraintName().equals("Moderate-risk employee assigned to a COVID ward") ||
                                cm.getConstraintName().equals("High-risk employee assigned to a COVID ward") ||
                                cm.getConstraintName().equals("Extreme-risk employee assigned to a COVID ward")))
                .map(cm -> new NonInoculatedEmployeeAssignedToCovidWardViolation((Shift) cm.getJustificationList()
                        .stream()
                        .filter(o -> o instanceof Shift)
                        .findFirst().get(), (HardMediumSoftLongScore) cm.getScore()))
                .collect(Collectors.toList());
    }

    public List<InoculatedEmployeeAssignedOutsideOfCovidWardViolation>
    getInoculatedEmployeeAssignedOutsideOfCovidWardViolationList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        (cm.getConstraintName().equals("Inoculated employee outside a COVID ward")))
                .map(cm -> new InoculatedEmployeeAssignedOutsideOfCovidWardViolation((Shift) cm.getJustificationList()
                        .stream()
                        .filter(o -> o instanceof Shift)
                        .findFirst().get(), (HardMediumSoftLongScore) cm.getScore()))
                .collect(Collectors.toList());
    }

    public List<MaximizeInoculatedEmployeeHoursReward>
    getMaximizeInoculatedEmployeeHoursRewardList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        (cm.getConstraintName().equals("Maximize inoculated hours")))
                .map(cm -> new MaximizeInoculatedEmployeeHoursReward((Shift) cm.getJustificationList()
                        .stream()
                        .filter(o -> o instanceof Shift)
                        .findFirst().get(), (HardMediumSoftLongScore) cm.getScore()))
                .collect(Collectors.toList());
    }

    public List<MigrationBetweenCovidAndNonCovidWardsViolation>
    getMigrationBetweenCovidAndNonCovidWardsViolationList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        cm.getConstraintName().equals("Migration between COVID and non-COVID wards"))
                .map(cm -> new MigrationBetweenCovidAndNonCovidWardsViolation(
                        (Shift) cm.getJustificationList().stream()
                                .filter(s -> s instanceof Shift && ((Shift) s).getSpot().isCovidWard())
                                .findAny().get(),
                        (Shift) cm.getJustificationList().stream()
                                .filter(s -> s instanceof Shift && !((Shift) s).getSpot().isCovidWard())
                                .findAny().get(),
                        (HardMediumSoftLongScore) cm.getScore()))
                .collect(Collectors.toList());
    }

    public List<NonCovidShiftSoonAfterCovidShiftViolation>
    getNonCovidShiftSoonAfterCovidShiftViolationList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        cm.getConstraintName()
                                .equals("Non-COVID shift started less than 8 hours after finishing a COVID shift"))
                .map(cm -> new NonCovidShiftSoonAfterCovidShiftViolation(
                        (Shift) cm.getJustificationList().stream()
                                .filter(s -> s instanceof Shift && ((Shift) s).getSpot().isCovidWard())
                                .findAny().get(),
                        (Shift) cm.getJustificationList().stream()
                                .filter(s -> s instanceof Shift && !((Shift) s).getSpot().isCovidWard())
                                .findAny().get(),
                        (HardMediumSoftLongScore) cm.getScore()))
                .collect(Collectors.toList());
    }

    public List<RequiredSkillViolation> getRequiredSkillViolationList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        cm.getConstraintName().equals(CONSTRAINT_REQUIRED_SKILL_FOR_A_SHIFT))
                .map(cm -> new RequiredSkillViolation((Shift) cm.getJustificationList().get(0),
                                                      (HardMediumSoftLongScore) cm.getScore()))
                .collect(Collectors.toList());
    }

    public List<UnavailableEmployeeViolation> getUnavailableEmployeeViolationList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        cm.getConstraintName().equals(CONSTRAINT_UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE))
                .map(cm -> new UnavailableEmployeeViolation((Shift) cm.getJustificationList().get(1),
                                                            (EmployeeAvailability) cm.getJustificationList().get(0),
                                                            (HardMediumSoftLongScore) cm.getScore()))
                .collect(Collectors.toList());
    }

    public List<DesiredTimeslotForEmployeeReward> getDesiredTimeslotForEmployeeRewardList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        cm.getConstraintName().equals(CONSTRAINT_DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE))
                .map(cm -> new DesiredTimeslotForEmployeeReward((Shift) cm.getJustificationList().get(1),
                                                                (EmployeeAvailability) cm.getJustificationList().get(0),
                                                                (HardMediumSoftLongScore) cm.getScore()))
                .collect(Collectors.toList());
    }

    public List<UndesiredTimeslotForEmployeePenalty> getUndesiredTimeslotForEmployeePenaltyList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        cm.getConstraintName().equals(CONSTRAINT_UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE))
                .map(cm -> new UndesiredTimeslotForEmployeePenalty((Shift) cm.getJustificationList().get(1),
                                                                   (EmployeeAvailability) cm.getJustificationList()
                                                                           .get(0),
                                                                   (HardMediumSoftLongScore) cm.getScore()))
                .collect(Collectors.toList());
    }

    public List<ShiftEmployeeConflict> getShiftEmployeeConflictList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        cm.getConstraintName().equals(CONSTRAINT_AT_MOST_ONE_SHIFT_ASSIGNMENT_PER_DAY_PER_EMPLOYEE) ||
                        cm.getConstraintName().equals(CONSTRAINT_NO_2_SHIFTS_WITHIN_10_HOURS_FROM_EACH_OTHER))
                .map(cm -> new ShiftEmployeeConflict((Shift) cm.getJustificationList().get(0),
                                                     (Shift) cm.getJustificationList().get(1),
                                                     (HardMediumSoftLongScore) cm.getScore()))
                .collect(Collectors.toList());
    }

    public List<RotationViolationPenalty> getRotationViolationPenaltyList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        cm.getConstraintName().equals(CONSTRAINT_EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE))
                .map(cm -> new RotationViolationPenalty((Shift) cm.getJustificationList().get(0),
                                                        (HardMediumSoftLongScore) cm.getScore()))
                .collect(Collectors.toList());
    }

    public List<UnassignedShiftPenalty> getUnassignedShiftPenaltyList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        cm.getConstraintName().equals(CONSTRAINT_ASSIGN_EVERY_SHIFT))
                .map(cm -> new UnassignedShiftPenalty((Shift) cm.getJustificationList().get(0),
                                                      (HardMediumSoftLongScore) cm.getScore()))
                .collect(Collectors.toList());
    }

    public List<ContractMinutesViolation> getContractMinutesViolationList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        // getJustificationList() was not consistent; sometimes employee was first, other times minutes worked was first
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> {
                    String constraintName = cm.getConstraintName();
                    return cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                            (constraintName.equals(CONSTRAINT_DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM) ||
                            constraintName.equals(CONSTRAINT_WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM) ||
                            constraintName.equals(CONSTRAINT_MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM) ||
                            constraintName.equals(CONSTRAINT_YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM));
                })
                .map(cm -> new ContractMinutesViolation((Employee) cm.getJustificationList()
                        .stream()
                        .filter(o -> o instanceof Employee)
                        .findFirst().get(), ContractMinutesViolation.Type.getTypeForViolation(cm.getConstraintName()),
                                                        (Long) cm.getJustificationList()
                                                                .stream()
                                                                .filter(o -> o instanceof Duration)
                                                                .findFirst().get(),
                                                        (HardMediumSoftLongScore) cm.getScore()))
                .collect(Collectors.toList());
    }
}
