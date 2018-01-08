package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.MouseEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.AbstractDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Drawable.PostMouseDownEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.HasTitle;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayViewPresenter;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils.Glyphs;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.interfaces.HasTimeslot;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotId;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;

public class SpotToolbox extends AbstractDrawable {

    private double x, y;
    public final static int WIDTH = 40;
    public final static int HEIGHT = 120;
    boolean onRemove;
    boolean onEdit;
    boolean onClone;
    SpotDrawable<SpotData> drawable;
    TwoDayViewPresenter<SpotId, SpotData, SpotDrawable<SpotData>> presenter;

    public SpotToolbox(SpotDrawable<SpotData> parent, TwoDayViewPresenter<SpotId, SpotData, SpotDrawable<
            SpotData>> view, double x,
            double y) {
        this.x = Math.max(TwoDayViewPresenter.SPOT_NAME_WIDTH, x + view.getLocationOfDate(parent.getStartTime()));
        this.y = Math.max(TwoDayViewPresenter.HEADER_HEIGHT, y + view.getLocationOfGroupSlot(parent.getGroupId(),
                parent.getIndex()));
        this.presenter = view;
        this.drawable = parent;
        onRemove = false;
        onEdit = false;
        onClone = false;
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
        CanvasUtils.drawGlyph(g, Glyphs.REMOVE, 20, x + 10, y + 40);

        if (!onEdit) {
            CanvasUtils.setFillColor(g, "#030303");
        } else {
            CanvasUtils.setFillColor(g, "#fafafa");
        }
        CanvasUtils.drawGlyph(g, Glyphs.EDIT, 20, x + 10, y + 80);

        if (!onClone) {
            CanvasUtils.setFillColor(g, "#030303");
        } else {
            CanvasUtils.setFillColor(g, "#fafafa");
        }
        CanvasUtils.drawGlyph(g, Glyphs.ADD, 20, x + 10, y + 120);
    }

    @Override
    public PostMouseDownEvent onMouseDown(MouseEvent e, double x, double y) {
        double localX = x - this.x;
        double localY = y - this.y;

        if (localX < 0 || localY < 0 || localX > WIDTH || localX > HEIGHT) {
            return PostMouseDownEvent.IGNORE;
        } else {
            if (localX > 0 && localY > 0 && localX < 40 && localY < 40) {
                ShiftRestServiceBuilder.removeShift(drawable.getData().getSpot().getTenantId(), drawable.getData()
                        .getShift().getId(), new FailureShownRestCallback<Boolean>() {

                            @Override
                            public void onSuccess(Boolean removed) {
                                presenter.removeDrawable(drawable.getData(), drawable);
                            }
                        });
                presenter.setToolBox(null);
                return PostMouseDownEvent.REMOVE_FOCUS;
            } else if (localX > 0 && localY > 40 && localX < 40 && localY < 80) {
                SpotShiftEditForm.create(drawable);
                presenter.setToolBox(null);
                return PostMouseDownEvent.REMOVE_FOCUS;
            } else if (localX > 0 && localY > 80 && localX < 40 && localY < 120) {
                ShiftRestServiceBuilder.addShift(drawable.getData().getSpot().getTenantId(), new ShiftView(drawable
                        .getData().getSpot().getTenantId(), drawable.getData().getSpot(), drawable.getData().getShift()
                                .getTimeSlot()), new FailureShownRestCallback<Long>() {

                                    @Override
                                    public void onSuccess(Long shiftId) {
                                        presenter.getCalendar().forceUpdate();
                                    }
                                });
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

        onRemove = false;
        onEdit = false;
        onClone = false;
        if (localX < 0 || localY < 0 || localX > WIDTH || localX > HEIGHT) {
            return false;
        } else {
            if (localX > 0 && localY > 0 && localX < 40 && localY < 40) {
                onRemove = true;
                return true;
            } else if (localX > 0 && localY > 40 && localX < 40 && localY < 80) {
                onEdit = true;
                return true;
            } else if (localX > 0 && localY > 80 && localX < 40 && localY < 120) {
                onClone = true;
                return true;
            }

            return true;
        }
    }

}
