package org.optaplanner.openshift.workerrostering.gwtui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import org.optaplanner.openshift.workerrostering.domain.Employee;
import org.optaplanner.openshift.workerrostering.gwtui.client.welcome.Welcome;

public class OptaShiftRosteringEntryPoint implements EntryPoint {

    public void onModuleLoad() {
//        Button b = new Button("Click me 3",
//                (ClickHandler) event -> Window.alert("OptaShiftRosteringEntryPoint, the new GWT stuff"));
//        RootPanel.get().add(b);

        Welcome welcome = new Welcome(
                new Employee("Ann"),
                new Employee("Beth"),
                new Employee("Carl"));
        Document.get().getBody().appendChild(welcome.getElement());
    }

}
