package org.optaplanner.openshift.employeerostering.gwtui.client.skill;

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
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.pages.Page;
import org.optaplanner.openshift.employeerostering.gwtui.client.tenant.TenantStore;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;

import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_actions;
import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_delete;
import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_edit;
import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_name;

@Templated
public class SkillListPanel implements IsElement,
                                       Page {

    @Inject
    @DataField
    private Button refreshButton;

    @Inject
    @DataField
    private TextBox skillNameTextBox;
    @Inject
    @DataField
    private Button addButton;

    @Inject
    private SyncBeanManager beanManager;

    // TODO use DataGrid instead
    @DataField
    private CellTable<Skill> table;
    @DataField
    private Pagination pagination;

    @Inject
    private TenantStore tenantStore;

    private SimplePager pager = new SimplePager();
    private ListDataProvider<Skill> dataProvider = new ListDataProvider<>();

    @Inject
    private TranslationService CONSTANTS;

    public SkillListPanel() {
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

    @Override
    public Promise<Void> beforeOpen() {
        return refresh();
    }

    public void onTenantChanged(final @Observes TenantStore.TenantChange tenant) {
        refresh();
    }

    @EventHandler("refreshButton")
    public void refresh(ClickEvent e) {
        refresh();
    }

    public Promise<Void> refresh() {
        return refreshTable();
    }

    private void initTable() {
        table.addColumn(new TextColumn<Skill>() {

            @Override
            public String getValue(Skill skill) {
                return skill.getName();
            }
        },
                CONSTANTS.format(General_name));

        Column<Skill, String> deleteColumn = new Column<Skill, String>(new ButtonCell(IconType.REMOVE,
                ButtonType.DANGER, ButtonSize.SMALL)) {

            @Override
            public String getValue(Skill skill) {
                return CONSTANTS.format(General_delete);
            }
        };
        deleteColumn.setFieldUpdater((index, skill, value) -> {
            SkillRestServiceBuilder.removeSkill(tenantStore.getCurrentTenantId(), skill.getId(), FailureShownRestCallback.onSuccess(i -> {
                refreshTable();
            }));
        });
        Column<Skill, String> editColumn = new Column<Skill, String>(new ButtonCell(IconType.EDIT, ButtonType.DEFAULT,
                ButtonSize.SMALL)) {

            @Override
            public String getValue(Skill skill) {
                return CONSTANTS.format(General_edit);
            }
        };
        editColumn.setFieldUpdater((index, skill, value) -> {
            SkillEditForm.create(beanManager, this, skill);
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
            SkillRestServiceBuilder.getSkillList(tenantStore.getCurrentTenantId(), FailureShownRestCallback.onSuccess(skillList -> {
                dataProvider.setList(skillList);
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
        String skillName = skillNameTextBox.getValue();
        skillNameTextBox.setValue("");
        skillNameTextBox.setFocus(true);
        SkillRestServiceBuilder.addSkill(tenantStore.getCurrentTenantId(), new Skill(tenantStore.getCurrentTenantId(), skillName), FailureShownRestCallback.onSuccess(i -> {
            refreshTable();
        }));
    }
}
