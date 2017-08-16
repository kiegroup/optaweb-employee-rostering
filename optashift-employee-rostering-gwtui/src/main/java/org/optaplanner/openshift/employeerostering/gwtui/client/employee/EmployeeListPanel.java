package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.extras.tagsinput.client.ui.base.SingleValueTagsInput;
import org.gwtbootstrap3.extras.typeahead.client.base.CollectionDataset;
import org.gwtbootstrap3.extras.typeahead.client.base.Dataset;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeSkillProficiency;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;

@Templated
public class EmployeeListPanel implements IsElement {

    private Integer tenantId = 1;

    @Inject @DataField
    private Button refreshButton;

    @Inject @DataField
    private TextBox employeeNameTextBox;
    @Inject @DataField
    private SingleValueTagsInput<Skill> skillsTagsInput;
    @Inject @DataField
    private Button addButton;

    // TODO use DataGrid instead
    @DataField
    private CellTable<Employee> table;
    @DataField
    private Pagination pagination;

    private SimplePager pager = new SimplePager();
    private ListDataProvider<Employee> dataProvider = new ListDataProvider<>();

    public EmployeeListPanel() {
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
        skillsTagsInput.setItemValue(Skill::getName);
        skillsTagsInput.setItemText(Skill::getName);
        skillsTagsInput.reconfigure();
    }

    @EventHandler("refreshButton")
    public void refresh(ClickEvent e) {
        refresh();
    }

    public void refresh() {
        refreshSkillsTagsInput();
        refreshTable();
    }

    private void refreshSkillsTagsInput() {
        SkillRestServiceBuilder.getSkillList(tenantId, new FailureShownRestCallback<List<Skill>>() {
            @Override
            public void onSuccess(List<Skill> skillList) {
                skillsTagsInput.removeAll();
                skillsTagsInput.setDatasets((Dataset<Skill>) new CollectionDataset<Skill>(skillList) {
                    @Override
                    public String getValue(Skill skill) {
                        return (skill == null) ? "" : skill.getName();
                    }
                });
                skillsTagsInput.reconfigure();
            }
        });
    }

    private void initTable() {
        table.addColumn(new TextColumn<Employee>() {
            @Override
            public String getValue(Employee employee) {
                return employee.getName();
            }
        }, "Name");
        table.addColumn(new TextColumn<Employee>() {
            @Override
            public String getValue(Employee employee) {
                List<EmployeeSkillProficiency> skillProficiencyList = employee.getSkillProficiencyList();
                if (skillProficiencyList == null) {
                    return "";
                }
                return skillProficiencyList.stream().map(skillProficiency -> skillProficiency.getSkill().getName())
                        .collect(Collectors.joining(", "));
            }
        }, "Skills");
        Column<Employee, String> deleteColumn = new Column<Employee, String>(new ButtonCell(IconType.REMOVE, ButtonType.DANGER, ButtonSize.SMALL)) {
            @Override
            public String getValue(Employee employee) {
                return "Delete";
            }
        };
        deleteColumn.setFieldUpdater((index, employee, value) -> {
            EmployeeRestServiceBuilder.removeEmployee(tenantId, employee.getId(), new FailureShownRestCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean removed) {
                    refreshTable();
                }
            });
        });
        table.addColumn(deleteColumn, "Actions");

        table.addRangeChangeHandler(event -> pagination.rebuild(pager));

        pager.setDisplay(table);
        pagination.clear();
        dataProvider.addDataDisplay(table);
    }

    private void refreshTable() {
        EmployeeRestServiceBuilder.getEmployeeList(tenantId, new FailureShownRestCallback<List<Employee>>() {
            @Override
            public void onSuccess(List<Employee> employeeList) {
                dataProvider.setList(employeeList);
                dataProvider.flush();
                pagination.rebuild(pager);
            }
        });
    }

    @EventHandler("addButton")
    public void add(ClickEvent e) {
        String employeeName = employeeNameTextBox.getValue();
        employeeNameTextBox.setValue("");
        employeeNameTextBox.setFocus(true);
        List<Skill> skillList = skillsTagsInput.getItems();
        Employee employee = new Employee(tenantId, employeeName);
        for (Skill skill : skillList) {
            employee.getSkillProficiencyList().add(new EmployeeSkillProficiency(tenantId, employee, skill));
        }
        EmployeeRestServiceBuilder.addEmployee(tenantId, employee, new FailureShownRestCallback<Long>() {
            @Override
            public void onSuccess(Long employeeId) {
                refreshTable();
            }
        });
    }

}
