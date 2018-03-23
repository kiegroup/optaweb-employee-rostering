package org.optaplanner.openshift.employeerostering.shared.common;

import java.time.OffsetDateTime;

public class OutOfDateException extends RuntimeException {

    // Exceptions cannot be generic, so we need to cast from Object
    private final Object outOfDateObj;

    private final OffsetDateTime objUpdateDateTime;
    private final OffsetDateTime lastUpdateDateTime;

    public OutOfDateException(Object outOfDateObj, OffsetDateTime objUpdateDateTime, OffsetDateTime lastUpdateDateTime) {
        // TODO: word better
        super("Calculated object [" + outOfDateObj + "] was last updated on [" + objUpdateDateTime + "], but there" +
                " been update to its parameters on [" + lastUpdateDateTime + "]");
        this.outOfDateObj = outOfDateObj;
        this.objUpdateDateTime = objUpdateDateTime;
        this.lastUpdateDateTime = lastUpdateDateTime;
    }

    @SuppressWarnings("unchecked")
    public <T> T getOutOfDateObject() {
        return (T) outOfDateObj;
    }

    public OffsetDateTime getObjUpdateDateTime() {
        return objUpdateDateTime;
    }

    public OffsetDateTime getLastUpdateDateTime() {
        return lastUpdateDateTime;
    }
}
