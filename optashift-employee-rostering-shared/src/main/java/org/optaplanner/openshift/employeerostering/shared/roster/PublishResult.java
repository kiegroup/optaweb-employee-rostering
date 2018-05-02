package org.optaplanner.openshift.employeerostering.shared.roster;

import java.time.LocalDate;

public class PublishResult {
    private LocalDate publishedFromInclusive;
    private LocalDate publishedToExclusive;
    
    @SuppressWarnings("unused")
    public PublishResult() {
        
    }
    
    public PublishResult(LocalDate publishedFromInclusive, LocalDate publishedToExclusive) {
        this.publishedFromInclusive = publishedFromInclusive;
        this.publishedToExclusive = publishedToExclusive;
    }

    public LocalDate getPublishedFromInclusive() {
        return publishedFromInclusive;
    }

    
    public void setPublishedFromInclusive(LocalDate publishedFromInclusive) {
        this.publishedFromInclusive = publishedFromInclusive;
    }

    
    public LocalDate getPublishedToExclusive() {
        return publishedToExclusive;
    }

    
    public void setPublishedToExclusive(LocalDate publishedToExclusive) {
        this.publishedToExclusive = publishedToExclusive;
    }
}
