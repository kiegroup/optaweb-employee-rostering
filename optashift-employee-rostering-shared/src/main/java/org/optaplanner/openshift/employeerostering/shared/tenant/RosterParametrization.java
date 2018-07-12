package org.optaplanner.openshift.employeerostering.shared.tenant;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.validation.constraints.NotNull;

import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;

@Entity
@NamedQueries({
               @NamedQuery(name = "RosterParametrization.find",
                           query = "select distinct rp from RosterParametrization rp" +
                                   " where rp.tenantId = :tenantId")
})
public class RosterParametrization extends AbstractPersistable {

    // TODO: Is 999 a reasonable max for the weights?
    @NotNull
    private Integer undesiredTimeSlotWeight = 100;
    @NotNull
    private Integer desiredTimeSlotWeight = 10;
    @NotNull
    private Integer rotationEmployeeMatchWeight = 500;

    @SuppressWarnings("unused")
    public RosterParametrization() {
        super(-1);
    }

    public RosterParametrization(Integer tenantId,
                                 Integer undesiredTimeSlotWeight, Integer desiredTimeSlotWeight,
                                 Integer rotationEmployeeMatchWeight) {
        super(tenantId);
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

}
