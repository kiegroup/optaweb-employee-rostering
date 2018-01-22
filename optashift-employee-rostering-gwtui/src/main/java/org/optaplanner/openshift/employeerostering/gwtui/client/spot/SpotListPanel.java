package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
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
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;

import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_actions;
import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_delete;
import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_edit;
import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.General_name;

@Templated
public class SpotListPanel implements IsElement {

    private Integer tenantId = null;

    @Inject
    @DataField
    private Button refreshButton;

    @Inject
    @DataField
    private TextBox spotNameTextBox;
    @Inject
    @DataField
    private SingleValueTagsInput<Skill> requiredSkillsTagsInput;
    @Inject
    @DataField
    private Button addButton;

    // TODO use DataGrid instead
    @DataField
    private CellTable<Spot> table;
    @DataField
    private Pagination pagination;

    private SimplePager pager = new SimplePager();
    private ListDataProvider<Spot> dataProvider = new ListDataProvider<>();

    @Inject
    private SyncBeanManager beanManager;
    @Inject
    private TranslationService CONSTANTS;

    public SpotListPanel() {
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
        requiredSkillsTagsInput.setItemValue(Skill::getName);
        requiredSkillsTagsInput.setItemText(Skill::getName);
        requiredSkillsTagsInput.reconfigure();
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
        refreshRequiredSkillListBox();
        refreshTable();
    }

    private void refreshRequiredSkillListBox() {
        if (tenantId == null) {
            return;
        }
        SkillRestServiceBuilder.getSkillList(tenantId, new FailureShownRestCallback<List<Skill>>() {

            @Override
            public void onSuccess(List<Skill> skillList) {
                requiredSkillsTagsInput.removeAll();
                requiredSkillsTagsInput.setDatasets((Dataset<Skill>) new CollectionDataset<Skill>(skillList) {

                    @Override
                    public String getValue(Skill skill) {
                        return (skill == null) ? "" : skill.getName();
                    }
                });
                requiredSkillsTagsInput.reconfigure();
            }
        });
    }

    private void initTable() {
        table.addColumn(new TextColumn<Spot>() {

            @Override
            public String getValue(Spot spot) {
                return spot.getName();
            }
        },
                CONSTANTS.format(General_name));
        table.addColumn(new TextColumn<Spot>() {

            @Override
            public String getValue(Spot spot) {
                Set<Skill> requiredSkillSet = spot.getRequiredSkillSet();
                if (requiredSkillSet == null) {
                    return "";
                }
                return requiredSkillSet.stream().reduce("", (a, s) -> (a.isEmpty()) ? s.getName() : a + "," + s
                        .getName(),
                        (a, s) -> (a.isEmpty()) ? s : a + "," + s);
            }
        }, "Required skill");
        Column<Spot, String> deleteColumn = new Column<Spot, String>(new ButtonCell(IconType.REMOVE, ButtonType.DANGER,
                ButtonSize.SMALL)) {

            @Override
            public String getValue(Spot spot) {
                return CONSTANTS.format(General_delete);
            }
        };
        deleteColumn.setFieldUpdater((index, spot, value) -> {
            SpotRestServiceBuilder.removeSpot(tenantId, spot.getId(), new FailureShownRestCallback<Boolean>() {

                @Override
                public void onSuccess(Boolean removed) {
                    refreshTable();
                }
            });
        });
        Column<Spot, String> editColumn = new Column<Spot, String>(new ButtonCell(IconType.EDIT, ButtonType.DEFAULT,
                ButtonSize.SMALL)) {

            @Override
            public String getValue(Spot spot) {
                return CONSTANTS.format(General_edit);
            }
        };
        editColumn.setFieldUpdater((index, spot, value) -> {
            final SpotListPanel panel = this;
            SkillRestServiceBuilder.getSkillList(tenantId, new FailureShownRestCallback<List<Skill>>() {

                @Override
                public void onSuccess(List<Skill> skillList) {
                    SpotEditForm.create(beanManager, panel, spot, skillList);
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
        SpotRestServiceBuilder.getSpotList(tenantId, new FailureShownRestCallback<List<Spot>>() {

            @Override
            public void onSuccess(List<Spot> spotList) {
                dataProvider.setList(spotList);
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
        String spotName = spotNameTextBox.getValue();
        spotNameTextBox.setValue("");
        spotNameTextBox.setFocus(true);
        Set<Skill> skillSet = new HashSet<>(requiredSkillsTagsInput.getItems());

        SpotRestServiceBuilder.addSpot(tenantId, new Spot(tenantId, spotName, skillSet),
                new FailureShownRestCallback<Spot>() {

                    @Override
                    public void onSuccess(Spot spot) {
                        refreshTable();
                    }
                });
    }

}
