package org.optaplanner.openshift.employeerostering.shared.rotation.view;

import java.time.Duration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.common.HasTimeslot;
import org.optaplanner.openshift.employeerostering.shared.rotation.ShiftTemplate;

public class ShiftTemplateView extends AbstractPersistable implements HasTimeslot {

    private Long spotId;
    private Long rotationEmployeeId;
    private Duration durationBetweenRotationStartAndTemplateStart;
    private Duration shiftTemplateDuration;

    public ShiftTemplateView() {}

    public ShiftTemplateView(Integer rotationLength, ShiftTemplate shiftTemplate) {
        super(shiftTemplate);
        this.spotId = shiftTemplate.getSpot().getId();
        this.rotationEmployeeId = (shiftTemplate.getRotationEmployee() != null) ? shiftTemplate.getRotationEmployee().getId() : null;
        this.durationBetweenRotationStartAndTemplateStart = Duration
                .ofDays(shiftTemplate.getStartDayOffset()).plusSeconds(shiftTemplate
                        .getStartTime().toSecondOfDay());
        this.shiftTemplateDuration = Duration
                .ofDays((shiftTemplate.getEndDayOffset() < shiftTemplate.getStartDayOffset()) ? rotationLength : 0)
                .plusDays(shiftTemplate.getEndDayOffset() - shiftTemplate.getStartDayOffset())
                .plusSeconds(shiftTemplate.getEndTime().toSecondOfDay())
                .minusSeconds(shiftTemplate.getStartTime().toSecondOfDay());
    }

    public ShiftTemplateView(Integer tenantId, Long spotId, Duration durationBetweenRotationStartAndTemplateStart, Duration shiftTemplateDuration, Long rotationEmployeeId) {
        super(tenantId);
        this.spotId = spotId;
        this.durationBetweenRotationStartAndTemplateStart = durationBetweenRotationStartAndTemplateStart;
        this.shiftTemplateDuration = shiftTemplateDuration;
        this.rotationEmployeeId = rotationEmployeeId;
    }

    public Long getSpotId() {
        return spotId;
    }

    public void setSpotId(Long spotId) {
        this.spotId = spotId;
    }

    public Long getRotationEmployeeId() {
        return rotationEmployeeId;
    }

    public void setRotationEmployeeId(Long rotationEmployeeId) {
        this.rotationEmployeeId = rotationEmployeeId;
    }

    public Duration getDurationBetweenRotationStartAndTemplateStart() {
        return durationBetweenRotationStartAndTemplateStart;
    }

    public void setDurationBetweenRotationStartAndTemplateStart(Duration durationBetweenRotationStartAndTemplateStart) {
        this.durationBetweenRotationStartAndTemplateStart = durationBetweenRotationStartAndTemplateStart;
    }

    public Duration getShiftTemplateDuration() {
        return shiftTemplateDuration;
    }

    public void setShiftTemplateDuration(Duration shiftTemplateDuration) {
        this.shiftTemplateDuration = shiftTemplateDuration;
    }

    @Override
    @JsonIgnore
    public Duration getDurationBetweenReferenceAndStart() {
        return getDurationBetweenRotationStartAndTemplateStart();
    }

    @Override
    @JsonIgnore
    public Duration getDurationOfTimeslot() {
        return getShiftTemplateDuration();
    }
}
