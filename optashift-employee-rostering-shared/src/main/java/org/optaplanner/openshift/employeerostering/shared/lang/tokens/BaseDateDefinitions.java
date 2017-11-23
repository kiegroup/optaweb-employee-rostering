package org.optaplanner.openshift.employeerostering.shared.lang.tokens;

/**
 * Defines where to place the base date when generating shifts.
 */
public enum BaseDateDefinitions {
    /**
     * If used, the base date will be the same as the start date
     * for generating the values.
     */
    SAME_AS_START_DATE,
    /**
     * If used, the base date will be 0:00 on the first day of the week
     * which the start date used for generating values is in.
     */
    WEEK_OF_START_DATE
}
