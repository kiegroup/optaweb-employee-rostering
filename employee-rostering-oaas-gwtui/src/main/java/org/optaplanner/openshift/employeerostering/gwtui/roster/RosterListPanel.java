package org.optaplanner.openshift.employeerostering.gwtui.roster;

import java.util.List;

import com.github.nmorel.gwtjackson.rest.api.RestCallback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.optaplanner.openshift.employeerostering.shared.domain.Roster;
import org.optaplanner.openshift.employeerostering.shared.rest.RosterServiceBuilder;

public class RosterListPanel extends Composite {

    interface RosterListUiBinder extends UiBinder<Widget, RosterListPanel> {}
    private static final RosterListUiBinder uiBinder = GWT.create(RosterListUiBinder.class);

    @UiField
    protected ListBox listBox;

//    @UiField(provided = true)
//    CellTable<Roster> rosterTable = new CellTable<Roster>(10);

    public RosterListPanel() {
        // sets listBox
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    protected void initWidget(Widget widget) {
        super.initWidget(widget);
        RosterServiceBuilder.getRosterList(new RestCallback<List<Roster>>() {
            @Override
            public void onSuccess(List<Roster> rosterList) {
                for (Roster roster : rosterList) {
                    listBox.addItem(roster.getSkillList().size() + " skills"); // TODO
                }
            }
        });
    }
}
