package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.resources.css.CssResources;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.tenant.Tenant;

import static org.optaplanner.openshift.employeerostering.gwtui.client.resources.i18n.OptaShiftUIConstants.*;

@Templated
public class SpotListPanel implements IsElement {

    private Integer tenantId = null;

    @Inject @DataField
    private Button refreshButton;

    @Inject @DataField
    private TextBox spotNameTextBox;
    @Inject @DataField
    private ListBox requiredSkillListBox;
    private List<Skill> requiredSkillListBoxValues;
    @Inject @DataField
    private Button addButton;

    // TODO use DataGrid instead
    @DataField
    private CellTable<Spot> table;
    @DataField
    private Pagination pagination;

    private SimplePager pager = new SimplePager();
    private ListDataProvider<Spot> dataProvider = new ListDataProvider<>();

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
                requiredSkillListBoxValues = skillList;
                requiredSkillListBox.clear();
                skillList.forEach(skill -> requiredSkillListBox.addItem(skill.getName()));
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
                Skill requiredSkill = spot.getRequiredSkill();
                if (requiredSkill == null) {
                    return "";
                }
                return requiredSkill.getName();
            }
        }, "Required skill");
        Column<Spot, String> deleteColumn = new Column<Spot, String>(new ButtonCell(IconType.REMOVE, ButtonType.DANGER, ButtonSize.SMALL)) {
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
        Column<Spot, String> editColumn = new Column<Spot, String>(new ButtonCell(IconType.EDIT, ButtonType.DEFAULT, ButtonSize.SMALL)) {
            @Override
            public String getValue(Spot spot) {
                return CONSTANTS.format(General_edit);
            }
        };
        editColumn.setFieldUpdater((index, spot, value) -> {
            CssResources.INSTANCE.popup().ensureInjected();
            PopupPanel popup = new PopupPanel(false);
            popup.setGlassEnabled(true);
            popup.setStyleName(CssResources.INSTANCE.popup().panel());
            
            VerticalPanel panel = new VerticalPanel();
            HorizontalPanel datafield = new HorizontalPanel();
            
            Label label = new Label("Spot Name");
            TextBox spotName = new TextBox();
            spotName.setValue(spot.getName());
            spotName.setStyleName(CssResources.INSTANCE.popup().textbox());
            datafield.add(label);
            datafield.add(spotName);
            panel.add(datafield);
            
            label = new Label("Required Skill");
            ListBox requiredSkillBox = new ListBox();
            requiredSkillListBoxValues.forEach((s) -> requiredSkillBox.addItem(s.getName()));
            requiredSkillBox.setItemSelected(requiredSkillListBoxValues.indexOf(spot.getRequiredSkill()), true);
            datafield.add(label);
            datafield.add(requiredSkillBox);
            panel.add(datafield);
            
            datafield = new HorizontalPanel();
            Button confirm = new Button();
            confirm.setText(CONSTANTS.format(General_update));
            confirm.addClickHandler((e) -> {
                spot.setName(spotName.getValue());
                int requiredSkillIndex = requiredSkillBox.getSelectedIndex();
                spot.setRequiredSkill(requiredSkillIndex < 0 ? null : requiredSkillListBoxValues.get(requiredSkillIndex));
                popup.hide();
                SpotRestServiceBuilder.updateSpot(tenantId, spot, new FailureShownRestCallback<Spot>() {
                    @Override
                    public void onSuccess(Spot spot) {
                        refreshTable();
                    }
                });
            });
            
            Button cancel = new Button();
            cancel.setText(CONSTANTS.format(General_cancel));
            cancel.addClickHandler((e) -> popup.hide());
            
            datafield.add(confirm);
            datafield.add(cancel);
            panel.add(datafield);
            
            popup.setWidget(panel);
            popup.center();
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
        int requiredSkillIndex = requiredSkillListBox.getSelectedIndex();
        Skill requiredSkill = requiredSkillIndex < 0 ? null : requiredSkillListBoxValues.get(requiredSkillIndex);

        SpotRestServiceBuilder.addSpot(tenantId, new Spot(tenantId, spotName, requiredSkill), new FailureShownRestCallback<Spot>() {
            @Override
            public void onSuccess(Spot spot) {
                refreshTable();
            }
        });
    }

}
