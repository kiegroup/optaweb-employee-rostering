package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;
import elemental2.promise.Promise;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.extras.tagsinput.client.ui.base.SingleValueTagsInput;
import org.gwtbootstrap3.extras.typeahead.client.base.CollectionDataset;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;

import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_actions;
import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_delete;
import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_edit;
import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_name;
import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_skills;

@Templated
public class EmployeeListPanel implements IsElement,
                                          Page {

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
    private TenantStore tenantStore;

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

    @Override
    public Promise<Void> beforeOpen() {
        return refresh();
    }

    public void onAnyTenantEvent(@Observes TenantStore.TenantChange tenant) {
        refresh();
    }

    @EventHandler("refreshButton")
    public void refresh(ClickEvent e) {
        refresh();
    }

    public Promise<Void> refresh() {
        return refreshSkillsTagsInput().then(i -> refreshTable());
    }

    private Promise<Void> refreshSkillsTagsInput() {

        if (tenantStore.getCurrentTenantId() == null) {
            return PromiseUtils.resolve();
        }

        return new Promise<>((res, rej) -> {
            SkillRestServiceBuilder.getSkillList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(skillList -> {
                skillsTagsInput.removeAll();
                skillsTagsInput.setDatasets(new CollectionDataset<Skill>(skillList) {

                    @Override
                    public String getValue(Skill skill) {
                        return (skill == null) ? "" : skill.getName();
                    }
                });
                skillsTagsInput.reconfigure();
                res.onInvoke(PromiseUtils.resolve());
            }));
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
                Set<Skill> skillProficiencySet = employee.getSkillProficiencySet();
                if (skillProficiencySet == null) {
                    return "";
                }
                return skillProficiencySet.stream().map(skillProficiency -> skillProficiency.getName())
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
            EmployeeRestServiceBuilder.removeEmployee(tenantStore.getCurrentTenantId(), employee.getId(), FailureShownRestCallback.onSuccess(i -> {
                refreshTable();
            }));
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
            SkillRestServiceBuilder.getSkillList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(skillList -> {
                EmployeeEditForm.create(beanManager, employeeListPanel, employee, skillList);
            }));
        });
        table.addColumn(deleteColumn, CONSTANTS.format(General_actions));
        table.addColumn(editColumn);

        table.addRangeChangeHandler(event -> pagination.rebuild(pager));

        pager.setDisplay(table);
        pagination.clear();
        dataProvider.addDataDisplay(table);
    }

    private Promise<Void> refreshTable() {
        if (tenantStore.getCurrentTenantId() == null) {
            return PromiseUtils.resolve();
        }
        return new Promise<>((res, rej) -> {
            EmployeeRestServiceBuilder.getEmployeeList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(employeeList -> {
                dataProvider.setList(employeeList);
                dataProvider.flush();
                pagination.rebuild(pager);
                res.onInvoke(PromiseUtils.resolve());
            }));
        });
    }

    @EventHandler("addButton")
    public void add(ClickEvent e) {
        if (tenantStore.getCurrentTenantId() == null) {
            throw new IllegalStateException("The tenantStore.getTenantId() (" + tenantStore.getCurrentTenantId() + ") can not be null at this time.");
        }
        String employeeName = employeeNameTextBox.getValue();
        employeeNameTextBox.setValue("");
        employeeNameTextBox.setFocus(true);
        List<Skill> skillList = skillsTagsInput.getItems();
        Employee employee = new Employee(tenantStore.getCurrentTenantId(), employeeName);
        employee.setSkillProficiencySet(skillList.stream().collect(Collectors.toSet()));
        EmployeeRestServiceBuilder.addEmployee(tenantStore.getCurrentTenantId(), employee, FailureShownRestCallback.onSuccess(i -> {
            refreshTable();
        }));
    }
}
