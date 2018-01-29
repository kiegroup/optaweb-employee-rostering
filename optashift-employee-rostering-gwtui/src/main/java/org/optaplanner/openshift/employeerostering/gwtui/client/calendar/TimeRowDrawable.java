package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import elemental2.dom.CanvasRenderingContext2D;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;

/**
 * A shift that can be drawn in a row of a table.
 * 
 * @param <G> Type of Group.
 * @param <I> Type of Shift.
 */
public interface TimeRowDrawable<G extends HasTitle, I extends HasTimeslot<G>> extends Drawable, HasTimeslot<G> {

    /**
     * Updates the shift of this TimeRowDrawable.
     * @param newData The new shift to use.
     */
    void updateData(I newData);

    /**
     * Draws the TimeRowDrawable at a specified position.
     * @param g Where to draw to.
     * @param x The x-coordinate of the top-left corner to draw the TimeRowDrawable at
     * @param y The y-coordinate of the top-left corner to draw the TimeRowDrawable at
     */
    void doDrawAt(CanvasRenderingContext2D g, double x, double y);

    /**
     * The row index of this TimeRowDrawable in its group.
     * @return The row index of this TimeRowDrawable in its group.
     */
    int getIndex();

    /**
     * Sets the row index of this TimeRowDrawable in its group.
     * @param index The new row index of this TimeRowDrawable.
     */
    void setIndex(int index);

}
