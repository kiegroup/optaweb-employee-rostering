package org.optaplanner.openshift.employeerostering.gwtui.client.roster;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.github.nmorel.gwtjackson.rest.api.RestCallback;
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
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.ListDataProvider;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.roster.view.SpotRosterView;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;

@Templated
public class SpotRosterViewPanel implements IsElement {

    private Integer tenantId = -1;

    @Inject @DataField
    private Button refreshButton;
    @Inject @DataField
    private Button solveButton;

    // TODO use DataGrid instead
    @DataField
    private CellTable<Spot> table;
    @DataField
    private Pagination pagination;

    private SimplePager pager = new SimplePager();
    private ListDataProvider<Spot> dataProvider = new ListDataProvider<>();

    private SpotRosterView spotRosterView;
    private Map<Long, Employee> employeeMap;

    public SpotRosterViewPanel() {
        table = new CellTable<>(10);
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
        refreshTable();
    }

    private void initTable() {
        table.addColumn(new TextColumn<Spot>() {
            @Override
            public String getValue(Spot spot) {
                return (spot == null) ? "" : spot.getName();
            }
        }, "Spot");
        table.addRangeChangeHandler(event -> pagination.rebuild(pager));

        pager.setDisplay(table);
        pagination.clear();
        dataProvider.addDataDisplay(table);
    }

    private void refreshTable() {
        RosterRestServiceBuilder.getCurrentSpotRosterView(tenantId, new RestCallback<SpotRosterView>() {
            @Override
            public void onSuccess(SpotRosterView spotRosterView) {
                SpotRosterViewPanel.this.spotRosterView = spotRosterView;
                employeeMap = spotRosterView.getEmployeeList().stream()
                        .collect(Collectors.toMap(Employee::getId, Function.identity()));
                for (int i = table.getColumnCount() - 1; i > 0; i--) {
                    table.removeColumn(i);
                }
                for (TimeSlot timeSlot : spotRosterView.getTimeSlotList()) {
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

                    table.addColumn(new IdentityColumn<>(new AbstractCell<Spot>("click") {
                        @Override
                        public void render(Context context, Spot spot, SafeHtmlBuilder sb) {
                            Long timeSlotId = timeSlot.getId();
                            Long spotId = spot.getId();
                            List<ShiftView> shiftViewList
                                    = spotRosterView.getSpotIdToTimeSlotIdToShiftViewListMap().get(spotId)
                                    .get(timeSlotId);
                            if (shiftViewList != null && !shiftViewList.isEmpty()) {
                                for (ShiftView shiftView : shiftViewList) {
                                    Long employeeId = shiftView.getEmployeeId();
                                    String employeeName;
                                    if (employeeId == null) {
                                        employeeName = null;
                                    } else {
                                        Employee employee = employeeMap.get(employeeId);
                                        if (employee == null) {
                                            throw new IllegalStateException("Impossible situation: the employeeId ("
                                                    + employeeId + ") does not exist in the employeeMap.");
                                        }
                                        employeeName = employee.getName();
                                    }
                                    sb.appendHtmlConstant("<span class=\"badge");
                                    if (employeeName == null) {
                                        sb.appendHtmlConstant(" badge-important");
                                    }
                                    sb.appendHtmlConstant("\">");
                                    sb.appendEscaped(employeeName == null ? "Unassigned" : employeeName);
                                    sb.appendHtmlConstant("<a class=\"btn btn-sm shiftRemove\" style=\"padding: 5px\" aria-label=\"Remove shift\">" +
                                            "<span class=\"glyphicon glyphicon-remove\" aria-hidden=\"true\"/>" +
                                            "</a>");
                                    sb.appendHtmlConstant("</span>");
                                }
                            }
                            sb.appendHtmlConstant("<a class=\"btn btn-sm shiftAdd\" aria-label=\"Add shift\">" +
                                    "<span class=\"glyphicon glyphicon-plus\" aria-hidden=\"true\"/>" +
                                    "</a>");
                        }

                        @Override
                        public void onBrowserEvent(Context context, Element parent, Spot spot, NativeEvent event, ValueUpdater<Spot> valueUpdater) {
                            super.onBrowserEvent(context, parent, spot, event, valueUpdater);
                            if ("click".equals(event.getType())) {
                                Element targetElement = Element.as(event.getEventTarget());
                                if (targetElement.hasClassName("shiftRemove") || targetElement.getParentElement().hasClassName("shiftRemove")) {
                                    Window.alert("Remove shift " + spot + " " + timeSlot); // TODO
                                }
                                if (targetElement.hasClassName("shiftAdd") || targetElement.getParentElement().hasClassName("shiftAdd")) {
                                    Window.alert("Add shift " + spot + " " + timeSlot); // TODO
                                }
                            }
                        }
                    }), headerHtml);
                }
                dataProvider.setList(spotRosterView.getSpotList());
                dataProvider.flush();
                pagination.rebuild(pager);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Window.alert("Failure calling REST method: " + throwable.getMessage());
                throw new IllegalStateException("REST call failure", throwable);
            }
        });
    }

    @EventHandler("refreshButton")
    public void refresh(ClickEvent e) {
        refreshTable();
    }

    @EventHandler("solveButton")
    public void solve(ClickEvent e) {
        RosterRestServiceBuilder.solveRoster(tenantId).send();
    }

}
