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
        this.secondsPerGridUnit = durationPerGridUnit.getSeconds();
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
