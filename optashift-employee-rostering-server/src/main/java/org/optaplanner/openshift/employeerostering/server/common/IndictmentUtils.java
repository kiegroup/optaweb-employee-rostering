package org.optaplanner.openshift.employeerostering.server.common;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.openshift.employeerostering.server.solver.WannabeSolverManager;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;
import org.optaplanner.openshift.employeerostering.shared.roster.Roster;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.violation.DesiredTimeslotForEmployeeReward;
import org.optaplanner.openshift.employeerostering.shared.violation.RequiredSkillViolation;
import org.optaplanner.openshift.employeerostering.shared.violation.RotationViolationPenalty;
import org.optaplanner.openshift.employeerostering.shared.violation.ShiftEmployeeConflict;
import org.optaplanner.openshift.employeerostering.shared.violation.UnassignedShiftPenalty;
import org.optaplanner.openshift.employeerostering.shared.violation.UnavailableEmployeeViolation;
import org.optaplanner.openshift.employeerostering.shared.violation.UndesiredTimeslotForEmployeePenalty;

@Singleton
public class IndictmentUtils {

    @Inject
    private WannabeSolverManager solverManager;

    private static final String CONSTRAINT_MATCH_PACKAGE = "org.optaplanner.openshift.employeerostering.server.solver";

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
                             (indictment != null) ? (HardMediumSoftLongScore) indictment.getScore() : HardMediumSoftLongScore.ZERO);
                             
    }

    public List<RequiredSkillViolation> getRequiredSkillViolationList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                         .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) && cm.getConstraintName().equals("Required skill for a shift"))
                         .map(cm -> new RequiredSkillViolation((Shift) cm.getJustificationList().get(0), (HardMediumSoftLongScore) cm.getScore()))
                         .collect(Collectors.toList());
    }

    public List<UnavailableEmployeeViolation> getUnavailableEmployeeViolationList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                         .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) && cm.getConstraintName().equals("Unavailable time slot for an employee"))
                         .map(cm -> new UnavailableEmployeeViolation((Shift) cm.getJustificationList().get(0), (EmployeeAvailability) cm.getJustificationList().get(1), (HardMediumSoftLongScore) cm.getScore()))
                         .collect(Collectors.toList());
    }

    public List<DesiredTimeslotForEmployeeReward> getDesiredTimeslotForEmployeeRewardList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                         .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) && cm.getConstraintName().equals("Desired time slot for an employee"))
                         .map(cm -> new DesiredTimeslotForEmployeeReward((Shift) cm.getJustificationList().get(0), (EmployeeAvailability) cm.getJustificationList().get(1), (HardMediumSoftLongScore) cm.getScore()))
                         .collect(Collectors.toList());
    }

    public List<UndesiredTimeslotForEmployeePenalty> getUndesiredTimeslotForEmployeePenaltyList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                         .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) && cm.getConstraintName().equals("Undesired time slot for an employee"))
                         .map(cm -> new UndesiredTimeslotForEmployeePenalty((Shift) cm.getJustificationList().get(0), (EmployeeAvailability) cm.getJustificationList().get(1), (HardMediumSoftLongScore) cm.getScore()))
                         .collect(Collectors.toList());
    }

    public List<ShiftEmployeeConflict> getShiftEmployeeConflictList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                         .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) && cm.getConstraintName().equals("At most one shift assignment per day per employee") || cm.getConstraintName().equals(
                                                                                                                                                                                                                         "No 2 shifts within 10 hours from each other"))
                         .map(cm -> new ShiftEmployeeConflict((Shift) cm.getJustificationList().get(0), (Shift) cm.getJustificationList().get(1), (HardMediumSoftLongScore) cm.getScore()))
                         .collect(Collectors.toList());
    }

    public List<RotationViolationPenalty> getRotationViolationPenaltyList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                         .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) && cm.getConstraintName().equals("Employee is not rotation employee"))
                         .map(cm -> new RotationViolationPenalty((Shift) cm.getJustificationList().get(0), (HardMediumSoftLongScore) cm.getScore()))
                         .collect(Collectors.toList());
    }
    
    public List<UnassignedShiftPenalty> getUnassignedShiftPenaltyList(Indictment indictment) {
        if (indictment == null) {
            return Collections.emptyList();
        }
        return indictment.getConstraintMatchSet().stream()
                         .filter(cm -> cm.getConstraintPackage().equals(CONSTRAINT_MATCH_PACKAGE) && cm.getConstraintName().equals("Assign every shift"))
                         .map(cm -> new UnassignedShiftPenalty((Shift) cm.getJustificationList().get(0), (HardMediumSoftLongScore) cm.getScore()))
                         .collect(Collectors.toList());
    }
}
