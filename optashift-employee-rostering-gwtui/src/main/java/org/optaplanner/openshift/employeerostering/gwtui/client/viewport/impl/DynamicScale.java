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
        // TODO: Warn the user they are viewing too large a slice, or limit the slice size
        this.secondsPerGridUnit = Math.max(getMinDurationForRange(start,end).getSeconds(), durationPerGridUnit.getSeconds());
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

    public int getScreenPixelsPerGridUnit() {
        return 20;
    }

}
