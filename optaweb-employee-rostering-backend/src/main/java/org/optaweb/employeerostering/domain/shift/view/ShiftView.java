package org.optaweb.employeerostering.domain.shift.view;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.common.DateTimeUtils;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.violation.ContractMinutesViolation;
import org.optaweb.employeerostering.domain.violation.DesiredTimeslotForEmployeeReward;
import org.optaweb.employeerostering.domain.violation.NoBreakViolation;
import org.optaweb.employeerostering.domain.violation.PublishedShiftReassignedPenalty;
import org.optaweb.employeerostering.domain.violation.RequiredSkillViolation;
import org.optaweb.employeerostering.domain.violation.RotationViolationPenalty;
import org.optaweb.employeerostering.domain.violation.ShiftEmployeeConflict;
import org.optaweb.employeerostering.domain.violation.UnassignedShiftPenalty;
import org.optaweb.employeerostering.domain.violation.UnavailableEmployeeViolation;
import org.optaweb.employeerostering.domain.violation.UndesiredTimeslotForEmployeePenalty;

public class ShiftView extends AbstractPersistable {

    private Long rotationEmployeeId;
    @NotNull
    private Long spotId;
    @NotNull
    private List<Long> requiredSkillSetIdList;

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
    private List<NoBreakViolation> noBreakViolationList;

    private List<PublishedShiftReassignedPenalty> publishedShiftReassignedPenaltyList;

    private HardMediumSoftLongScore indictmentScore;

    private boolean pinnedByUser = false;

    private Long employeeId = null;
    private Long originalEmployeeId = null;

    @SuppressWarnings("unused")
    public ShiftView() {
    }

    public ShiftView(Integer tenantId, Spot spot, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this(tenantId, spot, startDateTime, endDateTime, null);
    }

    public ShiftView(Integer tenantId, Spot spot, LocalDateTime startDateTime, LocalDateTime endDateTime,
            Employee rotationEmployee) {
        this(tenantId, spot, startDateTime, endDateTime, rotationEmployee, new ArrayList<>(), null);
    }

    public ShiftView(Integer tenantId, Spot spot, LocalDateTime startDateTime, LocalDateTime endDateTime,
            Employee rotationEmployee, List<Long> requiredSkillSetIdList, Employee originalEmployee) {
        super(tenantId);
        this.spotId = spot.getId();
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.rotationEmployeeId = (rotationEmployee == null) ? null : rotationEmployee.getId();
        this.originalEmployeeId = (originalEmployee == null) ? null : originalEmployee.getId();

        this.requiredSkillViolationList = null;
        this.shiftEmployeeConflictList = null;
        this.unavailableEmployeeViolationList = null;
        this.desiredTimeslotForEmployeeRewardList = null;
        this.undesiredTimeslotForEmployeePenaltyList = null;
        this.rotationViolationPenaltyList = null;
        this.contractMinutesViolationPenaltyList = null;
        this.noBreakViolationList = null;
        this.indictmentScore = null;

        this.requiredSkillSetIdList = requiredSkillSetIdList;
    }

    public ShiftView(ZoneId zoneId, Shift shift) {
        this(zoneId, shift, null, null, null, null, null, null, null, null, null, null, null);
    }

    public ShiftView(ZoneId zoneId, Shift shift, List<RequiredSkillViolation> requiredSkillViolationList,
            List<UnavailableEmployeeViolation> unavailableEmployeeViolationList,
            List<ShiftEmployeeConflict> shiftEmployeeConflictList,
            List<DesiredTimeslotForEmployeeReward> desiredTimeslotForEmployeeRewardList,
            List<UndesiredTimeslotForEmployeePenalty> undesiredTimeslotForEmployeePenaltyList,
            List<RotationViolationPenalty> rotationViolationPenaltyList,
            List<UnassignedShiftPenalty> unassignedShiftPenaltyList,
            List<ContractMinutesViolation> contractMinutesViolationPenaltyList,
            List<NoBreakViolation> noBreakViolationList,
            List<PublishedShiftReassignedPenalty> publishedShiftReassignedPenaltyList,
            HardMediumSoftLongScore indictmentScore) {
        super(shift);
        this.spotId = shift.getSpot().getId();
        this.requiredSkillSetIdList = shift.getRequiredSkillSet().stream()
                .map(Skill::getId).sorted().collect(Collectors.toCollection(ArrayList::new));
        this.startDateTime = DateTimeUtils.toLocalDateTimeInZone(shift.getStartDateTime(), zoneId);
        this.endDateTime = DateTimeUtils.toLocalDateTimeInZone(shift.getEndDateTime(), zoneId);
        this.pinnedByUser = shift.isPinnedByUser();
        this.rotationEmployeeId = (shift.getRotationEmployee() == null) ? null : shift.getRotationEmployee().getId();
        this.employeeId = (shift.getEmployee() == null) ? null : shift.getEmployee().getId();
        this.originalEmployeeId = (shift.getOriginalEmployee() == null) ? null : shift.getOriginalEmployee().getId();

        this.requiredSkillViolationList = requiredSkillViolationList;
        this.shiftEmployeeConflictList = shiftEmployeeConflictList;
        this.unavailableEmployeeViolationList = unavailableEmployeeViolationList;
        this.desiredTimeslotForEmployeeRewardList = desiredTimeslotForEmployeeRewardList;
        this.undesiredTimeslotForEmployeePenaltyList = undesiredTimeslotForEmployeePenaltyList;
        this.rotationViolationPenaltyList = rotationViolationPenaltyList;
        this.unassignedShiftPenaltyList = unassignedShiftPenaltyList;
        this.contractMinutesViolationPenaltyList = contractMinutesViolationPenaltyList;
        this.publishedShiftReassignedPenaltyList = publishedShiftReassignedPenaltyList;
        this.noBreakViolationList = noBreakViolationList;

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

    public List<Long> getRequiredSkillSetIdList() {
        return requiredSkillSetIdList;
    }

    public void setRequiredSkillSetIdList(List<Long> requiredSkillSetIdList) {
        this.requiredSkillSetIdList = requiredSkillSetIdList;
    }

    public Long getOriginalEmployeeId() {
        return originalEmployeeId;
    }

    public void setOriginalEmployeeId(Long originalEmployeeId) {
        this.originalEmployeeId = originalEmployeeId;
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

    public void setDesiredTimeslotForEmployeeRewardList(
            List<DesiredTimeslotForEmployeeReward> desiredTimeslotForEmployeeRewardList) {
        this.desiredTimeslotForEmployeeRewardList = desiredTimeslotForEmployeeRewardList;
    }

    public List<UndesiredTimeslotForEmployeePenalty> getUndesiredTimeslotForEmployeePenaltyList() {
        return undesiredTimeslotForEmployeePenaltyList;
    }

    public void setUndesiredTimeslotForEmployeePenaltyList(
            List<UndesiredTimeslotForEmployeePenalty> undesiredTimeslotForEmployeePenaltyList) {
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

    public List<NoBreakViolation> getNoBreakViolationList() {
        return noBreakViolationList;
    }

    public void setNoBreakViolationList(List<NoBreakViolation> noBreakViolationList) {
        this.noBreakViolationList = noBreakViolationList;
    }

    public List<PublishedShiftReassignedPenalty> getPublishedShiftReassignedPenaltyList() {
        return publishedShiftReassignedPenaltyList;
    }

    public void setPublishedShiftReassignedPenaltyList(
            List<PublishedShiftReassignedPenalty> publishedShiftReassignedPenaltyList) {
        this.publishedShiftReassignedPenaltyList = publishedShiftReassignedPenaltyList;
    }
}
