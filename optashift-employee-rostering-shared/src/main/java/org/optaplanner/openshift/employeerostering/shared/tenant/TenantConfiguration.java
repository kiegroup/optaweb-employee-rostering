package org.optaplanner.openshift.employeerostering.shared.tenant;

import java.time.DayOfWeek;
import java.time.ZoneId;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;

@Entity
@NamedQueries({
               @NamedQuery(name = "TenantConfiguration.find",
                           query = "select distinct c from TenantConfiguration c" +
                                   " where c.tenantId = :tenantId")
})
public class TenantConfiguration extends AbstractPersistable {

    // TODO: Is 999 a reasonable max for the weights?
    @NotNull
    private Integer undesiredTimeSlotWeight = 100;
    @NotNull
    private Integer desiredTimeSlotWeight = 10;
    @NotNull
    private Integer rotationEmployeeMatchWeight = 500;
    private ZoneId timeZone = ZoneId.of("UTC");

    @SuppressWarnings("unused")
    public TenantConfiguration() {
        super(-1);
    }

    public TenantConfiguration(Integer tenantId, Integer templateDuration, DayOfWeek weekStart,
                               Integer undesiredTimeSlotWeight, Integer desiredTimeSlotWeight,
                               Integer rotationEmployeeMatchWeight, ZoneId timeZone) {
        super(tenantId);
        this.undesiredTimeSlotWeight = undesiredTimeSlotWeight;
        this.desiredTimeSlotWeight = desiredTimeSlotWeight;
        this.rotationEmployeeMatchWeight = rotationEmployeeMatchWeight;
        this.timeZone = timeZone;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public ZoneId getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(ZoneId timeZone) {
        this.timeZone = timeZone;
    }

}
