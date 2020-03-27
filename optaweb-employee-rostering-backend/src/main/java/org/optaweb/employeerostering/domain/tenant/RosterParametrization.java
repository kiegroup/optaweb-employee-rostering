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

import org.optaweb.employeerostering.domain.common.AbstractPersistable;

@Entity
public class RosterParametrization extends AbstractPersistable {

    // TODO: Is 999 a reasonable max for the weights?
    @NotNull
    private Integer undesiredTimeSlotWeight = 100;
    @NotNull
    private Integer desiredTimeSlotWeight = 10;
    @NotNull
    private Integer rotationEmployeeMatchWeight = 500;
    @NotNull
    private Integer lowRiskEmployeeInCovidWardMatchWeight = 1; // Soft
    @NotNull
    private Integer moderateRiskEmployeeInCovidWardMatchWeight = 5; // Soft
    @NotNull
    private Integer highRiskEmployeeInCovidWardMatchWeight = 10; // Soft
    @NotNull
    private Integer extremeRiskEmployeeInCovidWardMatchWeight = 1; // Hard
    @NotNull
    private DayOfWeek weekStartDay = DayOfWeek.MONDAY;

    @SuppressWarnings("unused")
    public RosterParametrization() {
        super(-1);
    }

    public RosterParametrization(Integer tenantId,
                                 Integer undesiredTimeSlotWeight, Integer desiredTimeSlotWeight,
                                 Integer rotationEmployeeMatchWeight, DayOfWeek weekStartDay) {
        super(tenantId);
        this.undesiredTimeSlotWeight = undesiredTimeSlotWeight;
        this.desiredTimeSlotWeight = desiredTimeSlotWeight;
        this.rotationEmployeeMatchWeight = rotationEmployeeMatchWeight;
        this.weekStartDay = weekStartDay;
    }

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

    public Integer getLowRiskEmployeeInCovidWardMatchWeight() {
        return lowRiskEmployeeInCovidWardMatchWeight;
    }

    public void setLowRiskEmployeeInCovidWardMatchWeight(
            final Integer lowRiskEmployeeInCovidWardMatchWeight) {
        this.lowRiskEmployeeInCovidWardMatchWeight = lowRiskEmployeeInCovidWardMatchWeight;
    }

    public Integer getModerateRiskEmployeeInCovidWardMatchWeight() {
        return moderateRiskEmployeeInCovidWardMatchWeight;
    }

    public void setModerateRiskEmployeeInCovidWardMatchWeight(
            final Integer moderateRiskEmployeeInCovidWardMatchWeight) {
        this.moderateRiskEmployeeInCovidWardMatchWeight = moderateRiskEmployeeInCovidWardMatchWeight;
    }

    public Integer getHighRiskEmployeeInCovidWardMatchWeight() {
        return highRiskEmployeeInCovidWardMatchWeight;
    }

    public void setHighRiskEmployeeInCovidWardMatchWeight(final Integer highRiskEmployeeInCovidWardMatchWeight) {
        this.highRiskEmployeeInCovidWardMatchWeight = highRiskEmployeeInCovidWardMatchWeight;
    }

    public Integer getExtremeRiskEmployeeInCovidWardMatchWeight() {
        return extremeRiskEmployeeInCovidWardMatchWeight;
    }

    public void setExtremeRiskEmployeeInCovidWardMatchWeight(final Integer extremeRiskEmployeeInCovidWardMatchWeight) {
        this.extremeRiskEmployeeInCovidWardMatchWeight = extremeRiskEmployeeInCovidWardMatchWeight;
    }

    public DayOfWeek getWeekStartDay() {
        return weekStartDay;
    }

    public void setWeekStartDay(DayOfWeek weekStartDay) {
        this.weekStartDay = weekStartDay;
    }
}
