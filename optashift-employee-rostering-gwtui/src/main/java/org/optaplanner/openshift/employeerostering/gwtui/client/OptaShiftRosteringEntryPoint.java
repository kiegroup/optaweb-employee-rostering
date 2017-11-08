package org.optaplanner.openshift.employeerostering.gwtui.client;

import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.github.nmorel.gwtjackson.rest.api.RestRequestBuilder;
import com.google.gwt.core.client.GWT;
import org.jboss.errai.common.client.dom.Document;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.ui.shared.api.annotations.Bundle;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.MenuPanel;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;

@EntryPoint
@Bundle("resources/i18n/OptaShiftUIConstants.properties")
public class OptaShiftRosteringEntryPoint {

    static {
        // Keep in sync with web.xml
        RestRequestBuilder.setDefaultApplicationPath("/rest");
    }

    @Inject
    private Document document;

    @Inject
    private MenuPanel menuPanel;

    @PostConstruct
    public void onModuleLoad() {
        document.getBody().appendChild(menuPanel.getElement());
        /*GWT.setUncaughtExceptionHandler(new
                GWT.UncaughtExceptionHandler() {
                public void onUncaughtException(Throwable e) {
                  ErrorPopup.show(Arrays.asList(e.getStackTrace()).stream().reduce("", (a,b) -> a.toString() + "\n" + b.toString(), (a,b) -> a + "\n" + b));
              }
          });*/
    }
}