package org.optaplanner.openshift.workerrostering.gwtui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;

public class Hello implements EntryPoint {

    public void onModuleLoad() {
        Button b = new Button("Click me",
                (ClickHandler) event -> Window.alert("Hello, AJAX"));

        RootPanel.get().add(b);
    }

}
