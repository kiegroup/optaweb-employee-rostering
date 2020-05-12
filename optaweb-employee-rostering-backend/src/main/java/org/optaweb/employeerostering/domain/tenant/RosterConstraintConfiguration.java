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

    @NotNull
    private DayOfWeek weekStartDay = DayOfWeek.MONDAY;

    @ConstraintWeight("Required skill for a shift")
    private HardMediumSoftLongScore requiredSkill = HardMediumSoftLongScore.ofHard(100);
    @ConstraintWeight("Unavailable time slot for an employee")
    private HardMediumSoftLongScore unavailableTimeSlot = HardMediumSoftLongScore.ofHard(50);
    @ConstraintWeight("No overlapping shifts")
    private HardMediumSoftLongScore noOverlappingShifts = HardMediumSoftLongScore.ofHard(20);
    @ConstraintWeight("No more than 2 consecutive shifts")
    private HardMediumSoftLongScore noMoreThan2ConsecutiveShifts = HardMediumSoftLongScore.ofHard(10);
    @ConstraintWeight("Break between non-consecutive shifts is at least 10 hours")
    private HardMediumSoftLongScore breakBetweenNonConsecutiveShiftsAtLeast10Hours = HardMediumSoftLongScore.ofHard(1);
    @ConstraintWeight("Daily minutes must not exceed contract maximum")
    private HardMediumSoftLongScore contractMaximumDailyMinutes = HardMediumSoftLongScore.ofHard(1);
    @ConstraintWeight("Weekly minutes must not exceed contract maximum")
    private HardMediumSoftLongScore contractMaximumWeeklyMinutes = HardMediumSoftLongScore.ofHard(1);
    @ConstraintWeight("Monthly minutes must not exceed contract maximum")
    private HardMediumSoftLongScore contractMaximumMonthlyMinutes = HardMediumSoftLongScore.ofHard(1);
    @ConstraintWeight("Yearly minutes must not exceed contract maximum")
    private HardMediumSoftLongScore contractMaximumYearlyMinutes = HardMediumSoftLongScore.ofHard(1);

    @ConstraintWeight("Assign every shift")
    private HardMediumSoftLongScore assignEveryShift = HardMediumSoftLongScore.ofMedium(1);

    @ConstraintWeight("Employee is not original employee")
    private HardMediumSoftLongScore notOriginalEmployee = HardMediumSoftLongScore.ofSoft(100_000_000_000L);
    @ConstraintWeight("Undesired time slot for an employee")
    private HardMediumSoftLongScore undesiredTimeSlot = HardMediumSoftLongScore.ofSoft(20);
    @ConstraintWeight("Desired time slot for an employee")
    private HardMediumSoftLongScore desiredTimeSlot = HardMediumSoftLongScore.ofSoft(10);
    @ConstraintWeight("Employee is not rotation employee")
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
