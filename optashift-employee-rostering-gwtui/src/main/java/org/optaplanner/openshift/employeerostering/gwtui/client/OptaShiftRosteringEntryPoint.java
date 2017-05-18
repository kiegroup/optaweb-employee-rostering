package org.optaplanner.openshift.employeerostering.gwtui.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.github.nmorel.gwtjackson.rest.api.RestRequestBuilder;
import org.jboss.errai.common.client.dom.Document;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.optaplanner.openshift.employeerostering.gwtui.client.app.MenuPanel;

@EntryPoint
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
    }

}
