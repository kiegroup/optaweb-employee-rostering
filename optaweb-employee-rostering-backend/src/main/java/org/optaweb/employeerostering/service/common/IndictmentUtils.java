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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_ASSIGN_EVERY_SHIFT;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_BREAK_BETWEEN_NON_CONSECUTIVE_SHIFTS;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_EMPLOYEE_IS_NOT_ORIGINAL_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_NO_MORE_THAN_2_CONSECUTIVE_SHIFTS;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_NO_OVERLAPPING_SHIFTS;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_REQUIRED_SKILL_FOR_A_SHIFT;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM;
import static org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration.CONSTRAINT_YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.roster.Roster;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;
import org.optaweb.employeerostering.domain.violation.ContractMinutesViolation;
import org.optaweb.employeerostering.domain.violation.DesiredTimeslotForEmployeeReward;
import org.optaweb.employeerostering.domain.violation.IndictmentSummary;
import org.optaweb.employeerostering.domain.violation.NoBreakViolation;
import org.optaweb.employeerostering.domain.violation.PublishedShiftReassignedPenalty;
import org.optaweb.employeerostering.domain.violation.RequiredSkillViolation;
import org.optaweb.employeerostering.domain.violation.RotationViolationPenalty;
import org.optaweb.employeerostering.domain.violation.ShiftEmployeeConflict;
import org.optaweb.employeerostering.domain.violation.UnassignedShiftPenalty;
import org.optaweb.employeerostering.domain.violation.UnavailableEmployeeViolation;
import org.optaweb.employeerostering.domain.violation.UndesiredTimeslotForEmployeePenalty;
import org.springframework.stereotype.Component;

@Component
public class IndictmentUtils {

    public static final String CONSTRAINT_MATCH_PACKAGE = "org.optaweb.employeerostering.service.solver";
    private ScoreManager<Roster, HardMediumSoftLongScore> scoreManager;

    public IndictmentUtils(ScoreManager<Roster, HardMediumSoftLongScore> scoreManager) {
        this.scoreManager = scoreManager;
    }

    public Map<Object, Indictment<HardMediumSoftLongScore>> getIndictmentMapForRoster(Roster roster) {
        return scoreManager.explainScore(roster).getIndictmentMap();
    }

    public IndictmentSummary getIndictmentSummaryForRoster(Roster roster) {
        Map<String, ConstraintMatchTotal<HardMediumSoftLongScore>> constraintMatchTotalMap = scoreManager.explainScore(roster)
                .getConstraintMatchTotalMap();
        IndictmentSummary out = new IndictmentSummary();
        out.setConstraintToCountMap(constraintMatchTotalMap.values().stream()
                .collect(toMap(ConstraintMatchTotal::getConstraintName,
                        ConstraintMatchTotal::getConstraintMatchCount)));
        out.setConstraintToScoreImpactMap(constraintMatchTotalMap.values().stream()
                .collect(toMap(ConstraintMatchTotal::getConstraintName,
                        cmt -> (HardMediumSoftLongScore) cmt.getScore())));
        return out;
    }

    public ShiftView getShiftViewWithIndictment(ZoneId zoneId, Shift shift, Indictment<HardMediumSoftLongScore> indictment) {
        return new ShiftView(zoneId, shift,
                getRequiredSkillViolationList(indictment),
                getUnavailableEmployeeViolationList(indictment),
                getShiftEmployeeConflictList(indictment),
                getDesiredTimeslotForEmployeeRewardList(indictment),
                getUndesiredTimeslotForEmployeePenaltyList(indictment),
                getRotationViolationPenaltyList(indictment),
                getUnassignedShiftPenaltyList(indictment),
                getContractMinutesViolationList(indictment),
                getNoBreakViolationList(indictment),
                getPublishedShiftReassignedPenaltyList(indictment),
                (indictment != null) ? (HardMediumSoftLongScore) indictment.getScore() : HardMediumSoftLongScore.ZERO);
    }

