package org.optaplanner.openshift.employeerostering.gwtui.client.roster;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.github.nmorel.gwtjackson.rest.api.RestCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.shared.domain.Employee;
import org.optaplanner.openshift.employeerostering.shared.domain.ShiftAssignment;
import org.optaplanner.openshift.employeerostering.shared.domain.TimeSlot;
import org.optaplanner.openshift.employeerostering.shared.roster.Roster;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

@Templated
public class RosterListPanel implements IsElement {

    private Long tenantId = -1L;

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
    private Map<TimeSlot, Map<Employee, List<ShiftAssignment>>> shiftAssignmentMap;

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
                    List<ShiftAssignment> shiftAssignmentList = shiftAssignmentMap
                            .get(timeSlot).get(employee);
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
                shiftAssignmentMap = new LinkedHashMap<>(timeSlotList.size());
                timeSlotList.forEach(timeSlot -> shiftAssignmentMap.put(
                        timeSlot, new LinkedHashMap<>(employeeList.size())));
                for (ShiftAssignment shiftAssignment : roster.getShiftAssignmentList()) {
                    Map<Employee, List<ShiftAssignment>> subMap = shiftAssignmentMap.get(shiftAssignment.getTimeSlot());
                    Employee employee = shiftAssignment.getEmployee();
                    if (employee != null) {
                        // GWT does not support computeIfAbsent
                        List<ShiftAssignment> shiftAssignmentList = subMap.get(employee);
                        if (shiftAssignmentList == null) {
                            shiftAssignmentList = new ArrayList<>(2);
                            subMap.put(employee, shiftAssignmentList);
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
