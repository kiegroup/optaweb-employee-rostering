package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.MouseEvent;

/**
 * Something that can be drawn to a canvas.
 */
public interface Drawable {

    /**
     * Draws the thing.
     * @param g Where to draw the thing to.
     */
    public void draw(CanvasRenderingContext2D g);

    /**
     * Returns the parent of this Drawable (can be null).
     * @return The parent of this Drawable.
     */
    public Drawable getParent();

    /**
     * Sets the parent of this Drawable (can be null).
     * @param parent The new parent of this Drawable.
     */
    public void setParent(Drawable parent);

    /**
     * The x-position of this Drawable realitive to its parent.
     * @return The x-position of this Drawable realitive to its parent.
     */
    public double getLocalX();

    /**
     * The y-position of this Drawable realitive to its parent.
     * @return The y-position of this Drawable realitive to its parent.
     */
    public double getLocalY();

    /**
     * The global x-position of this Drawable.
     * @return The global x-position of this Drawable.
     */
    public double getGlobalX();

    /**
     * The global y-position of this Drawable.
     * @return The global y-position of this Drawable.
     */
    public double getGlobalY();

    //These events return true if the event is consumed. Additionally,
    //they are only called if the mouse is over the Drawable.

    /**
     * Called when the mouse enters the Drawable.
     * @param e Source event.
     * @param x Global mouse x.
     * @param y Global mouse y.
     * @return true iff the event was consumed.
     */
    public boolean onMouseEnter(MouseEvent e, double x, double y);

    /**
     * Called when the mouse exits the Drawable.
     * @param e Source event.
     * @param x Global mouse x.
     * @param y Global mouse y.
     * @return true iff the event was consumed.
     */
    public boolean onMouseExit(MouseEvent e, double x, double y);

    /**
     * Called when the mouse drags the Drawable.
     * @param e Source event.
     * @param x Global mouse x.
     * @param y Global mouse y.
     * @return true iff the event was consumed.
     */
    public boolean onMouseDrag(MouseEvent e, double x, double y);

    /**
     * Called when the mouse moves the Drawable.
     * @param e Source event.
     * @param x Global mouse x.
     * @param y Global mouse y.
     * @return true iff the event was consumed.
     */
    public boolean onMouseMove(MouseEvent e, double x, double y);

    /**
     * Called when the mouse clicks the Drawable.
     * @param e Source event.
     * @param x Global mouse x.
     * @param y Global mouse y.
     * @return REMOVE_FOCUS to remove focus from the canvas,
     * CAPTURE_DRAG to capture drag events, IGNORE if the event
     * wasn't consumed.
     */
    public PostMouseDownEvent onMouseDown(MouseEvent e, double x, double y);

    /**
     * Called when the mouse is released over the Drawable.
     * @param e Source event.
     * @param x Global mouse x.
     * @param y Global mouse y.
     * @return true iff the event was consumed.
     */
    public boolean onMouseUp(MouseEvent e, double x, double y);

    /**
     * Return values of {@link Drawable#onMouseDown(MouseEvent, double, double)}.
     */
    public enum PostMouseDownEvent {
        /**
         * Remove focus from the canvas.
         */
        REMOVE_FOCUS,
        /**
         * Capture drag events.
         */
        CAPTURE_DRAG,

        /**
         * Do not consume the event.
         */
        IGNORE
    }
}
