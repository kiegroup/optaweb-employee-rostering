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

    public static final String CONSTRAINT_COVID_LOW_RISK_EMPLOYEE = "Low-risk employee assigned to a COVID ward";
    public static final String CONSTRAINT_COVID_MODERATE_RISK_EMPLOYEE =
            "Moderate-risk employee assigned to a COVID ward";
    public static final String CONSTRAINT_COVID_HIGH_RISK_EMPLOYEE = "High-risk employee assigned to a COVID ward";
    public static final String CONSTRAINT_COVID_EXTREME_RISK_EMPLOYEE =
            "Extreme-risk employee assigned to a COVID ward";
    public static final String CONSTRAINT_COVID_INOCULATED_EMPLOYEE_OUTSIDE_COVID_WARD =
            "Inoculated employee outside a COVID ward";
    public static final String CONSTRAINT_COVID_UNIFORM_DISTRIBUTION_OF_INOCULATED_HOURS =
            "Uniform distribution of inoculated hours";
    public static final String CONSTRAINT_COVID_MAXIMIZE_INOCULATED_HOURS = "Maximize inoculated hours";
    public static final String CONSTRAINT_COVID_MIGRATION_BETWEEN_COVID_AND_NON_COVID_WARDS =
            "Migration between COVID and non-COVID wards";
    public static final String CONSTRAINT_COVID_NON_COVID_SHIFT_STARTED_SOON_AFTER_FINISHING_A_COVID_SHIFT =
            "Non-COVID shift started less than 8 hours after finishing a COVID shift";
    public static final String CONSTRAINT_REQUIRED_SKILL_FOR_A_SHIFT = "Required skill for a shift";
    public static final String CONSTRAINT_UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE =
            "Unavailable time slot for an employee";
    public static final String CONSTRAINT_AT_MOST_ONE_SHIFT_ASSIGNMENT_PER_DAY_PER_EMPLOYEE =
            "At most one shift assignment per day per employee";
    public static final String CONSTRAINT_NO_2_SHIFTS_WITHIN_10_HOURS_FROM_EACH_OTHER =
            "No 2 shifts within 10 hours from each other";
    public static final String CONSTRAINT_DAILY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM =
            "Daily minutes must not exceed contract maximum";
    public static final String CONSTRAINT_WEEKLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM =
            "Weekly minutes must not exceed contract maximum";
    public static final String CONSTRAINT_MONTHLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM =
            "Monthly minutes must not exceed contract maximum";
    public static final String CONSTRAINT_YEARLY_MINUTES_MUST_NOT_EXCEED_CONTRACT_MAXIMUM =
            "Yearly minutes must not exceed contract maximum";
    public static final String CONSTRAINT_ASSIGN_EVERY_SHIFT = "Assign every shift";
    public static final String CONSTRAINT_UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE = "Undesired time slot for an employee";
    public static final String CONSTRAINT_DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE = "Desired time slot for an employee";
    public static final String CONSTRAINT_EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE = "Employee is not rotation employee";

    // TODO: Is 999 a reasonable max for the weights?
    @NotNull
    private Integer undesiredTimeSlotWeight = 100;
    @NotNull
    private Integer desiredTimeSlotWeight = 10;
    @NotNull
    private Integer rotationEmployeeMatchWeight = 500;
    @NotNull
    private DayOfWeek weekStartDay = DayOfWeek.MONDAY;

    // COVID-specific constraints
    @ConstraintWeight(CONSTRAINT_COVID_LOW_RISK_EMPLOYEE)
    private HardMediumSoftLongScore lowRiskEmployeeInCovidWardMatchWeight = HardMediumSoftLongScore.ofSoft(1);
    @ConstraintWeight(CONSTRAINT_COVID_MODERATE_RISK_EMPLOYEE)
    private HardMediumSoftLongScore moderateRiskEmployeeInCovidWardMatchWeight = HardMediumSoftLongScore.ofSoft(5);
    @ConstraintWeight(CONSTRAINT_COVID_HIGH_RISK_EMPLOYEE)
    private HardMediumSoftLongScore highRiskEmployeeInCovidWardMatchWeight = HardMediumSoftLongScore.ofSoft(10);
    @ConstraintWeight(CONSTRAINT_COVID_EXTREME_RISK_EMPLOYEE)
    private HardMediumSoftLongScore extremeRiskEmployeeInCovidWardMatchWeight = HardMediumSoftLongScore.ofHard(1);
    @ConstraintWeight(CONSTRAINT_COVID_INOCULATED_EMPLOYEE_OUTSIDE_COVID_WARD)
    private HardMediumSoftLongScore inoculatedEmployeeOutsideCovidWardMatchWeight =
            HardMediumSoftLongScore.ofSoft(1000);
    @ConstraintWeight(CONSTRAINT_COVID_UNIFORM_DISTRIBUTION_OF_INOCULATED_HOURS)
    private HardMediumSoftLongScore uniformDistributionOfInoculatedHoursMatchWeight = HardMediumSoftLongScore.ofSoft(1);
    @ConstraintWeight(CONSTRAINT_COVID_MAXIMIZE_INOCULATED_HOURS)
    private HardMediumSoftLongScore maximizeInoculatedHoursMatchWeight = HardMediumSoftLongScore.ofSoft(10);
    @ConstraintWeight(CONSTRAINT_COVID_MIGRATION_BETWEEN_COVID_AND_NON_COVID_WARDS)
    private HardMediumSoftLongScore migrationBetweenCovidAndNonCovidWardMatchWeight =
            HardMediumSoftLongScore.ofSoft(10);
    @ConstraintWeight(CONSTRAINT_COVID_NON_COVID_SHIFT_STARTED_SOON_AFTER_FINISHING_A_COVID_SHIFT)
    private HardMediumSoftLongScore nonCovidShiftLessThan8HoursAfterCovidShiftMatchWeight =
            HardMediumSoftLongScore.ofHard(1);

    @ConstraintWeight(CONSTRAINT_REQUIRED_SKILL_FOR_A_SHIFT)
    private HardMediumSoftLongScore requiredSkill = HardMediumSoftLongScore.ofHard(100);
    @ConstraintWeight(CONSTRAINT_UNAVAILABLE_TIME_SLOT_FOR_AN_EMPLOYEE)
    private HardMediumSoftLongScore unavailableTimeSlot = HardMediumSoftLongScore.ofHard(50);
    @ConstraintWeight(CONSTRAINT_AT_MOST_ONE_SHIFT_ASSIGNMENT_PER_DAY_PER_EMPLOYEE)
    private HardMediumSoftLongScore oneShiftPerDay = HardMediumSoftLongScore.ofHard(10);
    @ConstraintWeight(CONSTRAINT_NO_2_SHIFTS_WITHIN_10_HOURS_FROM_EACH_OTHER)
    private HardMediumSoftLongScore noShiftsWithinTenHours = HardMediumSoftLongScore.ofHard(1);
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

    @ConstraintWeight(CONSTRAINT_UNDESIRED_TIME_SLOT_FOR_AN_EMPLOYEE)
    private HardMediumSoftLongScore undesiredTimeSlot = HardMediumSoftLongScore.ofSoft(1);
    @ConstraintWeight(CONSTRAINT_DESIRED_TIME_SLOT_FOR_AN_EMPLOYEE)
    private HardMediumSoftLongScore desiredTimeSlot = HardMediumSoftLongScore.ofSoft(1);
    @ConstraintWeight(CONSTRAINT_EMPLOYEE_IS_NOT_ROTATION_EMPLOYEE)
    private HardMediumSoftLongScore notRotationEmployee = HardMediumSoftLongScore.ZERO; // Disabled.

    @SuppressWarnings("unused")
    public RosterConstraintConfiguration() {
        super(-1);
    }

    public RosterConstraintConfiguration(Integer tenantId,
                                         Integer undesiredTimeSlotWeight, Integer desiredTimeSlotWeight,
                                         Integer rotationEmployeeMatchWeight, DayOfWeek weekStartDay) {
        super(tenantId);
        this.undesiredTimeSlotWeight = undesiredTimeSlotWeight;
        this.desiredTimeSlotWeight = desiredTimeSlotWeight;
        this.rotationEmployeeMatchWeight = rotationEmployeeMatchWeight;
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

    public Integer getUndesiredTimeSlotWeight() {
        return undesiredTimeSlotWeight;
    }

    public void setUndesiredTimeSlotWeight(Integer undesiredTimeSlotWeight) {
        this.undesiredTimeSlotWeight = undesiredTimeSlotWeight;
    }

    public Integer getDesiredTimeSlotWeight() {
        return desiredTimeSlotWeight;
    }

    public void setDesiredTimeSlotWeight(Integer desiredTimeSlotWeight) {
        this.desiredTimeSlotWeight = desiredTimeSlotWeight;
    }

    public Integer getRotationEmployeeMatchWeight() {
        return rotationEmployeeMatchWeight;
    }

    public void setRotationEmployeeMatchWeight(Integer rotationEmployeeMatchWeight) {
        this.rotationEmployeeMatchWeight = rotationEmployeeMatchWeight;
    }

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
