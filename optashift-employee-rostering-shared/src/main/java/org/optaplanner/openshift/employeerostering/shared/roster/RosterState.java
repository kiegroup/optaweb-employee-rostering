package org.optaplanner.openshift.employeerostering.shared.roster;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;

@Entity
@NamedQueries({
               @NamedQuery(name = "RosterState.find",
                           query = "select distinct rs from RosterState rs" +
                                   " where rs.tenantId = :tenantId")
})
public class RosterState extends AbstractPersistable {

    @NotNull
    private Integer publishNotice;
    @NotNull
    private Integer draftLength;
    @NotNull
    private Integer publishLength;
    @NotNull
    private Integer rotationLength;
    @NotNull
    private LocalDate firstDraftDate;
    @NotNull
    private Integer unplannedOffset;
    @NotNull
    private LocalDate lastHistoricDate;

    @SuppressWarnings("unused")
    public RosterState() {
        super(-1);
    }

    public RosterState(Integer tenantId, Integer publishNotice, Integer draftLength, Integer publishLength, Integer rotationLength, LocalDate firstDraftDate, Integer unplannedOffset) {
        super(tenantId);
        this.publishNotice = publishNotice;
        this.draftLength = draftLength;
        this.publishLength = publishLength;
        this.rotationLength = rotationLength;
        this.firstDraftDate = firstDraftDate;
        this.unplannedOffset = unplannedOffset;
    }

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

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public LocalDate getFirstDraftDate() {
        return firstDraftDate;
    }

    public void setFirstDraftDate(LocalDate firstDraftDate) {
        this.firstDraftDate = firstDraftDate;
    }

    public Integer getUnplannedOffset() {
        return unplannedOffset;
    }

    public void setUnplannedOffset(Integer unplannedOffset) {
        this.unplannedOffset = unplannedOffset;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public void setLastHistoricDate(LocalDate lastHistoricDate) {
        this.lastHistoricDate = lastHistoricDate;
    }

    public LocalDate getLastHistoricDate() {
        return lastHistoricDate;
    }

    @JsonIgnore
    public boolean isHistoric(Shift shift) {
        return shift.getEndDateTime().isBefore(OffsetDateTime.of(getFirstPublishedDate().atTime(LocalTime.MIDNIGHT), shift.getEndDateTime().getOffset()));
    }

    // Do we need this, since if a shift exists, it would be draft...
    /*@JsonIgnore
    public boolean isUnplanned(Shift shift) {
        return shift.getStartDateTime().isAfter(OffsetDateTime.of(getLastDraftDate().atTime(LocalTime.MIDNIGHT), shift.getStartDateTime().getOffset()));
    }*/

    @JsonIgnore
    public boolean isDraft(Shift shift) {
        return shift.getStartDateTime().isAfter(OffsetDateTime.of(getFirstDraftDate().atTime(LocalTime.MIDNIGHT), shift.getStartDateTime().getOffset()));
    }

    @JsonIgnore
    public boolean isPublished(Shift shift) {
        return !isHistoric(shift) && !isDraft(shift);
    }

    @JsonIgnore
    public LocalDate getFirstPublishedDate() {
        return lastHistoricDate.plusDays(1);
    }

    @JsonIgnore
    public LocalDate getLastPublishedDate() {
        return firstDraftDate.minusDays(1);
    }

    @JsonIgnore
    public LocalDate getLastDraftDate() {
        return firstDraftDate.plusDays(draftLength);
    }

    @JsonIgnore
    public LocalDate getPublishDeadline() {
        return firstDraftDate.minusDays(publishNotice);
    }
}
