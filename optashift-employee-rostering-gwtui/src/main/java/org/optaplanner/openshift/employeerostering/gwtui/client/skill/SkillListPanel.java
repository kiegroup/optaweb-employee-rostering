package org.optaplanner.openshift.employeerostering.gwtui.client.skill;

import java.util.List;

import com.github.nmorel.gwtjackson.rest.api.RestCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.optaplanner.openshift.employeerostering.shared.domain.Roster;
import org.optaplanner.openshift.employeerostering.shared.rest.RosterRestServiceBuilder;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.skill.SkillRestServiceBuilder;

public class SkillListPanel extends Composite {

    interface SkillListUiBinder extends UiBinder<Widget, SkillListPanel> {}
    private static final SkillListUiBinder uiBinder = GWT.create(SkillListUiBinder.class);

    // TODO use DataGrid instead
    @UiField(provided = true)
    CellTable<Skill> table = new CellTable<>(10);
    @UiField
    Pagination pagination;

    private SimplePager pager = new SimplePager();
    private ListDataProvider<Skill> dataProvider = new ListDataProvider<>();

    public SkillListPanel() {
        // sets listBox
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    protected void initWidget(Widget widget) {
        super.initWidget(widget);
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

//        final Column<Employee, String> col4 = new Column<Employee, String>(new ButtonCell(ButtonType.PRIMARY, IconType.GITHUB)) {
//            @Override
//            public String getValue(Employee object) {
//                return "Click Me";
//            }
//        };
//        col4.setFieldUpdater((index, object, value) -> Window.alert("Clicked!"));
//        rosterTable.addColumn(col4, "Buttons");

        table.addRangeChangeHandler(event -> pagination.rebuild(pager));

        pager.setDisplay(table);
        pagination.clear();
        dataProvider.addDataDisplay(table);
    }

    protected void refreshData() {
        Long tenantId = -1L;
        SkillRestServiceBuilder.getSkillList(tenantId, new RestCallback<List<Skill>>() {
            @Override
            public void onSuccess(List<Skill> skillList) {
                dataProvider.setList(skillList);
                dataProvider.flush();
                pagination.rebuild(pager);
            }

            @Override
            public void onFailure(Throwable throwable) {
                throw new IllegalStateException("REST call failure", throwable);
            }
        });
    }

}
