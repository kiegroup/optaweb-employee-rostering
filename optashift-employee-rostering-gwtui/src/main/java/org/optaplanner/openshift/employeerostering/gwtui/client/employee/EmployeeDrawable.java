package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.MouseEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.AbstractDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TimeRowDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayView;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayViewPresenter;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.ColorUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.css.CssParser;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.css.CssResources;

public class EmployeeDrawable<I extends EmployeeData> extends AbstractDrawable implements TimeRowDrawable<EmployeeId,
        I> {

    TwoDayViewPresenter<EmployeeId, ?, ?> view;
    EmployeeData data;
    int index;
    boolean isMouseOver;

    public EmployeeDrawable(TwoDayViewPresenter<EmployeeId, ?, ?> view, EmployeeData data, int index) {
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

        String spot;
        if (null == data.getSpot()) {
            spot = "Unassigned";
        } else {
            spot = data.getSpot().getName();
        }
        String pad = (data.isLocked()) ? "BB" : "";

        int fontSize = CanvasUtils.fitTextToBox(g, spot + pad, duration * view.getWidthPerMinute() * 0.75, view
                .getGroupHeight() * 0.75);
        g.font = CanvasUtils.getFont(fontSize);
        double[] textSize = CanvasUtils.getPreferredBoxSizeForText(g, spot, fontSize);

        g.fillText(spot, x + (duration * view.getWidthPerMinute() - textSize[0]) * 0.5,
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
    public PostMouseDownEvent onMouseDown(MouseEvent mouseEvent, double x, double y) {
        EmployeeShiftEditForm.create(this);
        return PostMouseDownEvent.REMOVE_FOCUS;
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
    public int getIndex() {
        return index;
    }

    @Override
    public EmployeeId getGroupId() {
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
        StringBuilder out = new StringBuilder(data.getEmployee().getName());
        out.append(' ');
        out.append(CommonUtils.pad(getStartTime().getHour() + "", 2));
        out.append(':');
        out.append(CommonUtils.pad(getStartTime().getMinute() + "", 2));
        out.append('-');
        out.append(CommonUtils.pad(getEndTime().getHour() + "", 2));
        out.append(':');
        out.append(CommonUtils.pad(getEndTime().getMinute() + "", 2));
        out.append(" -- ");
        String spot;
        if (null == data.getSpot()) {
            spot = "Unassigned";
        } else {
            spot = data.getSpot().getName();
            if (data.isLocked()) {
                spot += " (locked)";
            }
        }
        out.append("Assigned to ");
        out.append(spot);
        out.append("; slot is ");
        out.append((data.getAvailability() != null) ? data.getAvailability().getState().toString() : "Indifferent");
        return out.toString();
    }

    public EmployeeData getData() {
        return data;
    }

    public TwoDayViewPresenter<EmployeeId, ?, ?> getCalendarView() {
        return view;
    }

    private String getFillColor() {
        if (null == data.getAvailability()) {
            return CssParser.getCssProperty(CssResources.INSTANCE.calendar(),
                    CssResources.INSTANCE.calendar().employeeShiftViewIndifferent(),
                    "background-color");
        }

        switch (data.getAvailability().getState()) {
            case UNDESIRED:
                return CssParser.getCssProperty(CssResources.INSTANCE.calendar(),
                        CssResources.INSTANCE.calendar().employeeShiftViewUndesired(),
                        "background-color");
            case DESIRED:
                return CssParser.getCssProperty(CssResources.INSTANCE.calendar(),
                        CssResources.INSTANCE.calendar().employeeShiftViewDesired(),
                        "background-color");
            case UNAVAILABLE:
                return CssParser.getCssProperty(CssResources.INSTANCE.calendar(),
                        CssResources.INSTANCE.calendar().employeeShiftViewUnavailable(),
                        "background-color");
            default:
                return CssParser.getCssProperty(CssResources.INSTANCE.calendar(),
                        CssResources.INSTANCE.calendar().employeeShiftViewIndifferent(),
                        "background-color");
        }
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
