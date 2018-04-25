package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import java.time.OffsetDateTime;

public class DateTimeRange {

    private final OffsetDateTime startDateTime;
    private final OffsetDateTime endDateTime;

    public DateTimeRange(OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }

}
