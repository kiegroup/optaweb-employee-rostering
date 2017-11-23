package org.optaplanner.openshift.employeerostering.shared.lang.tokens;

import java.util.List;

/**
 * Describes the shifts to generate and how to generate them.<br>
 * Properties:<br>
 * {@link ShiftTemplate#baseDateType} <br>
 * {@link ShiftTemplate#repeatType} <br>
 * {@link ShiftTemplate#universalExceptions} <br>
 * {@link ShiftTemplate#shifts} <br>
 */
public class ShiftTemplate {

    /**
     * Defines the base date used by {@link ShiftInfo#startTime} and {@link ShiftInfo#endTime}.
     * Valid values for non-custom definitions are the members of {@link BaseDateDefinitions}.
     */
    EnumOrCustom baseDateType;

    /**
     * Define how to repeat the shifts defined in this template.
     * Valid values for non-custom definitions are members of {@link RepeatMode}
     */
    EnumOrCustom repeatType;

    /**
     * List of exceptions that apply to all shifts. It is strongly recommend to leave
     * {@link ShiftConditional#shift} null for each member of the list, as that shift will be created for all
     * shifts that match the exception. {@link ShiftTemplate#universalExceptions} are evaluated after
     * {@link ShiftInfo#exceptions}. 
     */
    List<ShiftConditional> universalExceptions;

    /**
     * List of shifts to generate
     */
    List<ShiftInfo> shifts;

    public ShiftTemplate() {

    }

    public ShiftTemplate(EnumOrCustom baseDateType, EnumOrCustom repeatType, List<ShiftConditional> universalExceptions,
            List<ShiftInfo> shifts) {
        this.baseDateType = baseDateType;
        this.repeatType = repeatType;
        this.universalExceptions = universalExceptions;
        this.shifts = shifts;
    }

    /**
     * Getter for {@link ShiftTemplate#baseDateType}
     * @return Value of {@link ShiftTemplate#baseDateType}
     */
    public EnumOrCustom getBaseDateType() {
        return baseDateType;
    }

    /**
     * Setter for {@link ShiftTemplate#baseDateType}
     * 
     * @param baseDateType Value to set {@link ShiftTemplate#baseDateType} to
     */
    public void setBaseDateType(EnumOrCustom baseDateType) {
        this.baseDateType = baseDateType;
    }

    /**
     * Getter for {@link ShiftTemplate#repeatType}
     * @return Value of {@link ShiftTemplate#repeatType}
     */
    public EnumOrCustom getRepeatType() {
        return repeatType;
    }

    /**
     * Setter for {@link ShiftTemplate#repeatType}
     * 
     * @param repeatType Value to set {@link ShiftTemplate#repeatType} to
     */
    public void setRepeatType(EnumOrCustom repeatType) {
        this.repeatType = repeatType;
    }

    /**
     * Getter for {@link ShiftTemplate#universalExceptions}
     * @return Value of {@link ShiftTemplate#universalExceptions}
     */
    public List<ShiftConditional> getUniversalExceptions() {
        return universalExceptions;
    }

    /**
     * Setter for {@link ShiftTemplate#universalExceptions}
     * 
     * @param universalExceptions Value to set {@link ShiftTemplate#universalExceptions} to
     */
    public void setUniversalExceptions(List<ShiftConditional> universalExceptions) {
        this.universalExceptions = universalExceptions;
    }

    /**
     * Getter for {@link ShiftTemplate#shifts}
     * @return Value of {@link ShiftTemplate#shifts}
     */
    public List<ShiftInfo> getShifts() {
        return shifts;
    }

    /**
     * Setter for {@link ShiftTemplate#shifts}
     * 
     * @param shifts Value to set {@link ShiftTemplate#shifts} to
     */
    public void setShifts(List<ShiftInfo> shifts) {
        this.shifts = shifts;
    }

}
