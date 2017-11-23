package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import elemental2.dom.CanvasRenderingContext2D;
import elemental2.dom.MouseEvent;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.AbstractDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TimeRowDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.TwoDayView;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.CanvasUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.canvas.ColorUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.css.CssResources;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailability;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaplanner.openshift.employeerostering.shared.shift.Shift;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

public class EmployeeDrawable extends AbstractDrawable implements TimeRowDrawable<EmployeeId> {

    TwoDayView<EmployeeId, ?, ?> view;
    EmployeeData data;
    int index;
    boolean isMouseOver;

    public EmployeeDrawable(TwoDayView<EmployeeId, ?, ?> view, EmployeeData data, int index) {
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
            if (data.isLocked()) {
                spot += " (locked)";
            }
        }
        g.fillText(spot, x, y + view.getGroupHeight());
    }

    @Override
    public boolean onMouseMove(MouseEvent e, double x, double y) {
        view.preparePopup(this.toString());
        return true;
    }

    @Override
    public boolean onMouseDown(MouseEvent mouseEvent, double x, double y) {
        SpotRestServiceBuilder.getSpotList(data.getShift().getTenantId(), new FailureShownRestCallback<List<Spot>>() {

            @Override
            public void onSuccess(List<Spot> spotList) {
                CssResources.INSTANCE.errorpopup().ensureInjected();
                PopupPanel popup = new PopupPanel(false);
                popup.setGlassEnabled(true);
                // TODO: Change errorpopup to popup when edit functionality is merged
                popup.setStyleName(CssResources.INSTANCE.errorpopup().panel());

                VerticalPanel panel = new VerticalPanel();
                HorizontalPanel datafield = new HorizontalPanel();

                Label label = new Label("Is Locked");
                CheckBox checkbox = new CheckBox();
                checkbox.setValue(data.isLocked());
                datafield.add(label);
                datafield.add(checkbox);
                panel.add(datafield);

                datafield = new HorizontalPanel();
                label = new Label("Assigned Spot");
                ListBox assignedSpot = new ListBox();
                spotList.forEach((s) -> assignedSpot.addItem(s.getName()));
                if (!data.isLocked()) {
                    assignedSpot.setEnabled(false);
                } else {
                    assignedSpot.setSelectedIndex(spotList.indexOf(data.getSpot()));
                }
                checkbox.addValueChangeHandler((v) -> assignedSpot.setEnabled(v.getValue()));
                datafield.add(label);
                datafield.add(assignedSpot);
                panel.add(datafield);

                datafield = new HorizontalPanel();
                label = new Label("Avaliability");
                ListBox employeeAvaliability = new ListBox();
                int index = 0;
                for (EmployeeAvailabilityState availabilityState : EmployeeAvailabilityState.values()) {
                    employeeAvaliability.addItem(availabilityState.toString());
                    if (null != data.getAvailability() && availabilityState.equals(data.getAvailability().getState())) {
                        employeeAvaliability.setSelectedIndex(index);
                    }
                    index++;
                }
                employeeAvaliability.addItem("NO PREFERENCE");
                if (null == data.getAvailability()) {
                    employeeAvaliability.setSelectedIndex(index);
                }
                datafield.add(label);
                datafield.add(employeeAvaliability);
                panel.add(datafield);

                datafield = new HorizontalPanel();
                Button confirm = new Button();
                // TODO: Use i18n value when edit functionality is merged
                confirm.setText("Confirm");
                confirm.addClickHandler((c) -> {
                    EmployeeAvailabilityState state = null;
                    try {
                        state = EmployeeAvailabilityState.valueOf(employeeAvaliability.getSelectedValue());
                        if (null == data.getAvailability()) {
                            EmployeeAvailabilityView availabilityView = new EmployeeAvailabilityView(data.getShift()
                                    .getTenantId(), data.getEmployee(), data.getShift().getTimeSlot(), state);
                            EmployeeRestServiceBuilder.addEmployeeAvailability(data.getShift().getTenantId(),
                                    availabilityView, new FailureShownRestCallback<Long>() {

                                        @Override
                                        public void onSuccess(Long id) {
                                            view.getCalendar().forceUpdate();
                                        }
                                    });
                        } else {
                            data.getAvailability().setState(state);
                            EmployeeRestServiceBuilder.updateEmployeeAvailability(data.getAvailability().getTenantId(),
                                    data.getAvailability(), new FailureShownRestCallback<Void>() {

                                        @Override
                                        public void onSuccess(Void result) {
                                            view.getCalendar().forceUpdate();
                                        }
                                    });
                        }
                    } catch (IllegalArgumentException e) {
                        if (data.getAvailability() != null) {
                            EmployeeRestServiceBuilder.removeEmployeeAvailability(data.getAvailability().getTenantId(),
                                    data.getAvailability().getId(), new FailureShownRestCallback<Boolean>() {

                                        @Override
                                        public void onSuccess(Boolean result) {
                                            view.getCalendar().forceUpdate();
                                        }
                                    });
                        }
                    }

                    if (checkbox.getValue()) {
                        Spot spot = spotList.stream().filter((e) -> e.getName().equals(assignedSpot.getSelectedValue()))
                                .findFirst().get();
                        popup.hide();
                        ShiftRestServiceBuilder.getShifts(spot.getTenantId(), new FailureShownRestCallback<List<
                                ShiftView>>() {

                            @Override
                            public void onSuccess(List<ShiftView> shifts) {
                                ShiftView shift = shifts.stream().filter((s) -> s.getSpotId().equals(spot.getId()) && s
                                        .getTimeSlotId().equals(data.getShift().getTimeSlot().getId())).findFirst()
                                        .get();
                                data.getShift().setLockedByUser(false);
                                shift.setEmployeeId(data.getEmployee().getId());
                                shift.setLockedByUser(true);
                                if (data.isLocked()) {
                                    ShiftView oldShift = new ShiftView(data.getShift());

                                    ShiftRestServiceBuilder.updateShift(data.getShift().getTenantId(), oldShift,
                                            new FailureShownRestCallback<Void>() {

                                                @Override
                                                public void onSuccess(Void result) {
                                                    ShiftRestServiceBuilder.updateShift(data.getShift().getTenantId(),
                                                            shift, new FailureShownRestCallback<Void>() {

                                                                @Override
                                                                public void onSuccess(Void result2) {
                                                                    view.getCalendar().forceUpdate();
                                                                }

                                                            });
                                                }

                                            });
                                } else {
                                    ShiftRestServiceBuilder.updateShift(data.getShift().getTenantId(), shift,
                                            new FailureShownRestCallback<Void>() {

                                                @Override
                                                public void onSuccess(Void result) {
                                                    view.getCalendar().forceUpdate();
                                                }
                                            });
                                }

                            }
                        });
                    } else if (data.isLocked()) {
                        data.getShift().setLockedByUser(false);
                        ShiftView shiftView = new ShiftView(data.getShift());
                        popup.hide();
                        ShiftRestServiceBuilder.updateShift(data.getShift().getTenantId(), shiftView,
                                new FailureShownRestCallback<Void>() {

                                    @Override
                                    public void onSuccess(Void result) {
                                        view.getCalendar().forceUpdate();
                                    }

                                });
                    } else {
                        popup.hide();
                    }

                });

                Button cancel = new Button();
                // TODO: Replace with i18n later
                cancel.setText("Cancel");
                cancel.addClickHandler((e) -> popup.hide());

                datafield.add(confirm);
                datafield.add(cancel);
                panel.add(datafield);

                popup.setWidget(panel);
                popup.center();
            }
        });

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
        String color = getFillColor();
        CanvasUtils.setFillColor(g, color);

        double start = getStartTime().toEpochSecond(ZoneOffset.UTC) / 60;
        double end = getEndTime().toEpochSecond(ZoneOffset.UTC) / 60;
        double duration = end - start;

        CanvasUtils.drawCurvedRect(g, getLocalX(), getLocalY(), duration * view.getWidthPerMinute(), view
                .getGroupHeight());

        CanvasUtils.setFillColor(g, ColorUtils.getTextColor(color));

        String spot;
        if (null == data.getSpot()) {
            spot = "Unassigned";
        } else {
            spot = data.getSpot().getName();
            if (data.isLocked()) {
                spot += " (locked)";
            }
        }
        g.fillText(spot, getLocalX(), getLocalY() + view.getGroupHeight());

        if (view.getGlobalMouseX() >= getGlobalX() && view.getGlobalMouseX() <= getGlobalX() + view.getWidthPerMinute()
                * duration && view.getGlobalMouseY() >= getGlobalY() && view.getGlobalMouseY() <= getGlobalY() + view
                        .getGroupHeight()) {
            view.preparePopup(this.toString());

        }
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

    private String getFillColor() {
        if (null == data.getAvailability()) {
            return "#0000FF";
        }

        switch (data.getAvailability().getState()) {
            case UNDESIRED:
                return "#FF0000";
            case DESIRED:
                return "#00FF00";
            case UNAVAILABLE:
                return "#000000";
            default:
                return "#FFFFFF";
        }
    }

}
