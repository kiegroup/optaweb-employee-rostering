/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.optaweb.employeerostering.shared.shift.view;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.shared.common.AbstractPersistable;
import org.optaweb.employeerostering.shared.common.GwtJavaTimeWorkaroundUtil;
import org.optaweb.employeerostering.shared.common.HasTimeslot;
import org.optaweb.employeerostering.shared.employee.Employee;
import org.optaweb.employeerostering.shared.shift.Shift;
import org.optaweb.employeerostering.shared.spot.Spot;
import org.optaweb.employeerostering.shared.violation.ContractMinutesViolation;
import org.optaweb.employeerostering.shared.violation.DesiredTimeslotForEmployeeReward;
import org.optaweb.employeerostering.shared.violation.RequiredSkillViolation;
import org.optaweb.employeerostering.shared.violation.RotationViolationPenalty;
import org.optaweb.employeerostering.shared.violation.ShiftEmployeeConflict;
import org.optaweb.employeerostering.shared.violation.UnassignedShiftPenalty;
import org.optaweb.employeerostering.shared.violation.UnavailableEmployeeViolation;
import org.optaweb.employeerostering.shared.violation.UndesiredTimeslotForEmployeePenalty;

public class ShiftView extends AbstractPersistable implements HasTimeslot {

    private Long rotationEmployeeId;
    @NotNull
    private Long spotId;
    @NotNull
    private LocalDateTime startDateTime;
    @NotNull
    private LocalDateTime endDateTime;

    private List<RequiredSkillViolation> requiredSkillViolationList;
    private List<UnavailableEmployeeViolation> unavailableEmployeeViolationList;
    private List<ShiftEmployeeConflict> shiftEmployeeConflictList;
    private List<DesiredTimeslotForEmployeeReward> desiredTimeslotForEmployeeRewardList;
    private List<UndesiredTimeslotForEmployeePenalty> undesiredTimeslotForEmployeePenaltyList;
    private List<RotationViolationPenalty> rotationViolationPenaltyList;
    private List<UnassignedShiftPenalty> unassignedShiftPenaltyList;
    private List<ContractMinutesViolation> contractMinutesViolationPenaltyList;
    private HardMediumSoftLongScore indictmentScore;

    private boolean pinnedByUser = false;

    private Long employeeId = null;

    @SuppressWarnings("unused")
    public ShiftView() {
    }

    public ShiftView(Integer tenantId, Spot spot, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this(tenantId, spot, startDateTime, endDateTime, null);
    }

    public ShiftView(Integer tenantId, Spot spot, LocalDateTime startDateTime, LocalDateTime endDateTime, Employee rotationEmployee) {
        super(tenantId);
        this.spotId = spot.getId();
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.rotationEmployeeId = (rotationEmployee == null) ? null : rotationEmployee.getId();

        this.requiredSkillViolationList = null;
        this.shiftEmployeeConflictList = null;
        this.unavailableEmployeeViolationList = null;
        this.desiredTimeslotForEmployeeRewardList = null;
        this.undesiredTimeslotForEmployeePenaltyList = null;
        this.rotationViolationPenaltyList = null;
        this.contractMinutesViolationPenaltyList = null;
        this.indictmentScore = null;
    }

    public ShiftView(ZoneId zoneId, Shift shift) {
        this(zoneId, shift, null, null, null, null, null, null, null, null, null);
    }

    public ShiftView(ZoneId zoneId, Shift shift, List<RequiredSkillViolation> requiredSkillViolationList,
                     List<UnavailableEmployeeViolation> unavailableEmployeeViolationList, List<ShiftEmployeeConflict> shiftEmployeeConflictList,
                     List<DesiredTimeslotForEmployeeReward> desiredTimeslotForEmployeeRewardList,
                     List<UndesiredTimeslotForEmployeePenalty> undesiredTimeslotForEmployeePenaltyList,
                     List<RotationViolationPenalty> rotationViolationPenaltyList,
                     List<UnassignedShiftPenalty> unassignedShiftPenaltyList,
                     List<ContractMinutesViolation> contractMinutesViolationPenaltyList,
                     HardMediumSoftLongScore indictmentScore) {
        super(shift);
        this.spotId = shift.getSpot().getId();
        this.startDateTime = GwtJavaTimeWorkaroundUtil.toLocalDateTimeInZone(shift.getStartDateTime(), zoneId);
        this.endDateTime = GwtJavaTimeWorkaroundUtil.toLocalDateTimeInZone(shift.getEndDateTime(), zoneId);
        this.pinnedByUser = shift.isPinnedByUser();
        this.rotationEmployeeId = (shift.getRotationEmployee() == null) ? null : shift.getRotationEmployee().getId();
        this.employeeId = (shift.getEmployee() == null) ? null : shift.getEmployee().getId();

        this.requiredSkillViolationList = requiredSkillViolationList;
        this.shiftEmployeeConflictList = shiftEmployeeConflictList;
        this.unavailableEmployeeViolationList = unavailableEmployeeViolationList;
        this.desiredTimeslotForEmployeeRewardList = desiredTimeslotForEmployeeRewardList;
        this.undesiredTimeslotForEmployeePenaltyList = undesiredTimeslotForEmployeePenaltyList;
        this.rotationViolationPenaltyList = rotationViolationPenaltyList;
        this.unassignedShiftPenaltyList = unassignedShiftPenaltyList;
        this.contractMinutesViolationPenaltyList = contractMinutesViolationPenaltyList;
        this.indictmentScore = indictmentScore;
    }

