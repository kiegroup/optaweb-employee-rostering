package org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.powers;

import java.util.function.Supplier;

import elemental2.dom.MouseEvent;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Blob;
import org.optaplanner.openshift.employeerostering.gwtui.client.rostergrid.model.Viewport;

public class BlobMouseHandler<T> {

    private Supplier<Blob<T>> blob;
    private Supplier<Viewport<T>> viewport;
    private IsElement blobView;

    private DragHandler dragHandler;
    private ResizeHandler resizeHandler;

    private ResizeFromPosition resizingFrom;
    private MouseAction mouseAction;
    private double mouseStartPos;
    private Long blobStartPos;
    private Long blobEndPos;

    public BlobMouseHandler(Supplier<Blob<T>> blob, Supplier<Viewport<T>> viewport, IsElement blobView) {
        this.blob = blob;
        this.viewport = viewport;
        this.blobView = blobView;
        this.mouseAction = MouseAction.HOVER;
        this.dragHandler = null;
        this.resizeHandler = null;

        blobView.getElement().addEventListener("mousedown", (e) -> onMouseDown((MouseEvent) e));
        blobView.getElement().addEventListener("mousemove", (e) -> onMouseMove((MouseEvent) e));
        blobView.getElement().addEventListener("mouseup", (e) -> onMouseUp((MouseEvent) e));
    }

    public BlobMouseHandler<T> withDragHandler(DragHandler dragHandler) {
        this.dragHandler = dragHandler;
        return this;
    }

    public BlobMouseHandler<T> withResizeHandler(ResizeHandler resizeHandler) {
        this.resizeHandler = resizeHandler;
        return this;
    }

    private void onMouseDown(MouseEvent e) {
        viewport.get().setMouseTarget(blobView);
        double gridPositionOfMouse = viewport.get().getGridPositionOfMouse(blob.get(), e);
        mouseStartPos = gridPositionOfMouse;
        blobStartPos = blob.get().getPositionInGridPixels();
        blobEndPos = blob.get().getEndPositionInGridPixels();

        if (gridPositionOfMouse < blob.get().getPositionInGridPixels() + 0.3) {
            resizingFrom = ResizeFromPosition.START;
            mouseAction = MouseAction.RESIZING;
        } else if (gridPositionOfMouse > blob.get().getEndPositionInGridPixels() - 0.7) {
            resizingFrom = ResizeFromPosition.END;
            mouseAction = MouseAction.RESIZING;
        } else {
            mouseAction = MouseAction.DRAGGING;
        }
    }

    private void onMouseMove(MouseEvent e) {
        double gridPositionOfMouse = viewport.get().getGridPositionOfMouse(blob.get(), e);
        switch (mouseAction) {
            case HOVER:
                if (resizeHandler != null && gridPositionOfMouse < blob.get().getPositionInGridPixels() + 0.3) {
                    blobView.getElement().style.cursor = "ew-resize";
                } else if (resizeHandler != null && gridPositionOfMouse > blob.get().getEndPositionInGridPixels() - 0.7) {
                    blobView.getElement().style.cursor = "ew-resize";
                } else if (dragHandler != null) {
                    blobView.getElement().style.cursor = "move";
                } else {
                    blobView.getElement().style.cursor = "auto";
                }
                break;
            case DRAGGING:
                if (dragHandler != null) {
                    dragHandler.onDrag(new BlobMouseEvent(mouseAction, resizingFrom, blobStartPos, blobEndPos,
                            mouseStartPos, gridPositionOfMouse));
                }
                break;
            case RESIZING:
                if (resizeHandler != null) {
                    resizeHandler.onResize(new BlobMouseEvent(mouseAction, resizingFrom, blobStartPos, blobEndPos,
                            mouseStartPos, gridPositionOfMouse));
                }
                break;
            default:
                throw new IllegalStateException("Missing mouseAction (" + mouseAction + ") in BlobMouseHandler.onMouseMove");

        }
    }

    private void onMouseUp(MouseEvent e) {
        if (viewport.get().getMouseTarget() != null && viewport.get().getMouseTarget().equals(blobView)) {
            viewport.get().setMouseTarget(null);
        }
        mouseAction = MouseAction.HOVER;
    }

    public interface DragHandler {

        void onDrag(BlobMouseEvent e);
    }

    public interface ResizeHandler {

        void onResize(BlobMouseEvent e);
    }

    public static final class BlobMouseEvent {

        private final MouseAction action;
        private final ResizeFromPosition resizingFrom;
        private final Long blobOriginalStartPositionInGridPixels;
        private final Long blobOriginalEndPositionInGridPixels;
        private final double mouseStartPositionInGridPixels;
        private final double mousePositionInGridPixels;

        public BlobMouseEvent(MouseAction action, ResizeFromPosition resizingFrom, Long blobStartPositionInGridPixels, Long blobEndPositionInGridPixels, double mouseStartPositionInGridPixels,
                              double mousePositionInGridPixels) {
            this.action = action;
            this.resizingFrom = resizingFrom;
            this.blobOriginalStartPositionInGridPixels = blobStartPositionInGridPixels;
            this.blobOriginalEndPositionInGridPixels = blobEndPositionInGridPixels;
            this.mouseStartPositionInGridPixels = mouseStartPositionInGridPixels;
            this.mousePositionInGridPixels = mousePositionInGridPixels;
        }

        public MouseAction getAction() {
            return action;
        }

        public ResizeFromPosition getResizingFrom() {
            return resizingFrom;
        }

        public Long getOriginalBlobStartPositionInGridPixels() {
            return blobOriginalStartPositionInGridPixels;
        }

        public Long getOriginalBlobEndPositionInGridPixels() {
            return blobOriginalEndPositionInGridPixels;
        }

        public double getMouseStartPositionInGridPixels() {
            return mouseStartPositionInGridPixels;
        }

        public double getMousePositionInGridPixels() {
            return mousePositionInGridPixels;
        }

        public double getMousePositionDeltaInGridPixels() {
            return getMousePositionInGridPixels() - getMouseStartPositionInGridPixels();
        }

        public Long getOriginalBlobLengthInGridPixels() {
            return getOriginalBlobEndPositionInGridPixels() - getOriginalBlobStartPositionInGridPixels();
        }

    }

    public static enum ResizeFromPosition {
        START,
        END;
    }

    public static enum MouseAction {
        HOVER,
        DRAGGING,
        RESIZING;
    }
}
