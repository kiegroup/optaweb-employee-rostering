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
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.view.client.ListDataProvider;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
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

    // TODO use DataGrid instead
    @DataField
    private CellTable<Employee> table;
    @DataField
    private Pagination pagination;
    
    @Inject @DataField
    private Button planNextPeriod;

    private SimplePager pager = new SimplePager();
    private ListDataProvider<Employee> dataProvider = new ListDataProvider<>();

    private EmployeeRosterView employeeRosterView;
    private Map<Long, Spot> spotMap;

    @Inject
    private TranslationService CONSTANTS;
    
        @EventHandler("planNextPeriod")
        public void plan(ClickEvent e) {
            ShiftRestServiceBuilder.addShiftsFromTemplate(tenantId,
                    employeeRosterView.getTimeSlotList().stream().max((a,b) -> a.getStartDateTime().compareTo(b.getStartDateTime())).get().getEndDateTime().toString(),
                    employeeRosterView.getTimeSlotList().stream().max((a,b) -> a.getStartDateTime().compareTo(b.getStartDateTime())).get().getEndDateTime().plusWeeks(2).toString(),
                    "NONE\n" +
                    ";\n" +
                    "0:0:0;0:8:0;Battery",
                        new FailureShownRestCallback<List<Long>>() {
                            public void onSuccess(List<Long> lst) {
                                StringBuilder out = new StringBuilder();
                                out.append("Start Date: ");
                                out.append(employeeRosterView.getStartDate().toString());
                                out.append("\nEnd Date: ");
                                out.append(employeeRosterView.getEndDate().toString());
                                out.append('\n');
                                out.append('[');
                                for (Long id : lst) {
                                    out.append(id);
                                    out.append(',');
                                }
                                out.append(']');
                                ErrorPopup.show(out.toString());
                            }
                        }
                    );
        }

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
        super.init();
        initTable();
    }

    private void initTable() {
        table.addColumn(new TextColumn<Employee>() {
            @Override
            public String getValue(Employee employee) {
                return (employee == null) ? "" : employee.getName();
            }
        },
                        CONSTANTS.format(General_employee));
        table.addRangeChangeHandler(event -> pagination.rebuild(pager));

        pager.setDisplay(table);
        pagination.clear();
        dataProvider.addDataDisplay(table);
    }

    @Override
    protected void refreshTable() {
        if (tenantId == null) {
            return;
        }
        RosterRestServiceBuilder.getCurrentEmployeeRosterView(tenantId, new FailureShownRestCallback<EmployeeRosterView>() {
            @Override
            public void onSuccess(EmployeeRosterView employeeRosterView) {
                EmployeeRosterViewPanel.this.employeeRosterView = employeeRosterView;
                if (null == employeeRosterView) {
                    throw new IllegalStateException(CONSTANTS.format(AbstractRosterViewPanel_emptyResult));
                }
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
                                    "<button type=\"button\" class=\"btn btn-xs btn-default timeSlotUnavailable");
                            if (state == EmployeeAvailabilityState.UNAVAILABLE) {
                                sb.appendHtmlConstant(" active");
                            }
                            sb.appendHtmlConstant("\" aria-label=\"Unavailable\">" +
                                    "<span class=\"glyphicon glyphicon-ban-circle\" aria-hidden=\"true\"/></button>" +
                                    "<button type=\"button\" class=\"btn btn-xs btn-default timeSlotUndesired");
                            if (state == EmployeeAvailabilityState.UNDESIRED) {
                                sb.appendHtmlConstant(" active");
                            }
                            sb.appendHtmlConstant("\" aria-label=\"Undesired\">" +
                                    "<span class=\"glyphicon glyphicon-remove-circle\" aria-hidden=\"true\"/></button>" +
                                    "<button type=\"button\" class=\"btn btn-xs btn-default timeSlotDesired");
                            if (state == EmployeeAvailabilityState.DESIRED) {
                                sb.appendHtmlConstant(" active");
                            }
                            sb.appendHtmlConstant("\" aria-label=\"Desired\">" +
                                    "<span class=\"glyphicon glyphicon-ok-circle\" aria-hidden=\"true\"/></button>" +
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
                                if (targetElement.hasClassName("glyphicon")) {
                                    targetElement = targetElement.getParentElement();
                                }
                                if (targetElement.getParentElement().hasClassName("timeSlotAvailability")) {
                                    EmployeeAvailabilityState newState;
                                    if (targetElement.hasClassName("timeSlotUnavailable")) {
                                        newState = EmployeeAvailabilityState.UNAVAILABLE;
                                    } else if (targetElement.hasClassName("timeSlotUndesired")) {
                                        newState = EmployeeAvailabilityState.UNDESIRED;
                                    } else if (targetElement.hasClassName("timeSlotDesired")) {
                                        newState = EmployeeAvailabilityState.DESIRED;
                                    } else {
                                        throw new IllegalStateException("The targetElement's className (" + targetElement.getClassName()
                                                + ") is not recognized as an employeeAvailabilityState.");
                                    }
                                    EmployeeAvailabilityView availabilityView = (employeeIdToAvailabilityViewMap == null)
                                            ? null : employeeIdToAvailabilityViewMap.get(employee.getId());
                                    if (availabilityView == null) {
                                        EmployeeRestServiceBuilder.addEmployeeAvailability(tenantId, new EmployeeAvailabilityView(tenantId, employee, timeSlot, newState), new FailureShownRestCallback<Long>() {
                                            @Override
                                            public void onSuccess(Long shiftId) {
                                                refreshTable();
                                            }
                                        });
                                    } else {
                                        if (availabilityView.getState() == newState) {
                                            EmployeeRestServiceBuilder.removeEmployeeAvailability(tenantId, availabilityView.getId(), new FailureShownRestCallback<Boolean>() {
                                                @Override
                                                public void onSuccess(Boolean success) {
                                                    refreshTable();
                                                }
                                            });
                                        } else {
                                            availabilityView.setState(newState);
                                            EmployeeRestServiceBuilder.updateEmployeeAvailability(tenantId, availabilityView, new FailureShownRestCallback<Void>() {
                                                @Override
                                                public void onSuccess(Void result) {
                                                    refreshTable();
                                                }
                                            });
                                        }
                                    }
                                }
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

}