    @Override
    public String toString() {
        return spotId + " " + startDateTime + "-" + endDateTime;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Long getSpotId() {
        return spotId;
    }

    public void setSpotId(Long spotId) {
        this.spotId = spotId;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public boolean isPinnedByUser() {
        return pinnedByUser;
    }

    public void setPinnedByUser(boolean pinnedByUser) {
        this.pinnedByUser = pinnedByUser;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getRotationEmployeeId() {
        return rotationEmployeeId;
    }

    public void setRotationEmployeeId(Long rotationEmployeeId) {
        this.rotationEmployeeId = rotationEmployeeId;
    }

    @Override
    @JsonIgnore
    public Duration getDurationBetweenReferenceAndStart() {
        return Duration.between(HasTimeslot.EPOCH, getStartDateTime());
    }

    @Override
    @JsonIgnore
    public Duration getDurationOfTimeslot() {
        return Duration.between(getStartDateTime(), getEndDateTime());
    }

    public List<RequiredSkillViolation> getRequiredSkillViolationList() {
        return requiredSkillViolationList;
    }

    public void setRequiredSkillViolationList(List<RequiredSkillViolation> requiredSkillViolationList) {
        this.requiredSkillViolationList = requiredSkillViolationList;
    }

    public List<UnavailableEmployeeViolation> getUnavailableEmployeeViolationList() {
        return unavailableEmployeeViolationList;
    }

    public void setUnavailableEmployeeViolationList(List<UnavailableEmployeeViolation> unavailableEmployeeViolationList) {
        this.unavailableEmployeeViolationList = unavailableEmployeeViolationList;
    }

    public List<ShiftEmployeeConflict> getShiftEmployeeConflictList() {
        return shiftEmployeeConflictList;
    }

    public void setShiftEmployeeConflictList(List<ShiftEmployeeConflict> shiftEmployeeConflictList) {
        this.shiftEmployeeConflictList = shiftEmployeeConflictList;
    }

    public HardMediumSoftLongScore getIndictmentScore() {
        return indictmentScore;
    }

    public void setIndictmentScore(HardMediumSoftLongScore indictmentScore) {
        this.indictmentScore = indictmentScore;
    }

    public List<DesiredTimeslotForEmployeeReward> getDesiredTimeslotForEmployeeRewardList() {
        return desiredTimeslotForEmployeeRewardList;
    }

    public void setDesiredTimeslotForEmployeeRewardList(List<DesiredTimeslotForEmployeeReward> desiredTimeslotForEmployeeRewardList) {
        this.desiredTimeslotForEmployeeRewardList = desiredTimeslotForEmployeeRewardList;
    }

    public List<UndesiredTimeslotForEmployeePenalty> getUndesiredTimeslotForEmployeePenaltyList() {
        return undesiredTimeslotForEmployeePenaltyList;
    }

    public void setUndesiredTimeslotForEmployeePenaltyList(List<UndesiredTimeslotForEmployeePenalty> undesiredTimeslotForEmployeePenaltyList) {
        this.undesiredTimeslotForEmployeePenaltyList = undesiredTimeslotForEmployeePenaltyList;
    }

    public List<RotationViolationPenalty> getRotationViolationPenaltyList() {
        return rotationViolationPenaltyList;
    }

    public void setRotationViolationPenaltyList(List<RotationViolationPenalty> rotationViolationPenaltyList) {
        this.rotationViolationPenaltyList = rotationViolationPenaltyList;
    }

    public List<UnassignedShiftPenalty> getUnassignedShiftPenaltyList() {
        return unassignedShiftPenaltyList;
    }

    public void setUnassignedShiftPenaltyList(List<UnassignedShiftPenalty> unassignedShiftPenaltyList) {
        this.unassignedShiftPenaltyList = unassignedShiftPenaltyList;
    }

    public List<ContractMinutesViolation> getContractMinutesViolationPenaltyList() {
        return contractMinutesViolationPenaltyList;
    }

    public void setContractMinutesViolationPenaltyList(List<ContractMinutesViolation> contractMinutesViolationPenaltyList) {
        this.contractMinutesViolationPenaltyList = contractMinutesViolationPenaltyList;
    }
}
