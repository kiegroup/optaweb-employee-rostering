package org.optaplanner.openshift.employeerostering.shared.lang.tokens;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;

/**
 * Describes the {@link EmployeeAvailabilityState} for an employee/employee group.<br>
 * Properties:<br>
 * {@link EmployeeTimeSlotInfo#employeeId} <br>
 * {@link EmployeeTimeSlotInfo#defaultAvailability} (Nullable) <br>
 * {@link EmployeeTimeSlotInfo#availabilityConditionList} (Nullable) <br>
 */
@Entity
public class EmployeeTimeSlotInfo extends AbstractPersistable {

    /**
     * What Employee/Employee Group does this EmployeeTimeSlotInfo
     * apply to.
     */
    @ManyToOne(cascade = CascadeType.ALL)
    IdOrGroup employeeId;

    /**
     * The {@link EmployeeAvailabilityState} this group have if
     * none of the conditions in {@link EmployeeTimeSlotInfo#availabilityConditionList}
     * apply.
     */
    EmployeeAvailabilityState defaultAvailability;

    /**
     * List of conditions that cause another {@link EmployeeAvailabilityState} to be used
     * instead of {@link EmployeeTimeSlotInfo#defaultAvailability}.
     */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<EmployeeConditional> availabilityConditionList;

    public EmployeeTimeSlotInfo() {

    }

    public EmployeeTimeSlotInfo(Integer tenantId, IdOrGroup id, EmployeeAvailabilityState availability) {
        super(tenantId);
        this.employeeId = id;
        this.defaultAvailability = availability;
    }

    public EmployeeTimeSlotInfo(Integer tenantId, IdOrGroup id, EmployeeAvailabilityState availability, List<
            EmployeeConditional> availabilityConditions) {
        this(tenantId, id, availability);
        this.availabilityConditionList = availabilityConditions;
    }

    /**
     * Getter for {@link EmployeeTimeSlotInfo#employeeId}
     * @return Value of {@link EmployeeTimeSlotInfo#employeeId}
     */
    public IdOrGroup getEmployeeId() {
        return employeeId;
    }

    /**
     * Setter for {@link EmployeeTimeSlotInfo#employeeId}
     * 
     * @param id Value to set {@link EmployeeTimeSlotInfo#employeeId} to
     */
    public void setEmployeeId(IdOrGroup id) {
        this.employeeId = id;
    }

    /**
     * Getter for {@link EmployeeTimeSlotInfo#defaultAvailability}
     * @return Value of {@link EmployeeTimeSlotInfo#defaultAvailability}
     */
    public EmployeeAvailabilityState getDefaultAvailability() {
        return defaultAvailability;
    }

    /**
     * Setter for {@link EmployeeTimeSlotInfo#defaultAvailability}
     * 
     * @param availability Value to set {@link EmployeeTimeSlotInfo#defaultAvailability} to
     */
    public void setDefaultAvailability(EmployeeAvailabilityState availability) {
        this.defaultAvailability = availability;
    }

    /**
     * Getter for {@link EmployeeTimeSlotInfo#availabilityConditionList}
     * @return Value of {@link EmployeeTimeSlotInfo#availabilityConditionList}
     */
    public List<EmployeeConditional> getAvailabilityConditionList() {
        return availabilityConditionList;
    }

    /**
     * Setter for {@link EmployeeTimeSlotInfo#availabilityConditionList}
     * 
     * @param availabilityConditionList Value to set {@link EmployeeTimeSlotInfo#availabilityConditionList} to
     */
    public void setAvailabilityConditionList(List<EmployeeConditional> availabilityConditionList) {
        this.availabilityConditionList = availabilityConditionList;
    }
}
