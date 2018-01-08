package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.MouseEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Drawable.PostMouseDownEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayViewPresenter;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils.Glyphs;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotId;

public class ShiftToolbox extends AbstractDrawable {

    private double x, y;
    public final static int WIDTH = 40;
    public final static int HEIGHT = 80;
    boolean onRemove;
    ShiftDrawable drawable;
    TwoDayViewPresenter<SpotId, ShiftData, ShiftDrawable> presenter;

    public ShiftToolbox(ShiftDrawable parent, TwoDayViewPresenter<SpotId, ShiftData, ShiftDrawable> presenter, double x,
            double y) {
        this.x = Math.max(TwoDayViewPresenter.SPOT_NAME_WIDTH, x + presenter.getLocationOfDate(parent.getStartTime()));
        this.y = Math.max(TwoDayViewPresenter.HEADER_HEIGHT, y + presenter.getLocationOfGroupSlot(parent.getGroupId(),
                parent.getIndex()));
        this.presenter = presenter;
        this.drawable = parent;
        onRemove = false;
    }

    @Override
    public double getLocalX() {
        return x;
    }

    @Override
    public double getLocalY() {
        return y;
    }

    @Override
    public void doDraw(CanvasRenderingContext2D g) {
        CanvasUtils.setFillColor(g, "#72767b");
        g.fillRect(x, y, WIDTH, HEIGHT);
        if (!onRemove) {
            CanvasUtils.setFillColor(g, "#030303");
        } else {
            CanvasUtils.setFillColor(g, "#fafafa");
        }
        CanvasUtils.drawGlyph(g, Glyphs.REMOVE, 20, x + 10, y + 50);
    }

    @Override
    public PostMouseDownEvent onMouseDown(MouseEvent e, double x, double y) {
        double localX = x - this.x;
        double localY = y - this.y;

        if (localX < 0 || localY < 0 || localX > WIDTH || localX > HEIGHT) {
            return PostMouseDownEvent.IGNORE;
        } else {
            if (localX > 10 && localY > 30 && localX < 40 && localY < 60) {
                presenter.removeDrawable(drawable.id, drawable);
                presenter.setToolBox(null);
                return PostMouseDownEvent.REMOVE_FOCUS;
            }
            return PostMouseDownEvent.REMOVE_FOCUS;
        }
    }

    @Override
    public boolean onMouseMove(MouseEvent e, double x, double y) {
        double localX = x - this.x;
        double localY = y - this.y;

        if (localX < 0 || localY < 0 || localX > WIDTH || localX > HEIGHT) {
            onRemove = false;
            return false;
        } else {
            if (localX > 10 && localY > 30 && localX < 40 && localY < 60) {
                onRemove = true;
            } else {
                onRemove = false;
            }
            return true;
        }
    }

}
