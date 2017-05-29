package org.optaplanner.openshift.employeerostering.gwtui.client.roster;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.github.nmorel.gwtjackson.rest.api.RestCallback;
import com.google.gwt.event.dom.client.ClickEvent;
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
import org.optaplanner.openshift.employeerostering.shared.shift.ShiftAssignment;
import org.optaplanner.openshift.employeerostering.shared.timeslot.TimeSlot;
import org.optaplanner.openshift.employeerostering.shared.roster.Roster;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;

@Templated
public class RosterListPanel implements IsElement {

    private Integer tenantId = -1;

    @Inject @DataField
    private Button refreshButton;
    @Inject @DataField
    private Button solveButton;

    // TODO use DataGrid instead
    @DataField
    private CellTable<Employee> table;
    @DataField
    private Pagination pagination;

    private SimplePager pager = new SimplePager();
    private ListDataProvider<Employee> dataProvider = new ListDataProvider<>();

    private List<TimeSlot> timeSlotList;
    private Map<Long, Map<Long, List<ShiftAssignment>>> timeSlotIdToEmployeeIdToShiftAssignmentMap;

    public RosterListPanel() {
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
        table.addColumn(new TextColumn<Employee>() {
            @Override
            public String getValue(Employee employee) {
                return employee.getName();
            }
        }, "Employee");
        for (int i = 0; i < 10; i++) {
            int timeSlotIndex = i;
            table.addColumn(new TextColumn<Employee>() {
                @Override
                public String getValue(Employee employee) {
                    if (timeSlotList == null || timeSlotIndex >= timeSlotList.size()) {
                        return "No timeslot";
                    }
                    TimeSlot timeSlot = timeSlotList.get(timeSlotIndex);
                    List<ShiftAssignment> shiftAssignmentList = timeSlotIdToEmployeeIdToShiftAssignmentMap
                            .get(timeSlot.getId()).get(employee.getId());
                    if (shiftAssignmentList == null) {
                        return "Free";
                    }
                    return shiftAssignmentList.stream().map(shiftAssignment -> shiftAssignment.getSpot().getName())
                            .collect(Collectors.joining(", "));
                }
            }, "Timeslot " + timeSlotIndex);
        }
        table.addRangeChangeHandler(event -> pagination.rebuild(pager));

        pager.setDisplay(table);
        pagination.clear();
        dataProvider.addDataDisplay(table);
    }

    private void refreshTable() {
        RosterRestServiceBuilder.getRoster(tenantId, new RestCallback<Roster>() {
            @Override
            public void onSuccess(Roster roster) {
                List<Employee> employeeList = roster.getEmployeeList();
                timeSlotList = roster.getTimeSlotList();
                timeSlotIdToEmployeeIdToShiftAssignmentMap = new LinkedHashMap<>(timeSlotList.size());
                for (TimeSlot timeSlot : timeSlotList) {
                    timeSlotIdToEmployeeIdToShiftAssignmentMap.put(timeSlot.getId(),
                            new LinkedHashMap<>(employeeList.size()));
                }
                for (ShiftAssignment shiftAssignment : roster.getShiftAssignmentList()) {
                    TimeSlot timeSlot = shiftAssignment.getTimeSlot();
                    Map<Long, List<ShiftAssignment>> subMap = timeSlotIdToEmployeeIdToShiftAssignmentMap.get(timeSlot.getId());
                    Employee employee = shiftAssignment.getEmployee();
                    if (employee != null) {
                        List<ShiftAssignment> shiftAssignmentList = subMap.get(employee.getId());
                        // GWT does not support computeIfAbsent
                        if (shiftAssignmentList == null) {
                            shiftAssignmentList = new ArrayList<>(2);
                            subMap.put(employee.getId(), shiftAssignmentList);
                        }
                        shiftAssignmentList.add(shiftAssignment);
                    }
                }
                dataProvider.setList(roster.getEmployeeList());
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
