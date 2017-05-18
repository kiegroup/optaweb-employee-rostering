package org.optaplanner.openshift.employeerostering.gwtui.client.skill;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.github.nmorel.gwtjackson.rest.api.RestCallback;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.optaplanner.openshift.employeerostering.shared.domain.Roster;
import org.optaplanner.openshift.employeerostering.shared.rest.RosterRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Templated
public class SkillListPanel implements IsElement {

    private Long tenantId = -1L;

    @Inject
    @DataField
    private TextBox skillNameTextBox;
    @Inject
    @DataField
    private Button addButton;

    // TODO use DataGrid instead
    @DataField
    private CellTable<Skill> table = new CellTable<>(10);
    @Inject
    @DataField
    private Pagination pagination;

    private SimplePager pager = new SimplePager();
    private ListDataProvider<Skill> dataProvider = new ListDataProvider<>();

    @PostConstruct
    protected void initWidget() {
        initTable();
        refreshData();
    }

    private void initTable() {
        table.addColumn(new TextColumn<Skill>() {
            @Override
            public String getValue(Skill skill) {
                return String.valueOf(skill.getName());
            }
        }, "Name");

        Column<Skill, String> deleteColumn = new Column<Skill, String>(new ButtonCell(ButtonType.DANGER, IconType.REMOVE)) {
            @Override
            public String getValue(Skill skill) {
                return "Delete";
            }
        };
        deleteColumn.setFieldUpdater((index, skill, value) -> {
            SkillRestServiceBuilder.removeSkill(tenantId, skill.getId(), new RestCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean removed) {
                    refreshData();
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

    private void refreshData() {
        SkillRestServiceBuilder.getSkillList(tenantId, new RestCallback<List<Skill>>() {
            @Override
            public void onSuccess(List<Skill> skillList) {
                dataProvider.setList(skillList);
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
        String skillName = skillNameTextBox.getValue();
        skillNameTextBox.setValue("");
        SkillRestServiceBuilder.addSkill(tenantId, new Skill(null, skillName), new RestCallback<Long>() {
            @Override
            public void onSuccess(Long skillId) {
                refreshData();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Window.alert("Failure calling REST method: " + throwable.getMessage());
                throw new IllegalStateException("REST call failure", throwable);
            }
        });
    }

}
