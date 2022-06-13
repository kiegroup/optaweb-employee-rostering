package org.optaweb.employeerostering.domain.roster.view;

import java.time.LocalDate;
import java.time.ZoneId;

import org.optaweb.employeerostering.domain.common.AbstractPersistable;
import org.optaweb.employeerostering.domain.tenant.Tenant;

public class RosterStateView extends AbstractPersistable {

    private Integer publishNotice; // In number of days

    private LocalDate firstDraftDate;

    private final Integer publishLength; // In number of days

    private Integer draftLength; // In number of days

    private Integer unplannedRotationOffset; // In number of days from reference point

    private Integer rotationLength; // In number of days

    private LocalDate lastHistoricDate;

    private ZoneId timeZone;

    private Tenant tenant;

    @SuppressWarnings("unused")
    public RosterStateView() {
        super(-1);
        publishLength = 7;
    }

    public RosterStateView(Integer tenantId, Integer publishNotice, LocalDate firstDraftDate, Integer publishLength,
            Integer draftLength, Integer unplannedRotationOffset, Integer rotationLength,
            LocalDate lastHistoricDate, ZoneId timeZone) {
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
