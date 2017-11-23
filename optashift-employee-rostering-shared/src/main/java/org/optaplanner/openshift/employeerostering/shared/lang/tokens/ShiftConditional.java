package org.optaplanner.openshift.employeerostering.shared.lang.tokens;

import org.optaplanner.openshift.employeerostering.shared.lang.parser.DateMatcher;

/**
 * Describes date related changes to the generated shift and when they should happen.<br>
 * Properties:<br>
 * {@link ShiftConditional#condition} <br>
 * {@link ShiftConditional#shift} (Nullable) <br>
 */
public class ShiftConditional {

    /**
     * A date matcher written in the language described in
     * {@link DateMatcher}
     */
    String condition;

    /**
     * Shift to be used if the condition is met. Can be null, which
     * will cause no shift to be generated
     */
    ShiftInfo shift;

    public ShiftConditional() {

    }

    public ShiftConditional(String condition) {
        this.condition = condition;
    }

    public ShiftConditional(String condition, ShiftInfo shift) {
        this(condition);
        this.shift = shift;
    }

    /**
     * Getter for {@link ShiftConditional#condition}
     * @return Value of {@link ShiftConditional#condition}
     */
    public String getCondition() {
        return condition;
    }

    /**
     * Getter for {@link ShiftConditional#shift}
     * @return Value of {@link ShiftConditional#shift}
     */
    public ShiftInfo getShift() {
        return shift;
    }

    /**
     * Setter for {@link ShiftConditional#condition}
     * 
     * @param condition Value to set {@link ShiftConditional#condition} to
     */
    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * Setter for {@link ShiftConditional#shift}
     * 
     * @param shift Value to set {@link ShiftConditional#shift} to
     */
    public void setShift(ShiftInfo shift) {
        this.shift = shift;
    }
}