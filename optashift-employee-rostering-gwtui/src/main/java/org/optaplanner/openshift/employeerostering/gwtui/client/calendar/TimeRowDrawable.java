package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.LocalDateTime;

import elemental2.dom.CanvasRenderingContext2D;

public interface TimeRowDrawable<G extends HasTitle> extends Drawable {

    void doDrawAt(CanvasRenderingContext2D g, double x, double y);

    int getIndex();

    G getGroupId();

    LocalDateTime getStartTime();

    LocalDateTime getEndTime();
}
