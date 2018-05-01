package org.optaplanner.openshift.employeerostering.gwtui.client.pages;

import java.time.LocalDateTime;

import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.LinearScale;
import org.optaplanner.openshift.employeerostering.shared.common.HasTimeslot;

public abstract class TimeslotBlob implements Blob<LocalDateTime> {

    private LinearScale<LocalDateTime> scale;

    public abstract HasTimeslot getTimeslot();

    @Override
    public LocalDateTime getPositionInScaleUnits() {
        return HasTimeslot.EPOCH.plus(getTimeslot().getDurationBetweenReferenceAndStart());
    }

    @Override
    public double getSizeInGridPixels() {
        return scale.toGridPixels(getPositionInScaleUnits()
                .plus(getTimeslot().getDurationOfTimeslot())) - scale.toGridPixels(getPositionInScaleUnits());
    }

    @Override
    public LinearScale<LocalDateTime> getScale() {
        return scale;
    }

    public void setScale(LinearScale<LocalDateTime> scale) {
        this.scale = scale;
    }

}
