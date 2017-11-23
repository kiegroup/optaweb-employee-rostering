package org.optaplanner.openshift.employeerostering.shared.lang.tokens;

import java.util.List;

import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;

/**
 * Describes the {@link EmployeeAvailabilityState} for an employee/employee group.<br>
 * Properties:<br>
 * {@link EmployeeTimeSlotInfo#id} <br>
 * {@link EmployeeTimeSlotInfo#defaultAvailability} (Nullable) <br>
 * {@link EmployeeTimeSlotInfo#availabilityConditions} (Nullable) <br>
 */
public class EmployeeTimeSlotInfo {

    /**
     * What Employee/Employee Group does this EmployeeTimeSlotInfo
     * apply to.
     */
    IdOrGroup id;

    /**
     * The {@link EmployeeAvailabilityState} this group have if
     * none of the conditions in {@link EmployeeTimeSlotInfo#availabilityConditions}
     * apply.
     */
    EmployeeAvailabilityState defaultAvailability;

    /**
     * List of conditions that cause another {@link EmployeeAvailabilityState} to be used
     * instead of {@link EmployeeTimeSlotInfo#defaultAvailability}.
     */
    List<EmployeeConditional> availabilityConditions;

    public EmployeeTimeSlotInfo() {

    }

    public EmployeeTimeSlotInfo(IdOrGroup id, EmployeeAvailabilityState availability) {
        this.id = id;
        this.defaultAvailability = availability;
    }

    public EmployeeTimeSlotInfo(IdOrGroup id, EmployeeAvailabilityState availability, List<
            EmployeeConditional> availabilityConditions) {
        this(id, availability);
        this.availabilityConditions = availabilityConditions;
    }

    /**
     * Getter for {@link EmployeeTimeSlotInfo#id}
     * @return Value of {@link EmployeeTimeSlotInfo#id}
     */
    public IdOrGroup getId() {
        return id;
    }

    /**
     * Setter for {@link EmployeeTimeSlotInfo#id}
     * 
     * @param id Value to set {@link EmployeeTimeSlotInfo#id} to
     */
    public void setId(IdOrGroup id) {
        this.id = id;
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
     * Getter for {@link EmployeeTimeSlotInfo#availabilityConditions}
     * @return Value of {@link EmployeeTimeSlotInfo#availabilityConditions}
     */
    public List<EmployeeConditional> getAvailabilityConditions() {
        return availabilityConditions;
    }

    /**
     * Setter for {@link EmployeeTimeSlotInfo#availabilityConditions}
     * 
     * @param availabilityConditions Value to set {@link EmployeeTimeSlotInfo#availabilityConditions} to
     */
    public void setAvailabilityConditions(List<EmployeeConditional> availabilityConditions) {
        this.availabilityConditions = availabilityConditions;
    }
}
