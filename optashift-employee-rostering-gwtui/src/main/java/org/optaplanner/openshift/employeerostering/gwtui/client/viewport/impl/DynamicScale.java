package org.optaplanner.openshift.employeerostering.gwtui.client.viewport.impl;

import java.time.Duration;
import java.time.LocalDateTime;

import org.optaplanner.openshift.employeerostering.gwtui.client.viewport.grid.LinearScale;

public class DynamicScale implements LinearScale<LocalDateTime> {

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Long secondsPerGridUnit;

    public DynamicScale(LocalDateTime start, LocalDateTime end, Duration durationPerGridUnit) {
        this.startDateTime = start;
        this.endDateTime = end;
        Duration minDuration = getMinDurationForRange(start,end);
        this.secondsPerGridUnit = durationPerGridUnit.getSeconds();
        
        if (secondsPerGridUnit < minDuration.getSeconds()) {
            throw new IllegalArgumentException("durationPerGridUnit (" + 
                                               durationPerGridUnit + ") is to small for timeframe between start (" + 
                                               start + ") and end (" +
                                               end + "). The minimum duration allowed for this timeframe is (" + 
                                               minDuration + ") since the grid is limited to 1000 grid units.");
        }
    }

    private Duration getMinDurationForRange(LocalDateTime start, LocalDateTime end) {
        // Chrome has a 1000 row limit (see https://github.com/w3c/csswg-drafts/issues/1009)
        return Duration.between(start, end).dividedBy(1000);
    }

    @Override
    public double toGridUnits(LocalDateTime valueInScaleUnits) {
        return Duration.between(startDateTime, valueInScaleUnits).getSeconds() / (double) getSecondsPerGridUnit();
    }

    @Override
    public LocalDateTime toScaleUnits(double valueInGridPixels) {
        return startDateTime.plusSeconds(Math.round(valueInGridPixels * secondsPerGridUnit));
    }

    @Override
    public LocalDateTime getEndInScaleUnits() {
        return endDateTime;
    }

    public Long getSecondsPerGridUnit() {
        return secondsPerGridUnit;
    }

}
