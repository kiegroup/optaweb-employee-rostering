package org.optaplanner.openshift.employeerostering.shared.lang.tokens;

/**
 * Specifics if {@link EnumOrCustom#value} is either a custom value or an enum value.<br>
 * Properties:<br>
 * {@link EnumOrCustom#isCustom} <br>
 * {@link EnumOrCustom#value} <br>
 */
public class EnumOrCustom {

    /**
     * If true, {@link EnumOrCustom#value} is a custom value, otherwise
     * {@link EnumOrCustom#value} is a member of an enum
     */
    boolean isCustom;

    /**
     * Member of an enum if {@link EnumOrCustom#isCustom} is true, a custom
     * value otherwise
     */
    String value;

    public EnumOrCustom() {
    }

    public EnumOrCustom(boolean isCustom, String value) {
        this.isCustom = isCustom;
        this.value = value;
    }

    /**
     * Getter for {@link EnumOrCustom#isCustom}
     * @return Value of {@link EnumOrCustom#isCustom}
     */
    public boolean getIsCustom() {
        return isCustom;
    }

    /**
     * Getter for {@link EnumOrCustom#value}
     * @return Value of {@link EnumOrCustom#value}
     */
    public String getValue() {
        return value;
    }

    /**
     * Setter for {@link EnumOrCustom#isCustom}
     * 
     * @param isCustom Value to set {@link EnumOrCustom#isCustom} to
     */
    public void setIsCustom(boolean isCustom) {
        this.isCustom = isCustom;
    }

    /**
     * Setter for {@link EnumOrCustom#value}
     * 
     * @param value Value to set {@link EnumOrCustom#value} to
     */
    public void setValue(String value) {
        this.value = value;
    }
}
