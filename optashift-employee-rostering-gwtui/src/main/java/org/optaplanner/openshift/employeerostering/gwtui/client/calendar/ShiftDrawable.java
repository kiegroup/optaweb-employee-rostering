package org.optaplanner.openshift.employeerostering.gwtui.client.calendar;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.MouseEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.ColorUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotData;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotId;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

public class ShiftDrawable extends AbstractDrawable implements TimeRowDrawable<SpotId> {

    SpotId spot;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String color;
    int index;
    boolean isMouseOver;
    TwoDayView<SpotId, ShiftData, ShiftDrawable> view;
    ShiftData id;
    Long minsOffset;

    public ShiftDrawable(TwoDayView<SpotId, ShiftData, ShiftDrawable> view, ShiftData data, int index) {
        this.view = view;
        this.startTime = data.getStartTime();
        this.endTime = data.getEndTime();
        this.spot = data.getGroupId();
        this.color = ColorUtils.getColor(view.getGroupIndex(data.getGroupId()));
        this.index = index;
        this.isMouseOver = false;
        this.id = data;
    }

    @Override
    public void doDraw(CanvasRenderingContext2D g) {
        CanvasUtils.setFillColor(g, color);

        double start = startTime.toEpochSecond(ZoneOffset.UTC) / 60;
        double end = endTime.toEpochSecond(ZoneOffset.UTC) / 60;
        double duration = end - start;

        CanvasUtils.drawCurvedRect(g, getLocalX(), getLocalY(), duration * view.getWidthPerMinute(), view
                .getGroupHeight());

        CanvasUtils.setFillColor(g, ColorUtils.getTextColor(color));
        g.fillText(spot.getTitle(), getLocalX(), getLocalY() + view.getGroupHeight());
    }

    @Override
    public void doDrawAt(CanvasRenderingContext2D g, double x, double y) {
        CanvasUtils.setFillColor(g, (isMouseOver) ? ColorUtils.brighten(color) : color);

        double start = startTime.toEpochSecond(ZoneOffset.UTC) / 60;
        double end = endTime.toEpochSecond(ZoneOffset.UTC) / 60;
        double duration = end - start;

        CanvasUtils.drawCurvedRect(g, x, y, duration * view.getWidthPerMinute(), view.getGroupHeight());

        CanvasUtils.setFillColor(g, ColorUtils.getTextColor(color));
        g.fillText(spot.getTitle(), x, y + view.getGroupHeight());

        CanvasUtils.setFillColor(g, "#FF0000");
        g.fillRect(x, y, 30, 30);
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
        if (x - getGlobalX() < 30 && y - getGlobalY() < 30) {
            view.getCalendar().removeShift(id);
            return PostMouseDownEvent.REMOVE_FOCUS;
        } else {
            minsOffset = (startTime.toEpochSecond(
                    ZoneOffset.UTC) - view.getMouseLocalDateTime().toEpochSecond(ZoneOffset.UTC)) / 60;
            return PostMouseDownEvent.CAPTURE_DRAG;
        }
    }

    @Override
    public boolean onMouseDrag(MouseEvent e, double x, double y) {
        long mins = (endTime.toEpochSecond(ZoneOffset.UTC) - startTime.toEpochSecond(ZoneOffset.UTC)) / 60;
        startTime = view.roundLocalDateTime(view.getMouseLocalDateTime().plusMinutes(minsOffset));
        endTime = startTime.plusMinutes(mins);
        return true;
    }

    @Override
    public boolean onMouseUp(MouseEvent e, double x, double y) {
        ShiftData shift = new ShiftData(new SpotData(new Shift(spot.getSpot().getTenantId(), spot.getSpot(),
                new TimeSlot(spot.getSpot().getTenantId(), startTime, endTime))));
        view.getCalendar().removeShift(id);
        view.getCalendar().addShift(shift);
        minsOffset = null;
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

}
