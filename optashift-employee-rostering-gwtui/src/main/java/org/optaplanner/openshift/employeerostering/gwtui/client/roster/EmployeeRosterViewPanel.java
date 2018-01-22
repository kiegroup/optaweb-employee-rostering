package org.optaplanner.openshift.employeerostering.gwtui.client.roster;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.view.client.ListDataProvider;
import elemental2.dom.HTMLCanvasElement;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Calendar;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.employee.EmployeeData;
import org.optaplanner.openshift.employeerostering.gwtui.client.employee.EmployeeDataFetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.employee.EmployeeDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.employee.EmployeeId;
import org.optaplanner.openshift.employeerostering.gwtui.client.employee.EmployeeNameFetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.view.EmployeeRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.*;

@Templated
public class EmployeeRosterViewPanel extends AbstractRosterViewPanel {

    @Inject
    @DataField
    private Button planNextPeriod;

    @Inject
    @DataField
    private Div container;

    private Calendar<EmployeeId, EmployeeData> calendar;

    private EmployeeRosterView employeeRosterView;
    private Map<Long, Spot> spotMap;

    boolean isDateSet = false;

    @Inject
    private SyncBeanManager beanManager;

    @Inject
    private TranslationService CONSTANTS;

    @EventHandler("planNextPeriod")
    public void plan(ClickEvent e) {
        ShiftRestServiceBuilder.addShiftsFromTemplate(tenantId,
                calendar.getShiftSet().stream().max((a, b) -> a.getStartTime().compareTo(b
                        .getStartTime())).get().getEndTime().toString(),
                calendar.getShiftSet().stream().max((a, b) -> a.getStartTime().compareTo(b
                        .getStartTime())).get().getEndTime().plusWeeks(2).toString(),
                new FailureShownRestCallback<List<Long>>() {

                    public void onSuccess(List<Long> lst) {
                    }
                });
    }

    public EmployeeRosterViewPanel() {
    }

    @PostConstruct
    protected void initWidget() {
        super.init();
        initTable();
    }

    private void initTable() {
        calendar = new Calendar.Builder<EmployeeId, EmployeeData, EmployeeDrawable<EmployeeData>>(container, tenantId,
                CONSTANTS)
                        .fetchingGroupsFrom(new EmployeeNameFetchable(() -> getTenantId()))
                        .withBeanManager(beanManager)
                        .asTwoDayView((v, d, i) -> new EmployeeDrawable<>(v, d, i));

        calendar.setDataProvider(new EmployeeDataFetchable(calendar, () -> getTenantId()));
        Window.addResizeHandler((e) -> calendar.setViewSize(e.getWidth() - container.getAbsoluteLeft(),
                e.getHeight() - container.getAbsoluteTop()));
    }

    @Override
    protected void refreshTable() {
        calendar.setViewSize(Window.getClientWidth() - container.getAbsoluteLeft(),
                Window.getClientHeight() - container.getAbsoluteTop());
        if (tenantId == null) {
            return;
        }
        if (!isDateSet) {
            RosterRestServiceBuilder.getCurrentEmployeeRosterView(tenantId, new FailureShownRestCallback<
                    EmployeeRosterView>() {

                @Override
                public void onSuccess(EmployeeRosterView employeeRosterView) {
                    isDateSet = true;
                    calendar.setDate(employeeRosterView.getTimeSlotList().stream().min((a, b) -> a.getStartDateTime()
                            .compareTo(b.getStartDateTime())).get().getStartDateTime());
                }
            });
        }
        calendar.forceUpdate();
    }

    private Integer getTenantId() {
        return tenantId;
    }

}
