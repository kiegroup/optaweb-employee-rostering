package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
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
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.css.CssResources;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeSkillProficiency;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;

import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.*;

@Templated
public class EmployeeListPanel implements IsElement {

    private Integer tenantId = null;

    @Inject
    @DataField
    private Button refreshButton;

    @Inject
    @DataField
    private TextBox employeeNameTextBox;
    @Inject
    @DataField
    private SingleValueTagsInput<Skill> skillsTagsInput;
    @Inject
    @DataField
    private Button addButton;

    // TODO use DataGrid instead
    @DataField
    private CellTable<Employee> table;
    @DataField
    private Pagination pagination;

    private SimplePager pager = new SimplePager();
    private ListDataProvider<Employee> dataProvider = new ListDataProvider<>();

    @Inject
    private SyncBeanManager beanManager;
    @Inject
    private TranslationService CONSTANTS;

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

    public void onAnyTenantEvent(@Observes Tenant tenant) {
        tenantId = tenant.getId();
        refresh();
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
        if (tenantId == null) {
            return;
        }
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
        }, CONSTANTS.format(General_name));
        table.addColumn(new TextColumn<Employee>() {

            @Override
            public String getValue(Employee employee) {
                Set<EmployeeSkillProficiency> skillProficiencySet = employee.getSkillProficiencySet();
                if (skillProficiencySet == null) {
                    return "";
                }
                return skillProficiencySet.stream().map(skillProficiency -> skillProficiency.getSkill().getName())
                        .collect(Collectors.joining(", "));
            }
        }, CONSTANTS.format(General_skills));
        Column<Employee, String> deleteColumn = new Column<Employee, String>(new ButtonCell(IconType.REMOVE,
                ButtonType.DANGER, ButtonSize.SMALL)) {

            @Override
            public String getValue(Employee employee) {
                return CONSTANTS.format(General_delete);
            }
        };
        deleteColumn.setFieldUpdater((index, employee, value) -> {
            EmployeeRestServiceBuilder.removeEmployee(tenantId, employee.getId(), new FailureShownRestCallback<
                    Boolean>() {

                @Override
                public void onSuccess(Boolean removed) {
                    refreshTable();
                }
            });
        });
        Column<Employee, String> editColumn = new Column<Employee, String>(new ButtonCell(IconType.EDIT,
                ButtonType.DEFAULT, ButtonSize.SMALL)) {

            @Override
            public String getValue(Employee employee) {
                return CONSTANTS.format(General_edit);
            }
        };
        editColumn.setFieldUpdater((index, employee, value) -> {
            EmployeeListPanel employeeListPanel = this;
            SkillRestServiceBuilder.getSkillList(tenantId, new FailureShownRestCallback<List<Skill>>() {

                @Override
                public void onSuccess(List<Skill> skillList) {
                    EmployeeEditForm.create(beanManager, employeeListPanel, employee, skillList);
                }
            });
        });
        table.addColumn(deleteColumn, CONSTANTS.format(General_actions));
        table.addColumn(editColumn);

        table.addRangeChangeHandler(event -> pagination.rebuild(pager));

        pager.setDisplay(table);
        pagination.clear();
        dataProvider.addDataDisplay(table);
    }

    private void refreshTable() {
        if (tenantId == null) {
            return;
        }
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
        if (tenantId == null) {
            throw new IllegalStateException("The tenantId (" + tenantId + ") can not be null at this time.");
        }
        String employeeName = employeeNameTextBox.getValue();
        employeeNameTextBox.setValue("");
        employeeNameTextBox.setFocus(true);
        List<Skill> skillList = skillsTagsInput.getItems();
        Employee employee = new Employee(tenantId, employeeName);
        for (Skill skill : skillList) {
            employee.getSkillProficiencySet().add(new EmployeeSkillProficiency(tenantId, employee, skill));
        }
        EmployeeRestServiceBuilder.addEmployee(tenantId, employee, new FailureShownRestCallback<Employee>() {

            @Override
            public void onSuccess(Employee employee) {
                refreshTable();
            }
        });
    }

}
