package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import elemental2.dom.CanvasRenderingContext2D;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;

public interface TimeRowDrawable<G extends HasTitle> extends Drawable, HasTimeslot<G> {

    void doDrawAt(CanvasRenderingContext2D g, double x, double y);

    int getIndex();

    void setIndex(int index);

}
