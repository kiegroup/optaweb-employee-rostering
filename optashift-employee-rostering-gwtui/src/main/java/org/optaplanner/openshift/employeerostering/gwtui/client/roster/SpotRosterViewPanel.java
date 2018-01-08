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
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.view.client.ListDataProvider;
import elemental2.dom.HTMLCanvasElement;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.Calendar;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.employee.EmployeeData;
import org.optaplanner.openshift.employeerostering.gwtui.client.employee.EmployeeDataFetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.employee.EmployeeDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.employee.EmployeeNameFetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotData;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotDataFetchable;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotDrawable;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotId;
import org.optaplanner.openshift.employeerostering.gwtui.client.spot.SpotNameFetchable;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.view.EmployeeRosterView;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.*;

@Templated
public class SpotRosterViewPanel extends AbstractRosterViewPanel {

    private SpotRosterView spotRosterView;
    private Map<Long, Employee> employeeMap;

    @Inject
    @DataField
    private Div container;

    private Calendar<SpotId, SpotData> calendar;

    @Inject
    private SyncBeanManager beanManager;

    @Inject
    private TranslationService CONSTANTS;

    boolean isDateSet = false;

    public SpotRosterViewPanel() {
    }

    @PostConstruct
    protected void initWidget() {
        super.init();
        initTable();
    }

    private void initTable() {
        calendar = new Calendar.Builder<SpotId, SpotData, SpotDrawable<SpotData>>(container, tenantId, CONSTANTS)
                .fetchingGroupsFrom(new SpotNameFetchable(() -> getTenantId()))
                .withBeanManager(beanManager)
                .asTwoDayView((v, d, i) -> new SpotDrawable<>(v, d, i));
        calendar.setDataProvider(new SpotDataFetchable(calendar, () -> getTenantId()));
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
            RosterRestServiceBuilder.getCurrentSpotRosterView(tenantId, new FailureShownRestCallback<SpotRosterView>() {

                @Override
                public void onSuccess(SpotRosterView spotRosterView) {
                    isDateSet = true;
                    calendar.setDate(spotRosterView.getTimeSlotList().stream().min((a, b) -> a.getStartDateTime()
                            .compareTo(b.getStartDateTime())).get().getStartDateTime());
                }
            });
        }
        calendar.forceUpdate();
    }

    private int getTenantId() {
        return tenantId;
    }

}
