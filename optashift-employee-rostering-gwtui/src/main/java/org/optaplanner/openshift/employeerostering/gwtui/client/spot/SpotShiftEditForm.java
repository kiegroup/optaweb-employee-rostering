package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayView;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.FormPopup;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

@Templated
public class SpotShiftEditForm implements IsElement {

    @Inject
    @DataField
    private CheckBox isLocked;

    @Inject
    @DataField
    private ListBox assignedEmployee;

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

    private static SpotDrawable spot;

    private FormPopup popup;

    private List<Employee> employeeList;

    public static SpotShiftEditForm create(SpotDrawable spotData) {
        spot = spotData;
        return (SpotShiftEditForm) spotData.getCalendarView().getCalendar().getInstanceOf(SpotShiftEditForm.class);
    }

    @PostConstruct
    protected void initWidget() {
        isLocked.setValue(spot.getData().isLocked());
        title.setInnerSafeHtml(new SafeHtmlBuilder().appendEscaped(spot.getData().getSpot().getName())
                .appendEscaped(" - ")
                .appendEscaped(spot.getData().getStartTime().toLocalTime().toString())
                .appendEscaped("-")
                .appendEscaped(spot.getData().getEndTime().toLocalTime().toString())
                .toSafeHtml());
        SpotShiftEditForm form = this;
        EmployeeRestServiceBuilder.getEmployeeList(spot.getData().getShift().getTenantId(),
                new FailureShownRestCallback<List<
                        Employee>>() {

                    @Override
                    public void onSuccess(List<Employee> tenantEmployeeList) {
                        employeeList = tenantEmployeeList;
                        employeeList.forEach((e) -> assignedEmployee.addItem(e.getName()));
                        if (!spot.getData().isLocked()) {
                            assignedEmployee.setEnabled(false);
                        } else {
                            assignedEmployee.setSelectedIndex(employeeList.indexOf(spot.getData()
                                    .getAssignedEmployee()));
                        }
                        isLocked.addValueChangeHandler((v) -> assignedEmployee.setEnabled(v.getValue()));

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
    public void save(ClickEvent click) {
        if (isLocked.getValue()) {
            Employee employee = employeeList.stream().filter((e) -> e.getName().equals(
                    assignedEmployee.getSelectedValue())).findFirst().get();
            spot.getData().getShift().setLockedByUser(true);
            spot.getData().getShift().setEmployee(employee);
            ShiftView shiftView = new ShiftView(spot.getData().getShift());
            popup.hide();
            ShiftRestServiceBuilder.updateShift(spot.getData().getShift().getTenantId(), shiftView,
                    new FailureShownRestCallback<Void>() {

                        @Override
                        public void onSuccess(Void result) {
                            spot.getCalendarView().getCalendar().forceUpdate();
                        }

                    });
        } else {
            spot.getData().getShift().setLockedByUser(false);
            ShiftView shiftView = new ShiftView(spot.getData().getShift());
            popup.hide();
            ShiftRestServiceBuilder.updateShift(spot.getData().getShift().getTenantId(), shiftView,
                    new FailureShownRestCallback<Void>() {

                        @Override
                        public void onSuccess(Void result) {
                            spot.getCalendarView().getCalendar().forceUpdate();
                        }

                    });
        }

    }

}
