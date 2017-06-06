package org.optaplanner.openshift.employeerostering.gwtui.client.roster;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.view.EmployeeRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

@Templated
public class EmployeeRosterViewPanel implements IsElement {

    private Integer tenantId = -1;

    @Inject @DataField
    private Button solveButton;
    @Inject @DataField
    private Button refreshButton;

    // TODO use DataGrid instead
    @DataField
    private CellTable<Employee> table;
    @DataField
    private Pagination pagination;

    private SimplePager pager = new SimplePager();
    private ListDataProvider<Employee> dataProvider = new ListDataProvider<>();

    private EmployeeRosterView employeeRosterView;
    private Map<Long, Spot> spotMap;

    public EmployeeRosterViewPanel() {
        table = new CellTable<>(15);
        table.setBordered(true);
        table.setCondensed(true);
        table.setStriped(true);
        table.setHover(true);
        table.setHeight("100%");
        table.setWidth("100%");
        pagination = new Pagination();
    }

    @PostConstruct
    protected void initWidget() {
        initTable();
    }

    @EventHandler("refreshButton")
    public void refresh(ClickEvent e) {
        refresh();
    }

    public void refresh() {
        refreshTable();
    }

    private void initTable() {
        table.addColumn(new TextColumn<Employee>() {
            @Override
            public String getValue(Employee employee) {
                return (employee == null) ? "" : employee.getName();
            }
        }, "Employee");
        table.addRangeChangeHandler(event -> pagination.rebuild(pager));

        pager.setDisplay(table);
        pagination.clear();
        dataProvider.addDataDisplay(table);
    }

    private void refreshTable() {
        RosterRestServiceBuilder.getCurrentEmployeeRosterView(tenantId, new FailureShownRestCallback<EmployeeRosterView>() {
            @Override
            public void onSuccess(EmployeeRosterView employeeRosterView) {
                EmployeeRosterViewPanel.this.employeeRosterView = employeeRosterView;
                spotMap = employeeRosterView.getSpotList().stream()
                        .collect(Collectors.toMap(Spot::getId, Function.identity()));
                for (int i = table.getColumnCount() - 1; i > 0; i--) {
                    table.removeColumn(i);
                }
                for (TimeSlot timeSlot : employeeRosterView.getTimeSlotList()) {
                    SafeHtml headerHtml = new SafeHtmlBuilder()
                            .appendHtmlConstant("<div>")
                            .appendEscaped(timeSlot.getStartDateTime().getDayOfWeek().toString().toLowerCase())
                            .appendHtmlConstant("<br/>")
                            .appendEscaped(timeSlot.getStartDateTime().toLocalDate().toString())
                            .appendHtmlConstant("<br/>")
                            .appendEscaped(timeSlot.getStartDateTime().toLocalTime().toString())
                            .appendEscaped("-")
                            .appendEscaped(timeSlot.getEndDateTime().toLocalTime().toString())
                            .appendHtmlConstant("</div>")
                            .toSafeHtml();

                    Map<Long, List<ShiftView>> employeeIdToShiftViewListMap
                            = employeeRosterView.getTimeSlotIdToEmployeeIdToShiftViewListMap().get(timeSlot.getId());
                    Map<Long, EmployeeAvailabilityView> employeeIdToAvailabilityViewMap
                            = employeeRosterView.getTimeSlotIdToEmployeeIdToAvailabilityViewMap().get(timeSlot.getId());
                    table.addColumn(new IdentityColumn<>(new AbstractCell<Employee>("click") {
                        @Override
                        public void render(Context context, Employee employee, SafeHtmlBuilder sb) {
                            EmployeeAvailabilityView availabilityView = (employeeIdToAvailabilityViewMap == null)
                                    ? null : employeeIdToAvailabilityViewMap.get(employee.getId());
                            EmployeeAvailabilityState state = (availabilityView == null) ? null : availabilityView.getState();
                            sb.appendHtmlConstant("<div class=\"btn-group timeSlotAvailability\" role=\"group\" aria-label=\"availability\">" +
                                    "<button type=\"button\" class=\"btn btn-xs btn-default");
                            if (state == EmployeeAvailabilityState.UNAVAILABLE) {
                                sb.appendHtmlConstant(" active");
                            }
                            sb.appendHtmlConstant("\" aria-label=\"Unavailable\">" +
                                    "<span class=\"glyphicon glyphicon-ban-circle timeSlotUnavailable\" aria-hidden=\"true\"/></button>" +
                                    "<button type=\"button\" class=\"btn btn-xs btn-default");
                            if (state == EmployeeAvailabilityState.UNDESIRED) {
                                sb.appendHtmlConstant(" active");
                            }
                            sb.appendHtmlConstant("\" aria-label=\"Undesired\">" +
                                    "<span class=\"glyphicon glyphicon-remove-circle timeSlotUndesired\" aria-hidden=\"true\"/></button>" +
                                    "<button type=\"button\" class=\"btn btn-xs btn-default");
                            if (state == EmployeeAvailabilityState.DESIRED) {
                                sb.appendHtmlConstant(" active");
                            }
                            sb.appendHtmlConstant("\" aria-label=\"Desired\">" +
                                    "<span class=\"glyphicon glyphicon-ok-circle timeSlotDesired\" aria-hidden=\"true\"/></button>" +
                                    "</div>");
                            List<ShiftView> shiftViewList = (employeeIdToShiftViewListMap == null)
                                    ? null : employeeIdToShiftViewListMap.get(employee.getId());
                            if (shiftViewList != null && !shiftViewList.isEmpty()) {
                                String labelType;
                                if (state == null) {
                                    labelType = "label-default";
                                } else switch (state) {
                                    case UNAVAILABLE:
                                        labelType = "label-danger";
                                        break;
                                    case UNDESIRED:
                                        labelType = "label-warning";
                                        break;
                                    case DESIRED:
                                        labelType = "label-success";
                                        break;
                                    default:
                                        throw new IllegalStateException(
                                                "The employeeAvailabilityState (" + state + ") is not implemented.");
                                }
                                for (ShiftView shiftView : shiftViewList) {
                                    Long spotId = shiftView.getSpotId();
                                    String spotName;
                                    if (spotId == null) {
                                        spotName = null;
                                    } else {
                                        Spot spot = spotMap.get(spotId);
                                        if (spot == null) {
                                            throw new IllegalStateException("Impossible situation: the spotId ("
                                                    + spotId + ") does not exist in the spotMap.");
                                        }
                                        spotName = spot.getName();
                                    }
                                    sb.appendHtmlConstant("<span class=\"label ");
                                    sb.appendHtmlConstant(labelType);
                                    sb.appendHtmlConstant("\">");
                                    sb.appendEscaped(spotName);
                                    sb.appendHtmlConstant("</span>");
                                }
                            }
                        }

                        @Override
                        public void onBrowserEvent(Context context, Element parent, Employee employee, NativeEvent event, ValueUpdater<Employee> valueUpdater) {
                            super.onBrowserEvent(context, parent, employee, event, valueUpdater);
                            if ("click".equals(event.getType())) {
                                Element targetElement = Element.as(event.getEventTarget());
                            }
                        }
                    }), headerHtml);
                }
                dataProvider.setList(employeeRosterView.getEmployeeList());
                dataProvider.flush();
                pagination.rebuild(pager);
            }
        });
    }

    @EventHandler("solveButton")
    public void solve(ClickEvent e) {
        RosterRestServiceBuilder.solveRoster(tenantId).send();
    }

}
