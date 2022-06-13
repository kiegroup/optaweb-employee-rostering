package org.optaweb.employeerostering.domain.roster;

import java.time.LocalDate;

public class PublishResult {

    private LocalDate publishedFromDate; // Inclusive
    private LocalDate publishedToDate; // Exclusive

    @SuppressWarnings("unused")
    public PublishResult() {
    }

    public PublishResult(LocalDate publishedFromDate, LocalDate publishedToDate) {
        this.publishedFromDate = publishedFromDate;
        this.publishedToDate = publishedToDate;
    }

    public LocalDate getPublishedFromDate() {
        return publishedFromDate;
    }

    public void setPublishedFromDate(LocalDate publishedFromDate) {
        this.publishedFromDate = publishedFromDate;
    }

    public LocalDate getPublishedToDate() {
        return publishedToDate;
    }

    public void setPublishedToDate(LocalDate publishedToDate) {
        this.publishedToDate = publishedToDate;
    }
}
