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

    // COVID-specific constraints
    @ConstraintWeight("Low-risk employee assigned to a COVID ward")
    private HardMediumSoftLongScore lowRiskEmployeeInCovidWardMatchWeight = HardMediumSoftLongScore.ofSoft(10);
    @ConstraintWeight("Moderate-risk employee assigned to a COVID ward")
    private HardMediumSoftLongScore moderateRiskEmployeeInCovidWardMatchWeight = HardMediumSoftLongScore.ofSoft(50);
    @ConstraintWeight("High-risk employee assigned to a COVID ward")
    private HardMediumSoftLongScore highRiskEmployeeInCovidWardMatchWeight = HardMediumSoftLongScore.ofSoft(100);
    @ConstraintWeight("Extreme-risk employee assigned to a COVID ward")
    private HardMediumSoftLongScore extremeRiskEmployeeInCovidWardMatchWeight = HardMediumSoftLongScore.ofHard(1);
    @ConstraintWeight("Inoculated employee outside a COVID ward")
    private HardMediumSoftLongScore inoculatedEmployeeOutsideCovidWardMatchWeight =
            HardMediumSoftLongScore.ofSoft(1_000);
    @ConstraintWeight("Uniform distribution of inoculated hours")
    private HardMediumSoftLongScore uniformDistributionOfInoculatedHoursMatchWeight =
            HardMediumSoftLongScore.ofSoft(1);
    @ConstraintWeight("Maximize inoculated hours")
    private HardMediumSoftLongScore maximizeInoculatedHoursMatchWeight = HardMediumSoftLongScore.ofSoft(50);
    @ConstraintWeight("Migration between COVID and non-COVID wards")
    private HardMediumSoftLongScore migrationBetweenCovidAndNonCovidWardMatchWeight =
            HardMediumSoftLongScore.ofSoft(100);
    @ConstraintWeight("Non-COVID shift started less than 8 hours after finishing a COVID shift")
    private HardMediumSoftLongScore nonCovidShiftLessThan8HoursAfterCovidShiftMatchWeight =
            HardMediumSoftLongScore.ofHard(1);

    @ConstraintWeight("Required skill for a shift")
    private HardMediumSoftLongScore requiredSkill = HardMediumSoftLongScore.ofHard(100);
    @ConstraintWeight("Unavailable time slot for an employee")
    private HardMediumSoftLongScore unavailableTimeSlot = HardMediumSoftLongScore.ofHard(50);
    @ConstraintWeight("At most one shift assignment per day per employee")
    private HardMediumSoftLongScore oneShiftPerDay = HardMediumSoftLongScore.ofHard(10);
    @ConstraintWeight("No 2 shifts within 10 hours from each other")
    private HardMediumSoftLongScore noShiftsWithinTenHours = HardMediumSoftLongScore.ofHard(1);
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
    // COVID-specific getters and setters
    // ************************************************************************

    public HardMediumSoftLongScore getLowRiskEmployeeInCovidWardMatchWeight() {
        return lowRiskEmployeeInCovidWardMatchWeight;
    }

    public void setLowRiskEmployeeInCovidWardMatchWeight(
            HardMediumSoftLongScore lowRiskEmployeeInCovidWardMatchWeight) {
        this.lowRiskEmployeeInCovidWardMatchWeight = lowRiskEmployeeInCovidWardMatchWeight;
    }

    public HardMediumSoftLongScore getModerateRiskEmployeeInCovidWardMatchWeight() {
        return moderateRiskEmployeeInCovidWardMatchWeight;
    }

    public void setModerateRiskEmployeeInCovidWardMatchWeight(
            HardMediumSoftLongScore moderateRiskEmployeeInCovidWardMatchWeight) {
        this.moderateRiskEmployeeInCovidWardMatchWeight = moderateRiskEmployeeInCovidWardMatchWeight;
    }

    public HardMediumSoftLongScore getHighRiskEmployeeInCovidWardMatchWeight() {
        return highRiskEmployeeInCovidWardMatchWeight;
    }

    public void setHighRiskEmployeeInCovidWardMatchWeight(
            HardMediumSoftLongScore highRiskEmployeeInCovidWardMatchWeight) {
        this.highRiskEmployeeInCovidWardMatchWeight = highRiskEmployeeInCovidWardMatchWeight;
    }

    public HardMediumSoftLongScore getExtremeRiskEmployeeInCovidWardMatchWeight() {
        return extremeRiskEmployeeInCovidWardMatchWeight;
    }

    public void setExtremeRiskEmployeeInCovidWardMatchWeight(
            HardMediumSoftLongScore extremeRiskEmployeeInCovidWardMatchWeight) {
        this.extremeRiskEmployeeInCovidWardMatchWeight = extremeRiskEmployeeInCovidWardMatchWeight;
    }

    public HardMediumSoftLongScore getInoculatedEmployeeOutsideCovidWardMatchWeight() {
        return inoculatedEmployeeOutsideCovidWardMatchWeight;
    }

    public void setInoculatedEmployeeOutsideCovidWardMatchWeight(
            HardMediumSoftLongScore inoculatedEmployeeOutsideCovidWardMatchWeight) {
        this.inoculatedEmployeeOutsideCovidWardMatchWeight = inoculatedEmployeeOutsideCovidWardMatchWeight;
    }

    public HardMediumSoftLongScore getUniformDistributionOfInoculatedHoursMatchWeight() {
        return uniformDistributionOfInoculatedHoursMatchWeight;
    }

    public void setUniformDistributionOfInoculatedHoursMatchWeight(
            HardMediumSoftLongScore uniformDistributionOfInoculatedHoursMatchWeight) {
        this.uniformDistributionOfInoculatedHoursMatchWeight = uniformDistributionOfInoculatedHoursMatchWeight;
    }

    public HardMediumSoftLongScore getMaximizeInoculatedHoursMatchWeight() {
        return maximizeInoculatedHoursMatchWeight;
    }

    public void setMaximizeInoculatedHoursMatchWeight(HardMediumSoftLongScore maximizeInoculatedHoursMatchWeight) {
        this.maximizeInoculatedHoursMatchWeight = maximizeInoculatedHoursMatchWeight;
    }

    public HardMediumSoftLongScore getMigrationBetweenCovidAndNonCovidWardMatchWeight() {
        return migrationBetweenCovidAndNonCovidWardMatchWeight;
    }

    public void setMigrationBetweenCovidAndNonCovidWardMatchWeight(
            HardMediumSoftLongScore migrationBetweenCovidAndNonCovidWardMatchWeight) {
        this.migrationBetweenCovidAndNonCovidWardMatchWeight = migrationBetweenCovidAndNonCovidWardMatchWeight;
    }

    public HardMediumSoftLongScore getNonCovidShiftLessThan8HoursAfterCovidShiftMatchWeight() {
        return nonCovidShiftLessThan8HoursAfterCovidShiftMatchWeight;
    }

    public void setNonCovidShiftLessThan8HoursAfterCovidShiftMatchWeight(
            HardMediumSoftLongScore nonCovidShiftLessThan8HoursAfterCovidShiftMatchWeight) {
        this.nonCovidShiftLessThan8HoursAfterCovidShiftMatchWeight =
                nonCovidShiftLessThan8HoursAfterCovidShiftMatchWeight;
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

    public HardMediumSoftLongScore getOneShiftPerDay() {
        return oneShiftPerDay;
    }

    public void setOneShiftPerDay(HardMediumSoftLongScore oneShiftPerDay) {
        this.oneShiftPerDay = oneShiftPerDay;
    }

    public HardMediumSoftLongScore getNoShiftsWithinTenHours() {
        return noShiftsWithinTenHours;
    }

    public void setNoShiftsWithinTenHours(HardMediumSoftLongScore noShiftsWithinTenHours) {
        this.noShiftsWithinTenHours = noShiftsWithinTenHours;
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
