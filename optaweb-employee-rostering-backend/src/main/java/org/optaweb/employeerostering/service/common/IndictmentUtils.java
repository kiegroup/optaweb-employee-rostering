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
import static org.optaweb.employeerostering.service.solver.EmployeeRosteringConstraintProvider.extractFirstDayOfWeek;

import java.time.Duration;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.constraint.ConstraintMatch;
import org.optaplanner.core.api.score.constraint.ConstraintMatchTotal;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.EmployeeAvailability;
import org.optaweb.employeerostering.domain.roster.Roster;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;
import org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration;
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

@ApplicationScoped
public class IndictmentUtils {

    public static final String CONSTRAINT_MATCH_PACKAGE = "org.optaweb.employeerostering.service.solver";
    private ScoreManager<Roster, HardMediumSoftLongScore> scoreManager;

    @Inject
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
                        ConstraintMatchTotal::getScore)));
        return out;
    }

    public ShiftView getShiftViewWithIndictment(ZoneId zoneId, Shift shift, RosterConstraintConfiguration configuration,
            Indictment<HardMediumSoftLongScore> shiftIndictment,
            Indictment<HardMediumSoftLongScore> employeeIndictment) {
        List<ContractMinutesViolation> contractMinutesViolationList =
                getContractMinutesViolationList(shift, configuration, employeeIndictment);
        HardMediumSoftLongScore totalImpactOnScore = HardMediumSoftLongScore.ZERO;
        if (shiftIndictment != null) {
            totalImpactOnScore = shiftIndictment.getScore();
        }
        for (ContractMinutesViolation contractMinutesViolation : contractMinutesViolationList) {
            totalImpactOnScore = totalImpactOnScore.add(contractMinutesViolation.getScore());
        }
        return new ShiftView(zoneId, shift,
                getRequiredSkillViolationList(shiftIndictment),
                getUnavailableEmployeeViolationList(shiftIndictment),
                getShiftEmployeeConflictList(shiftIndictment),
                getDesiredTimeslotForEmployeeRewardList(shiftIndictment),
                getUndesiredTimeslotForEmployeePenaltyList(shiftIndictment),
                getRotationViolationPenaltyList(shiftIndictment),
                getUnassignedShiftPenaltyList(shiftIndictment),
                contractMinutesViolationList,
                getNoBreakViolationList(shiftIndictment),
                getPublishedShiftReassignedPenaltyList(shiftIndictment),
                totalImpactOnScore);
    }

    public List<RequiredSkillViolation> getRequiredSkillViolationList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(constraintMatch -> constraintMatch.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        constraintMatch.getConstraintName().equals(CONSTRAINT_REQUIRED_SKILL_FOR_A_SHIFT))
                .map(constraintMatch -> new RequiredSkillViolation((Shift) constraintMatch.getJustificationList().get(0),
                        constraintMatch.getScore()))
                .collect(toList());
    }

    public List<UnavailableEmployeeViolation>
            getUnavailableEmployeeViolationList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(constraintMatch -> constraintMatch.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        constraintMatch.getConstraintName().equals(CONSTRAINT_UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE))
                .map(constraintMatch -> new UnavailableEmployeeViolation((Shift) constraintMatch.getJustificationList().get(1),
                        (EmployeeAvailability) constraintMatch.getJustificationList().get(0),
                        constraintMatch.getScore()))
                .collect(toList());
    }

    public List<DesiredTimeslotForEmployeeReward>
            getDesiredTimeslotForEmployeeRewardList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(constraintMatch -> constraintMatch.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        constraintMatch.getConstraintName().equals(CONSTRAINT_DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE))
                .map(constraintMatch -> new DesiredTimeslotForEmployeeReward(
                        (Shift) constraintMatch.getJustificationList().get(1),
                        (EmployeeAvailability) constraintMatch.getJustificationList().get(0),
                        constraintMatch.getScore()))
                .collect(toList());
    }

    public List<UndesiredTimeslotForEmployeePenalty>
            getUndesiredTimeslotForEmployeePenaltyList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(constraintMatch -> constraintMatch.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        constraintMatch.getConstraintName().equals(CONSTRAINT_UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE))
                .map(constraintMatch -> new UndesiredTimeslotForEmployeePenalty(
                        (Shift) constraintMatch.getJustificationList().get(1),
                        (EmployeeAvailability) constraintMatch.getJustificationList()
                                .get(0),
                        constraintMatch.getScore()))
                .collect(toList());
    }

    public List<ShiftEmployeeConflict> getShiftEmployeeConflictList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        List<String> employeeShiftConstraintNameList =
                Arrays.asList(CONSTRAINT_BREAK_BETWEEN_NON_CONSECUTIVE_SHIFTS, CONSTRAINT_NO_OVERLAPPING_SHIFTS);
        return indictment.getConstraintMatchSet().stream()
                .filter(constraintMatch -> constraintMatch.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        employeeShiftConstraintNameList.contains(constraintMatch.getConstraintName()))
                .map(constraintMatch -> new ShiftEmployeeConflict((Shift) constraintMatch.getJustificationList().get(0),
                        (Shift) constraintMatch.getJustificationList().get(1), constraintMatch.getScore()))
                .collect(toList());
    }

    public List<NoBreakViolation> getNoBreakViolationList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(constraintMatch -> constraintMatch.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        constraintMatch.getConstraintName().equals(CONSTRAINT_NO_MORE_THAN_2_CONSECUTIVE_SHIFTS))
                .map(constraintMatch -> new NoBreakViolation((Shift) constraintMatch.getJustificationList().get(0),
                        (Shift) constraintMatch.getJustificationList().get(1),
                        (Shift) constraintMatch.getJustificationList().get(2),
                        constraintMatch.getScore()))
                .collect(toList());
    }

    public List<RotationViolationPenalty> getRotationViolationPenaltyList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(constraintMatch -> constraintMatch.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        constraintMatch.getConstraintName().equals(CONSTRAINT_EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE))
                .map(constraintMatch -> new RotationViolationPenalty((Shift) constraintMatch.getJustificationList().get(0),
                        constraintMatch.getScore()))
                .collect(toList());
    }

    public List<UnassignedShiftPenalty> getUnassignedShiftPenaltyList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(constraintMatch -> constraintMatch.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        constraintMatch.getConstraintName().equals(CONSTRAINT_ASSIGN_EVERY_SHIFT))
                .map(constraintMatch -> new UnassignedShiftPenalty((Shift) constraintMatch.getJustificationList().get(0),
                        constraintMatch.getScore()))
                .collect(toList());
    }

    private static Predicate<ConstraintMatch> shiftImpactsConstraintMatch(Shift shift,
            RosterConstraintConfiguration configuration) {
        return constraintMatch -> {
            Object groupKey;
            switch (constraintMatch.getConstraintName()) {
                case CONSTRAINT_DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM:
                    groupKey = shift.getStartDateTime().toLocalDate();
                    break;
                case CONSTRAINT_WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM:
                    groupKey = extractFirstDayOfWeek(configuration.getWeekStartDay(),
                            shift.getStartDateTime());
                    break;
                case CONSTRAINT_MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM:
                    groupKey = YearMonth.from(shift.getStartDateTime());
                    break;
                case CONSTRAINT_YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM:
                    groupKey = shift.getStartDateTime().getYear();
                    break;
                default:
                    throw new IllegalStateException("Unhandled constraint (" + constraintMatch.getConstraintName() + ").");
            }
            return constraintMatch.getJustificationList().get(1).equals(groupKey);
        };
    }

    public List<ContractMinutesViolation> getContractMinutesViolationList(
            Shift shift,
            RosterConstraintConfiguration configuration,
            Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        List<String> contractMinutesConstraintNameList = Arrays.asList(
                CONSTRAINT_DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                CONSTRAINT_WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                CONSTRAINT_MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM,
                CONSTRAINT_YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM);
        return indictment.getConstraintMatchSet().stream()
                .filter(constraintMatch -> constraintMatch.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        contractMinutesConstraintNameList.contains(constraintMatch.getConstraintName()))
                .filter(shiftImpactsConstraintMatch(shift, configuration))
                .map(constraintMatch -> new ContractMinutesViolation((Employee) constraintMatch.getJustificationList().get(0),
                        ContractMinutesViolation.Type.getTypeForViolation(constraintMatch.getConstraintName()),
                        ((Duration) constraintMatch.getJustificationList().get(2)).toMinutes(),
                        constraintMatch.getScore()))
                .collect(Collectors.toList());
    }

    public List<PublishedShiftReassignedPenalty>
            getPublishedShiftReassignedPenaltyList(Indictment<HardMediumSoftLongScore> indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                .filter(constraintMatch -> constraintMatch.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) &&
                        constraintMatch.getConstraintName().equals(CONSTRAINT_EMPLOYEE_IS_NOT_ORIGINAL_EMPLOYEE))
                .map(constraintMatch -> new PublishedShiftReassignedPenalty(
                        (Shift) constraintMatch.getJustificationList().get(0),
                        constraintMatch.getScore()))
                .collect(toList());
    }
}
