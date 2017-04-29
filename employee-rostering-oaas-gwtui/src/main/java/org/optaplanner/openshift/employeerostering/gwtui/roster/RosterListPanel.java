package org.optaplanner.openshift.employeerostering.gwtui.roster;

import java.util.HashSet;
import java.util.List;

import com.github.nmorel.gwtjackson.rest.api.RestCallback;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.RangeChangeEvent;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.DataGrid;
import org.optaplanner.openshift.employeerostering.shared.domain.Employee;
import org.optaplanner.openshift.employeerostering.shared.domain.Roster;
import org.optaplanner.openshift.employeerostering.shared.domain.Skill;
import org.optaplanner.openshift.employeerostering.shared.rest.RosterServiceBuilder;

public class RosterListPanel extends Composite implements ProvidesResize {

    interface RosterListUiBinder extends UiBinder<Widget, RosterListPanel> {}
    private static final RosterListUiBinder uiBinder = GWT.create(RosterListUiBinder.class);

    @UiField
    protected ListBox listBox;

    @UiField(provided = true)
    DataGrid<Employee> rosterTable = new DataGrid<>(10);
    @UiField
    Pagination rosterPagination;

    private SimplePager rosterPager = new SimplePager();
    private ListDataProvider<Employee> rosterProvider = new ListDataProvider<>();

    public RosterListPanel() {
        // sets listBox
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    protected void initWidget(Widget widget) {
        super.initWidget(widget);
        initRosterTable();
rosterProvider.getList().add(new Employee("Foo", new HashSet<>()));
rosterProvider.flush();
rosterPagination.rebuild(rosterPager);
        RosterServiceBuilder.getRosterList(new RestCallback<List<Roster>>() {
            @Override
            public void onSuccess(List<Roster> rosterList) {
                for (Roster roster : rosterList) {
                    listBox.addItem(roster.getEmployeeList().size() + " employees"); // TODO
                }
                if (!rosterList.isEmpty()) {
                    Roster roster = rosterList.get(0);
                    rosterProvider.getList().addAll(roster.getEmployeeList());
                    rosterProvider.flush();
                    rosterPagination.rebuild(rosterPager);
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                listBox.addItem("ERROR " + throwable.getMessage());
                throw new IllegalStateException("REST call failure", throwable);
            }
        });
    }

    private void initRosterTable() {
        rosterTable.addColumn(new TextColumn<Employee>() {
            @Override
            public String getValue(Employee employee) {
                return String.valueOf(employee.getName());
            }
        }, "Name");
        rosterTable.addColumn(new TextColumn<Employee>() {
            @Override
            public String getValue(Employee employee) {
                return String.valueOf(employee.getSkillSet().size());
            }
        }, "# skills");

//        final Column<Employee, String> col4 = new Column<Employee, String>(new ButtonCell(ButtonType.PRIMARY, IconType.GITHUB)) {
//            @Override
//            public String getValue(Employee object) {
//                return "Click Me";
//            }
//        };
//        col4.setFieldUpdater((index, object, value) -> Window.alert("Clicked!"));
//        rosterTable.addColumn(col4, "Buttons");

        rosterTable.addRangeChangeHandler(event -> rosterPagination.rebuild(rosterPager));

        rosterPager.setDisplay(rosterTable);
        rosterPagination.clear();
        rosterProvider.addDataDisplay(rosterTable);
    }

}
