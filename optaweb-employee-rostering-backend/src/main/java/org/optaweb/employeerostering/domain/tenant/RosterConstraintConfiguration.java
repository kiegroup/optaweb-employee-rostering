package org.optaweb.employeerostering.domain.tenant;

import java.time.DayOfWeek;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.optaplanner.core.api.domain.constraintweight.ConstraintConfiguration;
import org.optaplanner.core.api.domain.constraintweight.ConstraintWeight;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.domain.common.AbstractPersistable;

@Entity
@ConstraintConfiguration(constraintPackage = "org.optaweb.employeerostering.service.solver")
public class RosterConstraintConfiguration extends AbstractPersistable {

    public static final String CONSTRAINT_REQUIRED_SKILL_FOR_A_SHIFT = "Required skill for a shift";
    public static final String CONSTRAINT_UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE =
            "Unavailable time slot for an employee";
    public static final String CONSTRAINT_NO_OVERLAPPING_SHIFTS = "No overlapping shifts";
    public static final String CONSTRAINT_NO_MORE_THAN_2_CONSECUTIVE_SHIFTS = "No more than 2 consecutive shifts";
    public static final String CONSTRAINT_BREAK_BETWEEN_NON_CONSECUTIVE_SHIFTS =
            "Break between non-consecutive shifts is at least 10 hours";
    public static final String CONSTRAINT_DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM =
            "Daily minutes must not exceed contract maximum";
    public static final String CONSTRAINT_WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM =
            "Weekly minutes must not exceed contract maximum";
    public static final String CONSTRAINT_MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM =
            "Monthly minutes must not exceed contract maximum";
    public static final String CONSTRAINT_YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM =
            "Yearly minutes must not exceed contract maximum";
    public static final String CONSTRAINT_ASSIGN_EVERY_SHIFT = "Assign every shift";
    public static final String CONSTRAINT_EMPLOYEE_IS_NOT_ORIGINAL_EMPLOYEE = "Employee is not original employee";
    public static final String CONSTRAINT_UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE = "Undesired time slot for an employee";
    public static final String CONSTRAINT_DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE = "Desired time slot for an employee";
    public static final String CONSTRAINT_EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE = "Employee is not rotation employee";

    @NotNull
    private DayOfWeek weekStartDay = DayOfWeek.MONDAY;

    @ConstraintWeight(CONSTRAINT_REQUIRED_SKILL_FOR_A_SHIFT)
    private HardMediumSoftLongScore requiredSkill = HardMediumSoftLongScore.ofHard(100);
    @ConstraintWeight(CONSTRAINT_UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE)
    private HardMediumSoftLongScore unavailableTimeSlot = HardMediumSoftLongScore.ofHard(50);
    @ConstraintWeight(CONSTRAINT_NO_OVERLAPPING_SHIFTS)
    private HardMediumSoftLongScore noOverlappingShifts = HardMediumSoftLongScore.ofHard(20);
    @ConstraintWeight(CONSTRAINT_NO_MORE_THAN_2_CONSECUTIVE_SHIFTS)
    private HardMediumSoftLongScore noMoreThan2ConsecutiveShifts = HardMediumSoftLongScore.ofHard(10);
    @ConstraintWeight(CONSTRAINT_BREAK_BETWEEN_NON_CONSECUTIVE_SHIFTS)
    private HardMediumSoftLongScore breakBetweenNonConsecutiveShiftsAtLeast10Hours = HardMediumSoftLongScore.ofHard(1);
    @ConstraintWeight(CONSTRAINT_DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM)
    private HardMediumSoftLongScore contractMaximumDailyMinutes = HardMediumSoftLongScore.ofHard(1);
    @ConstraintWeight(CONSTRAINT_WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM)
    private HardMediumSoftLongScore contractMaximumWeeklyMinutes = HardMediumSoftLongScore.ofHard(1);
    @ConstraintWeight(CONSTRAINT_MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM)
    private HardMediumSoftLongScore contractMaximumMonthlyMinutes = HardMediumSoftLongScore.ofHard(1);
    @ConstraintWeight(CONSTRAINT_YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM)
    private HardMediumSoftLongScore contractMaximumYearlyMinutes = HardMediumSoftLongScore.ofHard(1);

    @ConstraintWeight(CONSTRAINT_ASSIGN_EVERY_SHIFT)
    private HardMediumSoftLongScore assignEveryShift = HardMediumSoftLongScore.ofMedium(1);

    @ConstraintWeight(CONSTRAINT_EMPLOYEE_IS_NOT_ORIGINAL_EMPLOYEE)
    private HardMediumSoftLongScore notOriginalEmployee = HardMediumSoftLongScore.ofSoft(100_000_000_000L);
    @ConstraintWeight(CONSTRAINT_UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE)
    private HardMediumSoftLongScore undesiredTimeSlot = HardMediumSoftLongScore.ofSoft(20);
    @ConstraintWeight(CONSTRAINT_DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE)
    private HardMediumSoftLongScore desiredTimeSlot = HardMediumSoftLongScore.ofSoft(10);
    @ConstraintWeight(CONSTRAINT_EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE)
    private HardMediumSoftLongScore notRotationEmployee = HardMediumSoftLongScore.ofSoft(50);

