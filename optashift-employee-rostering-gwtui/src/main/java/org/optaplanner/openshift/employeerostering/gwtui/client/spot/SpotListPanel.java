package org.optaplanner.openshift.employeerostering.gwtui.client.spot;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.github.nmorel.gwtjackson.rest.api.RestCallback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
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
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.spot.SpotRestServiceBuilder;

@Templated
public class SpotListPanel implements IsElement {

    private Integer tenantId = -1;

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

    public SpotListPanel() {
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
        refreshRequiredSkillsListBox();
        initTable();
        refreshTable();
    }

    private void refreshRequiredSkillsListBox() {
        SkillRestServiceBuilder.getSkillList(tenantId, new RestCallback<List<Skill>>() {
            @Override
            public void onSuccess(List<Skill> skillList) {
                requiredSkillListBoxValues = skillList;
                requiredSkillListBox.clear();
                skillList.forEach(skill -> requiredSkillListBox.addItem(skill.getName()));
            }

            @Override
            public void onFailure(Throwable throwable) {
                Window.alert("Failure calling REST method: " + throwable.getMessage());
                throw new IllegalStateException("REST call failure", throwable);
            }
        });
    }

    private void initTable() {
        table.addColumn(new TextColumn<Spot>() {
            @Override
            public String getValue(Spot spot) {
                return spot.getName();
            }
        }, "Name");
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
        Column<Spot, String> deleteColumn = new Column<Spot, String>(new ButtonCell(ButtonType.DANGER, IconType.REMOVE)) {
            @Override
            public String getValue(Spot spot) {
                return "Delete";
            }
        };
        deleteColumn.setFieldUpdater((index, spot, value) -> {
            SpotRestServiceBuilder.removeSpot(tenantId, spot.getId(), new RestCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean removed) {
                    refreshTable();
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Window.alert("Failure calling REST method: " + throwable.getMessage());
                    throw new IllegalStateException("REST call failure", throwable);
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
        SpotRestServiceBuilder.getSpotList(tenantId, new RestCallback<List<Spot>>() {
            @Override
            public void onSuccess(List<Spot> spotList) {
                dataProvider.setList(spotList);
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

    @EventHandler("addButton")
    public void add(ClickEvent e) {
        String spotName = spotNameTextBox.getValue();
        spotNameTextBox.setValue("");
        spotNameTextBox.setFocus(true);
        int requiredSkillIndex = requiredSkillListBox.getSelectedIndex();
        Skill requiredSkill = requiredSkillIndex < 0 ? null : requiredSkillListBoxValues.get(requiredSkillIndex);

        SpotRestServiceBuilder.addSpot(tenantId, new Spot(tenantId, spotName, requiredSkill), new RestCallback<Long>() {
            @Override
            public void onSuccess(Long spotId) {
                refreshTable();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Window.alert("Failure calling REST method: " + throwable.getMessage());
                throw new IllegalStateException("REST call failure", throwable);
            }
        });
    }

}
