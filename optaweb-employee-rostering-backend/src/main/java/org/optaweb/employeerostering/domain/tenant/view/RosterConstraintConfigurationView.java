package org.optaweb.employeerostering.domain.tenant.view;

import java.time.DayOfWeek;

import javax.validation.constraints.NotNull;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.domain.common.AbstractPersistable;

public class RosterConstraintConfigurationView extends AbstractPersistable {

    @NotNull
    private DayOfWeek weekStartDay = DayOfWeek.MONDAY;

    private HardMediumSoftLongScore requiredSkill = HardMediumSoftLongScore.ofHard(100);
    private HardMediumSoftLongScore unavailableTimeSlot = HardMediumSoftLongScore.ofHard(50);
    private HardMediumSoftLongScore noOverlappingShifts = HardMediumSoftLongScore.ofHard(20);
    private HardMediumSoftLongScore noMoreThan2ConsecutiveShifts = HardMediumSoftLongScore.ofHard(10);
    private HardMediumSoftLongScore breakBetweenNonConsecutiveShiftsAtLeast10Hours = HardMediumSoftLongScore.ofHard(1);
    private HardMediumSoftLongScore contractMaximumDailyMinutes = HardMediumSoftLongScore.ofHard(1);
    private HardMediumSoftLongScore contractMaximumWeeklyMinutes = HardMediumSoftLongScore.ofHard(1);
    private HardMediumSoftLongScore contractMaximumMonthlyMinutes = HardMediumSoftLongScore.ofHard(1);
    private HardMediumSoftLongScore contractMaximumYearlyMinutes = HardMediumSoftLongScore.ofHard(1);
    private HardMediumSoftLongScore assignEveryShift = HardMediumSoftLongScore.ofMedium(1);
    private HardMediumSoftLongScore notOriginalEmployee = HardMediumSoftLongScore.ofSoft(100_000_000_000L);
    private HardMediumSoftLongScore undesiredTimeSlot = HardMediumSoftLongScore.ofSoft(1);
    private HardMediumSoftLongScore desiredTimeSlot = HardMediumSoftLongScore.ofSoft(1);
    private HardMediumSoftLongScore notRotationEmployee = HardMediumSoftLongScore.ofSoft(1);

    @SuppressWarnings("unused")
    public RosterConstraintConfigurationView() {
        super(-1);
    }

    public RosterConstraintConfigurationView(Integer tenantId, DayOfWeek weekStartDay) {
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

    public void setNoMoreThan2ConsecutiveShifts(
            HardMediumSoftLongScore noMoreThan2ConsecutiveShifts) {
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
