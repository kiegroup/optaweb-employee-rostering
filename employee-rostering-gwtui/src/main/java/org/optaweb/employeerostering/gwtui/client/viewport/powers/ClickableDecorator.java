package org.optaweb.employeerostering.gwtui.client.viewport.powers;

import java.util.function.Consumer;

import elemental2.dom.Event;
import elemental2.dom.MouseEvent;
import org.optaweb.employeerostering.gwtui.client.viewport.grid.GridObject;

public class ClickableDecorator<T, M> {

    private Consumer<MouseEvent> onClick;

    private double startX;
    private double startY;
    private double dx;
    private double dy;

    private final double MOVE_TOLERANCE = 20;

    public ClickableDecorator<T, M> applyFor(GridObject<T, M> gridObject) {
        gridObject.getElement().addEventListener("mousedown", this::gridObjectMouseDownListener);
        gridObject.getElement().addEventListener("mousemove", this::gridObjectMouseMoveListener);
        gridObject.getElement().addEventListener("click", this::gridObjectMouseClickListener);
        return this;
    }

    public ClickableDecorator<T, M> onClick(Consumer<MouseEvent> onClick) {
        this.onClick = onClick;
        return this;
    }

    public void gridObjectMouseDownListener(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        startX = mouseEvent.screenX;
        startY = mouseEvent.screenY;
        dx = 0;
        dy = 0;
    }

    public void gridObjectMouseMoveListener(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        dx += Math.abs(startX - mouseEvent.screenX);
        dy += Math.abs(startY - mouseEvent.screenY);
        startX = mouseEvent.screenX;
        startY = mouseEvent.screenY;
    }

    public void gridObjectMouseClickListener(Event event) {
        MouseEvent mouseEvent = (MouseEvent) event;
        if (dx + dy <= MOVE_TOLERANCE) {
            onClick.accept(mouseEvent);
        }
    }
}
