package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.MouseEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayViewPresenter;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.ColorUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.css.CssParser;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.css.CssResources;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotData;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotId;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

public class ShiftDrawable extends AbstractDrawable implements TimeRowDrawable<SpotId, ShiftData> {

    SpotId spot;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String color;
    int index;
    boolean isMouseOver, isMoving, dragged;
    TwoDayViewPresenter<SpotId, ShiftData, ShiftDrawable> view;
    ShiftData id;
    Long minsOffset;

    public ShiftDrawable(TwoDayViewPresenter<SpotId, ShiftData, ShiftDrawable> view, ShiftData data, int index) {
        this.view = view;
        this.startTime = data.getStartTime();
        this.endTime = data.getEndTime();
        this.spot = data.getGroupId();
        this.color = ColorUtils.getColor(view.getGroupIndex(data.getGroupId()));
        this.index = index;
        this.isMouseOver = false;
        this.dragged = false;
        this.id = data;
    }

    @Override
    public void doDraw(CanvasRenderingContext2D g) {
        doDrawAt(g, getGlobalX(), getGlobalY());
    }

    @Override
    public void doDrawAt(CanvasRenderingContext2D g, double x, double y) {
        String color = (isMouseOver) ? ColorUtils.brighten(getFillColor()) : getFillColor();
        CanvasUtils.setFillColor(g, color);

        double start = startTime.toEpochSecond(ZoneOffset.UTC) / 60;
        double end = endTime.toEpochSecond(ZoneOffset.UTC) / 60;
        double duration = end - start;

        CanvasUtils.drawCurvedRect(g, x, y, duration * view.getWidthPerMinute(), view.getGroupHeight());

        CanvasUtils.setFillColor(g, ColorUtils.getTextColor(color));
        int fontSize = CanvasUtils.fitTextToBox(g, spot.getTitle(), duration * view.getWidthPerMinute() * 0.75, view
                .getGroupHeight() * 0.75);
        g.font = CanvasUtils.getFont(fontSize);
        double[] textSize = CanvasUtils.getPreferredBoxSizeForText(g, spot.getTitle(), fontSize);

        g.fillText(spot.getTitle(), x + (duration * view.getWidthPerMinute() - textSize[0]) * 0.5,
                y + (view.getGroupHeight() + textSize[1] * 0.375) * 0.5);

        CanvasUtils.setFillColor(g, "#000000");
        g.fillRect(x + duration * view.getWidthPerMinute() - 30, y + view.getGroupHeight() - 30, 30, 30);
    }

    @Override
    public double getLocalX() {
        double start = startTime.toEpochSecond(ZoneOffset.UTC) / 60;
        return start * view.getWidthPerMinute();
    }

    @Override
    public double getLocalY() {
        Integer cursorIndex = view.getCursorIndex(spot);
        return (null != cursorIndex && cursorIndex > index) ? index * view.getGroupHeight() : (index + 1) * view
                .getGroupHeight();
    }

    @Override
    public boolean onMouseMove(MouseEvent e, double x, double y) {
        view.preparePopup(this.toString());
        return true;
    }

    @Override
    public PostMouseDownEvent onMouseDown(MouseEvent e, double x, double y) {
        double start = startTime.toEpochSecond(ZoneOffset.UTC) / 60;
        double end = endTime.toEpochSecond(ZoneOffset.UTC) / 60;
        double duration = end - start;

        if (view.getLocationOfDate(endTime) - x + TwoDayViewPresenter.SPOT_NAME_WIDTH < 30 && y - getGlobalY() - view
                .getGroupHeight() + 30 > 0) {
            isMoving = false;
            return PostMouseDownEvent.CAPTURE_DRAG;
        } else {
            isMoving = true;
            minsOffset = (startTime.toEpochSecond(
                    ZoneOffset.UTC) - view.getMouseLocalDateTime().toEpochSecond(ZoneOffset.UTC)) / 60;
            return PostMouseDownEvent.CAPTURE_DRAG;
        }
    }

    @Override
    public boolean onMouseDrag(MouseEvent e, double x, double y) {
        view.preparePopup(this.toString());
        dragged = true;
        if (isMoving) {
            long mins = (endTime.toEpochSecond(ZoneOffset.UTC) - startTime.toEpochSecond(ZoneOffset.UTC)) / 60;
            startTime = view.roundLocalDateTime(view.getMouseLocalDateTime().plusMinutes(minsOffset));
            endTime = startTime.plusMinutes(mins);
        } else {
            LocalDateTime newEndTime = view.roundLocalDateTime(view.getMouseLocalDateTime());
            if (newEndTime.isAfter(startTime)) {
                endTime = newEndTime;
            }
        }
        return true;
    }

    @Override
    public boolean onMouseUp(MouseEvent e, double x, double y) {
        if (dragged) {
            ShiftData shift = new ShiftData(new SpotData(new Shift(spot.getSpot().getTenantId(), spot.getSpot(),
                    new TimeSlot(spot.getSpot().getTenantId(), startTime, endTime))));
            shift.shift.getShift().setId(id.shift.getShift().getId());
            view.removeDrawable(id, this);
            view.addShift(shift);
        } else {
            view.setToolBox(new ShiftToolbox(this, view, -ShiftToolbox.WIDTH, 0));
        }
        minsOffset = null;
        dragged = false;
        return true;
    }

    @Override
    public boolean onMouseEnter(MouseEvent e, double x, double y) {
        isMouseOver = true;
        return true;
    }

    @Override
    public boolean onMouseExit(MouseEvent e, double x, double y) {
        isMouseOver = false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(spot.getTitle());
        out.append(' ');
        out.append(CommonUtils.pad(startTime.getHour() + "", 2));
        out.append(':');
        out.append(CommonUtils.pad(startTime.getMinute() + "", 2));
        out.append('-');
        out.append(CommonUtils.pad(endTime.getHour() + "", 2));
        out.append(':');
        out.append(CommonUtils.pad(endTime.getMinute() + "", 2));
        return out.toString();
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public SpotId getGroupId() {
        return spot;
    }

    private String getFillColor() {
        return CssParser.getCssProperty(CssResources.INSTANCE.calendar(),
                CssResources.INSTANCE.calendar().spotShiftView(),
                "background-color");
    }

    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ShiftDrawable) {
            ShiftDrawable other = (ShiftDrawable) o;
            return this.id.equals(other.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public void updateData(ShiftData newData) {
        this.id = newData;
    }

}
