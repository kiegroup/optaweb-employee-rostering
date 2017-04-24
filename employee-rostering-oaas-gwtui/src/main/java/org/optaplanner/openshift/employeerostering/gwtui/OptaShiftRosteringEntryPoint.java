package org.optaplanner.openshift.employeerostering.gwtui;

import com.github.nmorel.gwtjackson.rest.api.RestRequestBuilder;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import org.optaplanner.openshift.employeerostering.gwtui.roster.RosterListPanel;

public class OptaShiftRosteringEntryPoint implements EntryPoint {

    public void onModuleLoad() {
        // Keep in sync with web.xml
        RestRequestBuilder.setDefaultApplicationPath("/rest");

//        Button b = new Button("Click me 3",
//                (ClickHandler) event -> Window.alert("OptaShiftRosteringEntryPoint, the new GWT stuff"));
//        RootPanel.get().add(b);

        RosterListPanel rosterListPanel = new RosterListPanel();
        Document.get().getBody().appendChild(rosterListPanel.getElement());
    }

}
