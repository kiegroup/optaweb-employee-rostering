package org.optaplanner.openshift.employeerostering.shared.tenant;

import java.time.DayOfWeek;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;

@Entity
public class TenantConfiguration extends AbstractPersistable {

    // TODO: Is 999 a reasonable max for the weights?
    @NotNull
    private Integer undesiredTimeSlotWeight = 100;
    @NotNull
    private Integer desiredTimeSlotWeight = 10;
    @NotNull
    private Integer rotationEmployeeMatchWeight = 500;
    @NotNull
    private Integer templateDuration = 1;
    @NotNull
    private DayOfWeek weekStart = DayOfWeek.MONDAY;

    @SuppressWarnings("unused")
    public TenantConfiguration() {
        super(-1);
    }

    public TenantConfiguration(Integer tenantId, Integer templateDuration, DayOfWeek weekStart,
                               Integer undesiredTimeSlotWeight, Integer desiredTimeSlotWeight,
                               Integer rotationEmployeeMatchWeight) {
        super(tenantId);
        this.templateDuration = templateDuration;
        this.weekStart = weekStart;
        this.undesiredTimeSlotWeight = undesiredTimeSlotWeight;
        this.desiredTimeSlotWeight = desiredTimeSlotWeight;
        this.rotationEmployeeMatchWeight = rotationEmployeeMatchWeight;
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

    public Integer getTemplateDuration() {
        return templateDuration;
    }

    public void setTemplateDuration(Integer templateDuration) {
        this.templateDuration = templateDuration;
    }

    public DayOfWeek getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(DayOfWeek weekStart) {
        this.weekStart = weekStart;
    }

}
