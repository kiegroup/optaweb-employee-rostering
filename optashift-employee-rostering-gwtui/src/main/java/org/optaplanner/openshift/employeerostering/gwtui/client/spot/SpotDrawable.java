package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.MouseEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.AbstractDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TimeRowDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayViewPresenter;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.ColorUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.css.CssParser;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.css.CssResources;

public class SpotDrawable<I extends SpotData> extends AbstractDrawable implements TimeRowDrawable<SpotId, I> {

    TwoDayViewPresenter<SpotId, SpotData, SpotDrawable<SpotData>> view;
    SpotData data;
    int index;
    boolean isMouseOver;

    public SpotDrawable(TwoDayViewPresenter<SpotId, SpotData, SpotDrawable<SpotData>> view, SpotData data, int index) {
        this.view = view;
        this.data = data;
        this.index = index;
        this.isMouseOver = false;
        //ErrorPopup.show(this.toString());
    }

    @Override
    public double getLocalX() {
        double start = getStartTime().toEpochSecond(ZoneOffset.UTC) / 60;
        return start * view.getWidthPerMinute();
    }

    @Override
    public double getLocalY() {
        Integer cursorIndex = view.getCursorIndex(getGroupId());
        return (null != cursorIndex && cursorIndex > index) ? index * view.getGroupHeight() : (index + 1) * view
                .getGroupHeight();
    }

    @Override
    public void doDrawAt(CanvasRenderingContext2D g, double x, double y) {
        String color = (isMouseOver) ? ColorUtils.brighten(getFillColor()) : getFillColor();
        CanvasUtils.setFillColor(g, color);

        double start = getStartTime().toEpochSecond(ZoneOffset.UTC) / 60;
        double end = getEndTime().toEpochSecond(ZoneOffset.UTC) / 60;
        double duration = end - start;

        CanvasUtils.drawCurvedRect(g, x, y, duration * view.getWidthPerMinute(), view.getGroupHeight());

        CanvasUtils.setFillColor(g, ColorUtils.getTextColor(color));

        String employee;
        if (null == data.getAssignedEmployee()) {
            employee = "Unassigned";
        } else {
            employee = data.getAssignedEmployee().getName();
        }

        String pad = (data.isLocked()) ? "BB" : "";

        int fontSize = CanvasUtils.fitTextToBox(g, employee + pad, duration * view.getWidthPerMinute() * 0.75, view
                .getGroupHeight() * 0.75);
        g.font = CanvasUtils.getFont(fontSize);
        double[] textSize = CanvasUtils.getPreferredBoxSizeForText(g, employee, fontSize);

        g.fillText(employee, x + (duration * view.getWidthPerMinute() - textSize[0]) * 0.5,
                y + (view.getGroupHeight() + textSize[1] * 0.375) * 0.5);

        if (data.isLocked()) {
            CanvasUtils.drawGlyph(g, CanvasUtils.Glyphs.LOCK, fontSize, x +
                    (duration * view.getWidthPerMinute() + textSize[0]) * 0.5, y + (view.getGroupHeight() + textSize[1]
                            * 0.375)
                            * 0.5);
        }
    }

    @Override
    public boolean onMouseMove(MouseEvent e, double x, double y) {
        view.preparePopup(this.toString());
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

    public SpotData getData() {
        return data;
    }

    public TwoDayViewPresenter<SpotId, ?, ?> getCalendarView() {
        return view;
    }

    @Override
    public PostMouseDownEvent onMouseDown(MouseEvent mouseEvent, double x, double y) {
        view.setToolBox(new SpotToolbox((SpotDrawable<SpotData>) this, view, -SpotToolbox.WIDTH, -SpotToolbox.HEIGHT));
        return PostMouseDownEvent.CAPTURE_DRAG;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public SpotId getGroupId() {
        return data.getGroupId();
    }

    @Override
    public LocalDateTime getStartTime() {
        return data.getStartTime();
    }

    @Override
    public LocalDateTime getEndTime() {
        return data.getEndTime();
    }

    @Override
    public void doDraw(CanvasRenderingContext2D g) {
        doDrawAt(g, getGlobalX(), getGlobalY());
    }

    public String toString() {
        StringBuilder out = new StringBuilder(data.getSpot().getName());
        out.append(' ');
        out.append(CommonUtils.pad(getStartTime().getHour() + "", 2));
        out.append(':');
        out.append(CommonUtils.pad(getStartTime().getMinute() + "", 2));
        out.append('-');
        out.append(CommonUtils.pad(getEndTime().getHour() + "", 2));
        out.append(':');
        out.append(CommonUtils.pad(getEndTime().getMinute() + "", 2));
        out.append(" -- ");
        String employee;
        if (null == data.getAssignedEmployee()) {
            employee = "no one";
        } else {
            employee = data.getAssignedEmployee().getName();
            if (data.isLocked()) {
                employee += " (locked)";
            }
        }
        out.append("Assigned to ");
        out.append(employee);
        return out.toString();
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
    public void updateData(I newData) {
        this.data = newData;
    }

}