    @SuppressWarnings("unused")
    public RosterConstraintConfiguration() {
        super(-1);
    }

    public RosterConstraintConfiguration(Integer tenantId, DayOfWeek weekStartDay) {
        super(tenantId);
        this.weekStartDay = weekStartDay;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public DayOfWeek getWeekStartDay() {
        return weekStartDay;
    }

    public void setWeekStartDay(DayOfWeek weekStartDay) {
        this.weekStartDay = weekStartDay;
    }

    public HardMediumSoftLongScore getRequiredSkill() {
        return requiredSkill;
    }

    public void setRequiredSkill(HardMediumSoftLongScore requiredSkill) {
        this.requiredSkill = requiredSkill;
    }

    public HardMediumSoftLongScore getUnavailableTimeSlot() {
        return unavailableTimeSlot;
    }

    public void setUnavailableTimeSlot(HardMediumSoftLongScore unavailableTimeSlot) {
        this.unavailableTimeSlot = unavailableTimeSlot;
    }

    public HardMediumSoftLongScore getNoOverlappingShifts() {
        return noOverlappingShifts;
    }

    public void setNoOverlappingShifts(HardMediumSoftLongScore noOverlappingShifts) {
        this.noOverlappingShifts = noOverlappingShifts;
    }

    public HardMediumSoftLongScore getNoMoreThan2ConsecutiveShifts() {
        return noMoreThan2ConsecutiveShifts;
    }

    public void setNoMoreThan2ConsecutiveShifts(HardMediumSoftLongScore noMoreThan2ConsecutiveShifts) {
        this.noMoreThan2ConsecutiveShifts = noMoreThan2ConsecutiveShifts;
    }

    public HardMediumSoftLongScore getBreakBetweenNonConsecutiveShiftsAtLeast10Hours() {
        return breakBetweenNonConsecutiveShiftsAtLeast10Hours;
    }

    public void setBreakBetweenNonConsecutiveShiftsAtLeast10Hours(
            HardMediumSoftLongScore breakBetweenNonConsecutiveShiftsAtLeast10Hours) {
        this.breakBetweenNonConsecutiveShiftsAtLeast10Hours = breakBetweenNonConsecutiveShiftsAtLeast10Hours;
    }

    public HardMediumSoftLongScore getContractMaximumDailyMinutes() {
        return contractMaximumDailyMinutes;
    }

    public void setContractMaximumDailyMinutes(HardMediumSoftLongScore contractMaximumDailyMinutes) {
        this.contractMaximumDailyMinutes = contractMaximumDailyMinutes;
    }

    public HardMediumSoftLongScore getContractMaximumWeeklyMinutes() {
        return contractMaximumWeeklyMinutes;
    }

    public void setContractMaximumWeeklyMinutes(HardMediumSoftLongScore contractMaximumWeeklyMinutes) {
        this.contractMaximumWeeklyMinutes = contractMaximumWeeklyMinutes;
    }

    public HardMediumSoftLongScore getContractMaximumMonthlyMinutes() {
        return contractMaximumMonthlyMinutes;
    }

    public void setContractMaximumMonthlyMinutes(HardMediumSoftLongScore contractMaximumMonthlyMinutes) {
        this.contractMaximumMonthlyMinutes = contractMaximumMonthlyMinutes;
    }

    public HardMediumSoftLongScore getContractMaximumYearlyMinutes() {
        return contractMaximumYearlyMinutes;
    }

    public void setContractMaximumYearlyMinutes(HardMediumSoftLongScore contractMaximumYearlyMinutes) {
        this.contractMaximumYearlyMinutes = contractMaximumYearlyMinutes;
    }

    public HardMediumSoftLongScore getAssignEveryShift() {
        return assignEveryShift;
    }

    public void setAssignEveryShift(HardMediumSoftLongScore assignEveryShift) {
        this.assignEveryShift = assignEveryShift;
    }

    public HardMediumSoftLongScore getNotOriginalEmployee() {
        return notOriginalEmployee;
    }

    public void setNotOriginalEmployee(HardMediumSoftLongScore notOriginalEmployee) {
        this.notOriginalEmployee = notOriginalEmployee;
    }

    public HardMediumSoftLongScore getUndesiredTimeSlot() {
        return undesiredTimeSlot;
    }

    public void setUndesiredTimeSlot(HardMediumSoftLongScore undesiredTimeSlot) {
        this.undesiredTimeSlot = undesiredTimeSlot;
    }

    public HardMediumSoftLongScore getDesiredTimeSlot() {
        return desiredTimeSlot;
    }

    public void setDesiredTimeSlot(HardMediumSoftLongScore desiredTimeSlot) {
        this.desiredTimeSlot = desiredTimeSlot;
    }

    public HardMediumSoftLongScore getNotRotationEmployee() {
        return notRotationEmployee;
    }

    public void setNotRotationEmployee(HardMediumSoftLongScore notRotationEmployee) {
        this.notRotationEmployee = notRotationEmployee;
    }
}
