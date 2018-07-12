package org.optaplanner.openshift.employeerostering.shared.roster;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.common.HasTimeslot;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;

@Entity
@NamedQueries({
               @NamedQuery(name = "RosterState.find",
                           query = "select distinct rs from RosterState rs" +
                                   " where rs.tenantId = :tenantId")
})
public class RosterState extends AbstractPersistable {

    @NotNull
    private Integer publishNotice; // In number of days
    @NotNull
    private LocalDate firstDraftDate;
    @NotNull
    private Integer publishLength; // In number of days
    @NotNull
    private Integer draftLength; // In number of days
    @NotNull
    private Integer unplannedRotationOffset; // In number of days from reference point
    @NotNull
    @Min(2) // Min 2 since it is impossible to do wrapping shifts templates on single day rotations
    private Integer rotationLength; // In number of days
    @NotNull
    private LocalDate lastHistoricDate;
    @NotNull
    private ZoneId timeZone;

    @OneToOne
    @NotNull
    private Tenant tenant;

    @SuppressWarnings("unused")
    public RosterState() {
        super(-1);
    }

    public RosterState(Integer tenantId, Integer publishNotice, LocalDate firstDraftDate, Integer publishLength, Integer draftLength, Integer unplannedRotationOffset, Integer rotationLength, LocalDate lastHistoricDate,
                       ZoneId timeZone) {
        super(tenantId);
        this.publishNotice = publishNotice;
        this.firstDraftDate = firstDraftDate;
        this.publishLength = publishLength;
        this.draftLength = draftLength;
        this.unplannedRotationOffset = unplannedRotationOffset;
        this.rotationLength = rotationLength;
        this.lastHistoricDate = lastHistoricDate;
        this.timeZone = timeZone;
    }

    @JsonIgnore
    public boolean isHistoric(OffsetDateTime dateTime) {
        return dateTime.isBefore(OffsetDateTime.of(getFirstPublishedDate().atTime(LocalTime.MIDNIGHT), dateTime.getOffset()));
    }

    @JsonIgnore
    public boolean isDraft(OffsetDateTime dateTime) {
        return !dateTime.isBefore(OffsetDateTime.of(getFirstDraftDate().atTime(LocalTime.MIDNIGHT), dateTime.getOffset()));
    }

    @JsonIgnore
    public boolean isPublished(OffsetDateTime dateTime) {
        return !isHistoric(dateTime) && !isDraft(dateTime);
    }

    @JsonIgnore
    public boolean isHistoric(LocalDateTime dateTime) {
        return dateTime.isBefore(getFirstPublishedDate().atTime(LocalTime.MIDNIGHT));
    }

    @JsonIgnore
    public boolean isDraft(LocalDateTime dateTime) {
        return !dateTime.isBefore(getFirstDraftDate().atTime(LocalTime.MIDNIGHT));
    }

    @JsonIgnore
    public boolean isPublished(LocalDateTime dateTime) {
        return !isHistoric(dateTime) && !isDraft(dateTime);
    }

    @JsonIgnore
    public boolean isHistoric(Shift shift) {
        return isHistoric(shift.getStartDateTime());
    }

    @JsonIgnore
    public boolean isDraft(Shift shift) {
        return isDraft(shift.getStartDateTime());
    }

    @JsonIgnore
    public boolean isPublished(Shift shift) {
        return isPublished(shift.getStartDateTime());
    }

    @JsonIgnore
    public boolean isHistoric(HasTimeslot shift) {
        return isHistoric(HasTimeslot.EPOCH.plus(shift.getDurationBetweenReferenceAndStart()));
    }

    @JsonIgnore
    public boolean isDraft(HasTimeslot shift) {
        return isDraft(HasTimeslot.EPOCH.plus(shift.getDurationBetweenReferenceAndStart()));
    }

    @JsonIgnore
    public boolean isPublished(HasTimeslot shift) {
        return isPublished(HasTimeslot.EPOCH.plus(shift.getDurationBetweenReferenceAndStart()));
    }

    @JsonIgnore
    public LocalDate getFirstPublishedDate() {
        return lastHistoricDate.plusDays(1);
    }

    @JsonIgnore
    public LocalDate getFirstUnplannedDate() {
        return firstDraftDate.plusDays(draftLength);
    }

    @JsonIgnore
    public LocalDate getPublishDeadline() {
        return firstDraftDate.minusDays(publishNotice);
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public Integer getPublishNotice() {
        return publishNotice;
    }

    public void setPublishNotice(Integer publishNotice) {
        this.publishNotice = publishNotice;
    }

    public Integer getDraftLength() {
        return draftLength;
    }

    public void setDraftLength(Integer draftLength) {
        this.draftLength = draftLength;
    }

    public Integer getPublishLength() {
        return publishLength;
    }

    public void setPublishLength(Integer publishLength) {
        this.publishLength = publishLength;
    }

    public Integer getRotationLength() {
        return rotationLength;
    }

    public void setRotationLength(Integer rotationLength) {
        this.rotationLength = rotationLength;
    }

    public LocalDate getFirstDraftDate() {
        return firstDraftDate;
    }

    public void setFirstDraftDate(LocalDate firstDraftDate) {
        this.firstDraftDate = firstDraftDate;
    }

    public Integer getUnplannedRotationOffset() {
        return unplannedRotationOffset;
    }

    public void setUnplannedRotationOffset(Integer unplannedOffset) {
        this.unplannedRotationOffset = unplannedOffset;
    }

    public void setLastHistoricDate(LocalDate lastHistoricDate) {
        this.lastHistoricDate = lastHistoricDate;
    }

    public LocalDate getLastHistoricDate() {
        return lastHistoricDate;
    }

    public ZoneId getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(ZoneId timeZone) {
        this.timeZone = timeZone;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

}