    public List<RequiredSkillViolation> getRequiredSkillViolationList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        cm.getConstraintName().equals(CONSTRAINT_REQUIRED_SKILL_FOR_A_SHIFT))
                .map(cm -> new RequiredSkillViolation((Shift) cm.getJustificationList().get(0),
                        (HardMediumSoftLongScore) cm.getScore()))
                .collect(toList());
    }

    public List<UnavailableEmployeeViolation>
            getUnavailableEmployeeViolationList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        cm.getConstraintName().equals(CONSTRAINT_UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE))
                .map(cm -> new UnavailableEmployeeViolation((Shift) cm.getJustificationList().get(0),
                        (EmployeeAvailability) cm.getJustificationList().get(1),
                        (HardMediumSoftLongScore) cm.getScore()))
                .collect(toList());
    }

    public List<DesiredTimeslotForEmployeeReward>
            getDesiredTimeslotForEmployeeRewardList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        cm.getConstraintName().equals(CONSTRAINT_DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE))
                .map(cm -> new DesiredTimeslotForEmployeeReward((Shift) cm.getJustificationList().get(0),
                        (EmployeeAvailability) cm.getJustificationList().get(1),
                        (HardMediumSoftLongScore) cm.getScore()))
                .collect(toList());
    }

    public List<UndesiredTimeslotForEmployeePenalty>
            getUndesiredTimeslotForEmployeePenaltyList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        cm.getConstraintName().equals(CONSTRAINT_UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE))
                .map(cm -> new UndesiredTimeslotForEmployeePenalty((Shift) cm.getJustificationList().get(0),
                        (EmployeeAvailability) cm.getJustificationList()
                                .get(1),
                        (HardMediumSoftLongScore) cm.getScore()))
                .collect(toList());
    }

    public List<ShiftEmployeeConflict> getShiftEmployeeConflictList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        List<String> employeeShiftConstraintNameList =
                Arrays.asList(CONSTRAINT_BREAK_BETWEEN_NON_CONSECUTIVE_SHIFTS, CONSTRAINT_NO_OVERLAPPING_SHIFTS);
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        employeeShiftConstraintNameList.contains(cm.getConstraintName()))
                .map(cm -> new ShiftEmployeeConflict((Shift) cm.getJustificationList().get(0),
                        (Shift) cm.getJustificationList().get(1),
                        (HardMediumSoftLongScore) cm.getScore()))
                .collect(toList());
    }

    public List<NoBreakViolation> getNoBreakViolationList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        cm.getConstraintName().equals(CONSTRAINT_NO_MORE_THAN_2_CONSECUTIVE_SHIFTS))
                .map(cm -> new NoBreakViolation((Shift) cm.getJustificationList().get(0),
                        (Shift) cm.getJustificationList().get(1),
                        (Shift) cm.getJustificationList().get(2),
                        (HardMediumSoftLongScore) cm.getScore()))
                .collect(toList());
    }

    public List<RotationViolationPenalty> getRotationViolationPenaltyList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        cm.getConstraintName().equals(CONSTRAINT_EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE))
                .map(cm -> new RotationViolationPenalty((Shift) cm.getJustificationList().get(0),
                        (HardMediumSoftLongScore) cm.getScore()))
                .collect(toList());
    }

    public List<UnassignedShiftPenalty> getUnassignedShiftPenaltyList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        cm.getConstraintName().equals(CONSTRAINT_ASSIGN_EVERY_SHIFT))
                .map(cm -> new UnassignedShiftPenalty((Shift) cm.getJustificationList().get(0),
                        (HardMediumSoftLongScore) cm.getScore()))
                .collect(toList());
    }

    public List<ContractMinutesViolation> getContractMinutesViolationList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        // getJustificationList() was not consistent; sometimes employee was first, other times minutes worked was first
        List<String> contractMinutesConstraintNameList = Arrays.asList(
                CONSTRAINT_DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                CONSTRAINT_WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                CONSTRAINT_MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                CONSTRAINT_YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM);
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        contractMinutesConstraintNameList.contains(cm.getConstraintName()))
                .map(cm -> new ContractMinutesViolation((Employee) cm.getJustificationList()
                        .stream()
                        .filter(o -> o instanceof Employee)
                        .findFirst().get(), ContractMinutesViolation.Type.getTypeForViolation(cm.getConstraintName()),
                        (Long) cm.getJustificationList()
                                .stream()
                                .filter(o -> o instanceof Long)
                                .findFirst().get(),
                        (HardMediumSoftLongScore) cm.getScore()))
                .collect(toList());
    }

    public List<PublishedShiftReassignedPenalty>
            getPublishedShiftReassignedPenaltyList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        cm.getConstraintName().equals(CONSTRAINT_EMPLOYEE_IS_NOT_ORIGINAL_EMPLOYEE))
                .map(cm -> new PublishedShiftReassignedPenalty((Shift) cm.getJustificationList().get(0),
                        (HardMediumSoftLongScore) cm.getScore()))
                .collect(toList());
    }
}
