package org.optaplanner.openshift.employeerostering.gwtui.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.github.nmorel.gwtjackson.rest.api.RestRequestBuilder;
import org.jboss.errai.common.client.dom.Document;
import org.jboss.errai.ioc.client.api.EntryPoint;
import org.optaplanner.openshift.employeerostering.gwtui.client.skill.SkillListPanel;

@EntryPoint
public class OptaShiftRosteringEntryPoint {

    static {
        // Keep in sync with web.xml
        RestRequestBuilder.setDefaultApplicationPath("/rest");
    }

    @Inject
    private Document document;

    @Inject
    private SkillListPanel skillListPanel;

    @PostConstruct
    public void onModuleLoad() {

        document.getBody().appendChild(skillListPanel.getElement());
//        Button b = new Button("Click me 3",
//                (ClickHandler) event -> Window.alert("OptaShiftRosteringEntryPoint, the new GWT stuff"));
//        RootPanel.get().add(b);

//        SkillListPanel skillListPanel = new SkillListPanel();
//        Document.get().getBody().appendChild(skillListPanel.getElement());
//        RosterListPanel rosterListPanel = new RosterListPanel();
//        Document.get().getBody().appendChild(rosterListPanel.getElement());

    }

}
