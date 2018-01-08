package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Panel;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayView;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.FormPopup;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

@Templated
public class EmployeeShiftEditForm implements IsElement {

    @Inject
    @DataField
    private CheckBox isLocked;

    @Inject
    @DataField
    private ListBox assignedSpot;

    @Inject
    @DataField
    private ListBox employeeAvaliability;

    @Inject
    @DataField
    private Button saveButton;

    @Inject
    @DataField
    private Button cancelButton;

    @Inject
    @DataField
    private Button closeButton;

    @Inject
    @DataField
    private @Named(value = "h3") HeadingElement title;

    @Inject
    private TranslationService CONSTANTS;

    private static EmployeeDrawable employee;

    private FormPopup popup;

    private List<Spot> spotList;

    public static EmployeeShiftEditForm create(EmployeeDrawable employeeData) {
        employee = employeeData;
        return (EmployeeShiftEditForm) employeeData.getCalendarView().getCalendar().getInstanceOf(
                EmployeeShiftEditForm.class);
    }

    @PostConstruct
    protected void initWidget() {
        isLocked.setValue(employee.getData().isLocked());
        title.setInnerSafeHtml(new SafeHtmlBuilder().appendEscaped(employee.getData().getEmployee().getName())
                .appendEscaped(" - ")
                .appendEscaped(employee.getData().getStartTime().toLocalTime().toString())
                .appendEscaped("-")
                .appendEscaped(employee.getData().getEndTime().toLocalTime().toString())
                .toSafeHtml());
        EmployeeShiftEditForm form = this;
        SpotRestServiceBuilder.getSpotList(employee.getData().getShift().getTenantId(), new FailureShownRestCallback<
                List<
                        Spot>>() {

            @Override
            public void onSuccess(List<Spot> tenantSpotList) {
                spotList = tenantSpotList;
                spotList.forEach((s) -> assignedSpot.addItem(s.getName()));
                if (!employee.getData().isLocked()) {
                    assignedSpot.setEnabled(false);
                } else {
                    assignedSpot.setSelectedIndex(spotList.indexOf(employee.getData().getSpot()));
                }
                isLocked.addValueChangeHandler((v) -> assignedSpot.setEnabled(v.getValue()));

                int index = 0;
                for (EmployeeAvailabilityState availabilityState : EmployeeAvailabilityState.values()) {
                    employeeAvaliability.addItem(availabilityState.toString());
                    if (null != employee.getData().getAvailability() && availabilityState.equals(employee.getData()
                            .getAvailability().getState())) {
                        employeeAvaliability.setSelectedIndex(index);
                    }
                    index++;
                }
                employeeAvaliability.addItem("NO PREFERENCE");
                if (null == employee.getData().getAvailability()) {
                    employeeAvaliability.setSelectedIndex(index);
                }

                popup = FormPopup.getFormPopup(form);
                popup.center();
            }
        });
    }

    @EventHandler("cancelButton")
    public void cancel(ClickEvent e) {
        popup.hide();
    }

    @EventHandler("closeButton")
    public void close(ClickEvent e) {
        popup.hide();
    }

    @EventHandler("saveButton")
    public void save(ClickEvent e) {
        EmployeeAvailabilityState state = null;
        try {
            state = EmployeeAvailabilityState.valueOf(employeeAvaliability.getSelectedValue());
            if (null == employee.getData().getAvailability()) {
                EmployeeAvailabilityView availabilityView = new EmployeeAvailabilityView(employee.getData().getShift()
                        .getTenantId(), employee.getData().getEmployee(), employee.getData().getShift().getTimeSlot(),
                        state);
                EmployeeRestServiceBuilder.addEmployeeAvailability(employee.getData().getShift().getTenantId(),
                        availabilityView, new FailureShownRestCallback<Long>() {

                            @Override
                            public void onSuccess(Long id) {
                                employee.getCalendarView().getCalendar().forceUpdate();
                            }
                        });
            } else {
                employee.getData().getAvailability().setState(state);
                EmployeeRestServiceBuilder.updateEmployeeAvailability(employee.getData().getAvailability()
                        .getTenantId(),
                        employee.getData().getAvailability(), new FailureShownRestCallback<Void>() {

                            @Override
                            public void onSuccess(Void result) {
                                employee.getCalendarView().getCalendar().forceUpdate();
                            }
                        });
            }
        } catch (IllegalArgumentException error) {
            if (employee.getData().getAvailability() != null) {
                EmployeeRestServiceBuilder.removeEmployeeAvailability(employee.getData().getAvailability()
                        .getTenantId(),
                        employee.getData().getAvailability().getId(), new FailureShownRestCallback<Boolean>() {

                            @Override
                            public void onSuccess(Boolean result) {
                                employee.getCalendarView().getCalendar().forceUpdate();
                            }
                        });
            }
        }

        if (isLocked.getValue()) {
            Spot spot = spotList.stream().filter((m_employee) -> m_employee.getName().equals(assignedSpot
                    .getSelectedValue()))
                    .findFirst().get();
            popup.hide();
            ShiftRestServiceBuilder.getShifts(spot.getTenantId(), new FailureShownRestCallback<List<
                    ShiftView>>() {

                @Override
                public void onSuccess(List<ShiftView> shifts) {
                    ShiftView shift = shifts.stream().filter((s) -> s.getSpotId().equals(spot.getId()) && s
                            .getTimeSlotId().equals(employee.getData().getShift().getTimeSlot().getId())).findFirst()
                            .orElseGet(() -> null);
                    if (null != shift) {
                        employee.getData().getShift().setLockedByUser(false);
                        shift.setEmployeeId(employee.getData().getEmployee().getId());
                        shift.setLockedByUser(true);
                        if (employee.getData().isLocked()) {
                            ShiftView oldShift = new ShiftView(employee.getData().getShift());

                            ShiftRestServiceBuilder.updateShift(employee.getData().getShift().getTenantId(), oldShift,
                                    new FailureShownRestCallback<Void>() {

                                        @Override
                                        public void onSuccess(Void result) {
                                            ShiftRestServiceBuilder.updateShift(employee.getData().getShift()
                                                    .getTenantId(),
                                                    shift, new FailureShownRestCallback<Void>() {

                                                        @Override
                                                        public void onSuccess(Void result2) {
                                                            employee.getCalendarView().getCalendar().forceUpdate();
                                                        }

                                                    });
                                        }

                                    });
                        } else {
                            ShiftRestServiceBuilder.updateShift(employee.getData().getShift().getTenantId(), shift,
                                    new FailureShownRestCallback<Void>() {

                                        @Override
                                        public void onSuccess(Void result) {
                                            employee.getCalendarView().getCalendar().forceUpdate();
                                        }
                                    });
                        }

                    } else {
                        ErrorPopup.show("Cannot find shift with spot " + spot.getName() + " for timeslot "
                                + employee.getData().getShift().getTimeSlot());
                    }
                }
            });
        } else if (employee.getData().isLocked()) {
            employee.getData().getShift().setLockedByUser(false);
            ShiftView shiftView = new ShiftView(employee.getData().getShift());
            popup.hide();
            ShiftRestServiceBuilder.updateShift(employee.getData().getShift().getTenantId(), shiftView,
                    new FailureShownRestCallback<Void>() {

                        @Override
                        public void onSuccess(Void result) {
                            employee.getCalendarView().getCalendar().forceUpdate();
                        }

                    });
        } else {
            popup.hide();
        }
    }

}
