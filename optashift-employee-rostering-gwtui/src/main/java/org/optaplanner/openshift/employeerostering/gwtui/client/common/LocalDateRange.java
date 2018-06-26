package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import java.time.LocalDate;

public class LocalDateRange {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public LocalDateRange(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

}
