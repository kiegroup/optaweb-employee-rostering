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

package org.optaweb.employeerostering.domain.tenant.view;

import java.time.DayOfWeek;

import javax.validation.constraints.NotNull;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.domain.common.AbstractPersistable;

public class RosterConstraintConfigurationView extends AbstractPersistable {

    @NotNull
    private Integer undesiredTimeSlotWeight = 100;
    @NotNull
    private Integer desiredTimeSlotWeight = 10;
    @NotNull
    private Integer rotationEmployeeMatchWeight = 500;
    @NotNull
    private DayOfWeek weekStartDay = DayOfWeek.MONDAY;

    // COVID-specific constraints
    private HardMediumSoftLongScore lowRiskEmployeeInCovidWard = HardMediumSoftLongScore.ofSoft(1);
    private HardMediumSoftLongScore moderateRiskEmployeeInCovidWard = HardMediumSoftLongScore.ofSoft(5);
    private HardMediumSoftLongScore highRiskEmployeeInCovidWard = HardMediumSoftLongScore.ofSoft(10);
    private HardMediumSoftLongScore extremeRiskEmployeeInCovidWard = HardMediumSoftLongScore.ofHard(1);
    private HardMediumSoftLongScore inoculatedEmployeeOutsideCovidWard = HardMediumSoftLongScore.ofSoft(1000);
    private HardMediumSoftLongScore maximizeInoculatedHours = HardMediumSoftLongScore.ofSoft(10);
    private HardMediumSoftLongScore migrationBetweenCovidAndNonCovidWard = HardMediumSoftLongScore.ofSoft(10);
    private HardMediumSoftLongScore nonCovidShiftLessThan8HoursAfterCovidShift = HardMediumSoftLongScore.ofHard(1);

    private HardMediumSoftLongScore requiredSkill = HardMediumSoftLongScore.ofHard(100);
    private HardMediumSoftLongScore unavailableTimeSlot = HardMediumSoftLongScore.ofHard(50);
    private HardMediumSoftLongScore oneShiftPerDay = HardMediumSoftLongScore.ofHard(10);
    private HardMediumSoftLongScore noShiftsWithinTenHours = HardMediumSoftLongScore.ofHard(1);
    private HardMediumSoftLongScore contractMaximumDailyMinutes = HardMediumSoftLongScore.ofHard(1);
    private HardMediumSoftLongScore contractMaximumWeeklyMinutes = HardMediumSoftLongScore.ofHard(1);
    private HardMediumSoftLongScore contractMaximumMonthlyMinutes = HardMediumSoftLongScore.ofHard(1);
    private HardMediumSoftLongScore contractMaximumYearlyMinutes = HardMediumSoftLongScore.ofHard(1);

    private HardMediumSoftLongScore assignEveryShift = HardMediumSoftLongScore.ofMedium(1);

    private HardMediumSoftLongScore undesiredTimeSlot = HardMediumSoftLongScore.ofSoft(1);
    private HardMediumSoftLongScore desiredTimeSlot = HardMediumSoftLongScore.ofSoft(1);
    private HardMediumSoftLongScore notRotationEmployee = HardMediumSoftLongScore.ofSoft(1);

    @SuppressWarnings("unused")
    public RosterConstraintConfigurationView() {
        super(-1);
    }

    public RosterConstraintConfigurationView(Integer tenantId,
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

    public HardMediumSoftLongScore getLowRiskEmployeeInCovidWard() {
        return lowRiskEmployeeInCovidWard;
    }

    public void setLowRiskEmployeeInCovidWard(HardMediumSoftLongScore lowRiskEmployeeInCovidWard) {
        this.lowRiskEmployeeInCovidWard = lowRiskEmployeeInCovidWard;
    }

    public HardMediumSoftLongScore getModerateRiskEmployeeInCovidWard() {
        return moderateRiskEmployeeInCovidWard;
    }

    public void setModerateRiskEmployeeInCovidWard(HardMediumSoftLongScore moderateRiskEmployeeInCovidWard) {
        this.moderateRiskEmployeeInCovidWard = moderateRiskEmployeeInCovidWard;
    }

    public HardMediumSoftLongScore getHighRiskEmployeeInCovidWard() {
        return highRiskEmployeeInCovidWard;
    }

    public void setHighRiskEmployeeInCovidWard(HardMediumSoftLongScore highRiskEmployeeInCovidWard) {
        this.highRiskEmployeeInCovidWard = highRiskEmployeeInCovidWard;
    }

    public HardMediumSoftLongScore getExtremeRiskEmployeeInCovidWard() {
        return extremeRiskEmployeeInCovidWard;
    }

    public void setExtremeRiskEmployeeInCovidWard(HardMediumSoftLongScore extremeRiskEmployeeInCovidWard) {
        this.extremeRiskEmployeeInCovidWard = extremeRiskEmployeeInCovidWard;
    }

    public HardMediumSoftLongScore getInoculatedEmployeeOutsideCovidWard() {
        return inoculatedEmployeeOutsideCovidWard;
    }

    public void setInoculatedEmployeeOutsideCovidWard(HardMediumSoftLongScore inoculatedEmployeeOutsideCovidWard) {
        this.inoculatedEmployeeOutsideCovidWard = inoculatedEmployeeOutsideCovidWard;
    }

    public HardMediumSoftLongScore getMaximizeInoculatedHours() {
        return maximizeInoculatedHours;
    }

    public void setMaximizeInoculatedHours(HardMediumSoftLongScore maximizeInoculatedHours) {
        this.maximizeInoculatedHours = maximizeInoculatedHours;
    }

    public HardMediumSoftLongScore getMigrationBetweenCovidAndNonCovidWard() {
        return migrationBetweenCovidAndNonCovidWard;
    }

    public void setMigrationBetweenCovidAndNonCovidWard(HardMediumSoftLongScore migrationBetweenCovidAndNonCovidWard) {
        this.migrationBetweenCovidAndNonCovidWard = migrationBetweenCovidAndNonCovidWard;
    }

    public HardMediumSoftLongScore getNonCovidShiftLessThan8HoursAfterCovidShift() {
        return nonCovidShiftLessThan8HoursAfterCovidShift;
    }

    public void setNonCovidShiftLessThan8HoursAfterCovidShift(
            HardMediumSoftLongScore nonCovidShiftLessThan8HoursAfterCovidShift) {
        this.nonCovidShiftLessThan8HoursAfterCovidShift = nonCovidShiftLessThan8HoursAfterCovidShift;
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
